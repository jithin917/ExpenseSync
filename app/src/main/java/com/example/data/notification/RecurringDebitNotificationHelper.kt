package com.example.data.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.data.model.CashProjectionSummary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RecurringDebitNotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "channel_recurring_cash_alerts"
        const val CHANNEL_NAME = "Salary, Rent & Recurring Debits"
        const val NOTIFICATION_ID = 2001
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Liquidity alerts for upcoming salary, rent, and monthly recurring bills"
                enableLights(true)
                enableVibration(true)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            manager?.createNotificationChannel(channel)
        }
    }

    /**
     * Posts a rich projection notification if there are upcoming cash obligations within the configured horizon.
     */
    fun postProjectionAlert(
        projection: CashProjectionSummary,
        currencySymbol: String = "₹",
        isManualTest: Boolean = false
    ): Boolean {
        if (!projection.notificationsEnabled && !isManualTest) {
            return false
        }

        if (projection.debitsInWindow.isEmpty() && !isManualTest) {
            // Nothing due in window
            return false
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissionGranted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!permissionGranted && !isManualTest) {
                return false
            }
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val totalFormatted = formatNumber(projection.totalRequiredAmount, currencySymbol)
        val title = if (projection.debitsInWindow.isNotEmpty()) {
            "🚨 Liquidity Alert: $totalFormatted Needed in ${projection.lookaheadDays} Days"
        } else {
            "✅ Cashflow Healthy: No debits due in next ${projection.lookaheadDays} days"
        }

        val shortSummary = buildString {
            if (projection.salaryTotal > 0) {
                append("👥 Salaries: ${formatNumber(projection.salaryTotal, currencySymbol)}  ")
            }
            if (projection.rentTotal > 0) {
                append("🏢 Rent: ${formatNumber(projection.rentTotal, currencySymbol)}  ")
            }
            if (projection.utilitiesTotal > 0 || projection.subscriptionsTotal > 0) {
                val bills = projection.utilitiesTotal + projection.subscriptionsTotal
                append("⚡ Bills: ${formatNumber(bills, currencySymbol)}")
            }
        }.ifBlank { "All recurring debits are funded." }

        val bigText = buildString {
            appendLine("Upcoming obligations in next ${projection.lookaheadDays} days:")
            if (projection.debitsInWindow.isEmpty()) {
                appendLine("• All recurring obligations for the cycle are cleared!")
            } else {
                for (debit in projection.debitsInWindow.take(6)) {
                    val sdf = SimpleDateFormat("MMM d", Locale.getDefault())
                    val dueStr = sdf.format(Date(debit.nextDueDate))
                    val status = when {
                        debit.isFlexibleSchedule -> "🔄 Flexible / As Needed"
                        debit.daysRemaining < 0 -> "⚠️ OVERDUE (${-debit.daysRemaining}d ago)"
                        debit.daysRemaining == 0 -> "🔥 DUE TODAY ($dueStr)"
                        debit.daysRemaining == 1 -> "⏰ Due Tomorrow ($dueStr)"
                        else -> "Due in ${debit.daysRemaining}d ($dueStr)"
                    }
                    val amt = formatNumber(debit.amount, currencySymbol)
                    appendLine("• ${debit.name}: $amt ($status)")
                }
                if (projection.debitsInWindow.size > 6) {
                    val remaining = projection.debitsInWindow.size - 6
                    appendLine("• ...and $remaining more obligations")
                }
            }
            appendLine()
            appendLine("Total Required: $totalFormatted")
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(shortSummary)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(NOTIFICATION_ID, builder.build())
            return true
        } catch (_: SecurityException) {
            return false
        }
    }

    private fun formatNumber(amt: Double, sym: String): String {
        return if (amt % 1.0 == 0.0) "$sym${amt.toLong()}" else "$sym%.2f".format(amt)
    }
}
