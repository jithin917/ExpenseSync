package com.example.data.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.data.local.AppDatabase
import com.example.data.local.entity.AppSettingEntity
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.PayeeEntity
import com.example.data.local.entity.TransactionWithDetails
import com.example.data.model.CashProjectionEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar

class RecurringDebitAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        val scope = CoroutineScope(Dispatchers.IO)

        scope.launch {
            try {
                val db = AppDatabase.getDatabase(context)
                val settingsList: List<AppSettingEntity> = db.appSettingDao().getAllSettings().first()
                val settingsMap = settingsList.associate { it.key to it.value }

                val notifsEnabled = settingsMap["recurring_notifications_enabled"]?.toBoolean() ?: true
                val lookaheadDays = settingsMap["projection_lookahead_days"]?.toIntOrNull() ?: 10
                val alertThreshold = settingsMap["recurring_alert_threshold_days"]?.toIntOrNull() ?: 3
                val currencySymbol = settingsMap["currency_symbol"] ?: "₹"

                if (notifsEnabled) {
                    val payees: List<PayeeEntity> = db.payeeDao().getAllPayees().first()
                    val categories: List<CategoryEntity> = db.categoryDao().getAllActiveCategories().first()
                    val transactions: List<TransactionWithDetails> = db.transactionDao().getAllTransactionsWithDetails().first()

                    val projection = CashProjectionEngine.calculateProjection(
                        payees = payees,
                        categories = categories,
                        transactions = transactions,
                        lookaheadDays = lookaheadDays,
                        notificationsEnabled = notifsEnabled,
                        alertThresholdDays = alertThreshold
                    )

                    if (projection.debitsInWindow.isNotEmpty() || projection.urgentCount > 0) {
                        val helper = RecurringDebitNotificationHelper(context)
                        helper.postProjectionAlert(
                            projection = projection,
                            currencySymbol = currencySymbol,
                            isManualTest = false
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("RecurringAlarmReceiver", "Error evaluating recurring debit alarm", e)
            } finally {
                // Schedule next alarm for 9:00 AM next day
                scheduleDailyProjectionCheck(context)
                pendingResult.finish()
            }
        }
    }

    companion object {
        private const val REQUEST_CODE = 4401

        fun scheduleDailyProjectionCheck(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val intent = Intent(context, RecurringDebitAlarmReceiver::class.java)
            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
            val pendingIntent = PendingIntent.getBroadcast(context, REQUEST_CODE, intent, flags)

            val calendar = Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis()
                set(Calendar.HOUR_OF_DAY, 9)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                if (before(Calendar.getInstance())) {
                    add(Calendar.DAY_OF_MONTH, 1)
                }
            }

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.set(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                }
            } catch (e: Exception) {
                Log.e("RecurringAlarmReceiver", "Failed to set alarm", e)
            }
        }
    }
}
