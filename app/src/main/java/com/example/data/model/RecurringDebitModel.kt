package com.example.data.model

import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.PayeeEntity
import com.example.data.local.entity.PaymentSourceEntity
import com.example.data.local.entity.TransactionWithDetails
import java.util.Calendar
import java.util.Date

enum class RecurringDebitType(
    val title: String,
    val shortLabel: String,
    val iconName: String,
    val colorHex: String
) {
    SALARY("Staff Salaries & Wages", "Salary", "payments", "#16A34A"),
    RENT("Office & Workshop Rent", "Rent", "hotel", "#EA580C"),
    UTILITY("Electricity & Power Bills", "Electricity", "bolt", "#CA8A04"),
    SUBSCRIPTION("Internet & Phone Recharges", "Broadband", "wifi", "#0284C7"),
    EMI("Loan & Machinery EMI", "EMI", "account_balance", "#7C3AED"),
    VENDOR("Vendor Fixed Retainer", "Vendor", "inventory", "#0D9488"),
    OTHER("Other Recurring Debit", "Other", "schedule", "#64748B");

    companion object {
        fun fromString(typeStr: String?, categoryTag: String?, name: String?): RecurringDebitType {
            if (!typeStr.isNullOrBlank()) {
                val matched = entries.firstOrNull { it.name.equals(typeStr, ignoreCase = true) }
                if (matched != null) return matched
            }

            val text = "${categoryTag ?: ""} ${name ?: ""}".lowercase()
            return when {
                text.contains("salary") || text.contains("wage") || text.contains("payroll") || text.contains("worker") -> SALARY
                text.contains("rent") || text.contains("lease") || text.contains("landlord") -> RENT
                text.contains("electric") || text.contains("kseb") || text.contains("power") || text.contains("utility") || text.contains("water") -> UTILITY
                text.contains("airtel") || text.contains("internet") || text.contains("wifi") || text.contains("recharge") || text.contains("phone") || text.contains("fiber") -> SUBSCRIPTION
                text.contains("emi") || text.contains("loan") || text.contains("finance") -> EMI
                text.contains("lining") || text.contains("fabric") || text.contains("vendor") || text.contains("tailor") -> VENDOR
                else -> OTHER
            }
        }
    }
}

data class UpcomingDebitItem(
    val payeeId: String,
    val name: String,
    val recurringType: RecurringDebitType,
    val amount: Double,
    val frequency: String,
    val dueDayOfMonth: Int,
    val nextDueDate: Long,
    val daysRemaining: Int, // 0 = Due Today, 1 = Due Tomorrow, -X = Overdue by X days, +X = Due in X days
    val isDueWithinWindow: Boolean,
    val isPaidInCurrentCycle: Boolean,
    val isFlexibleSchedule: Boolean = false, // Flexible / As-needed vs Strict Due Date
    val defaultCategoryId: String?,
    val defaultCategoryName: String?,
    val defaultPaymentMode: String?,
    val defaultPaymentSourceId: String?,
    val phoneOrContact: String?,
    val notes: String?
) {
    val statusLabel: String
        get() = when {
            isPaidInCurrentCycle -> "Paid this month"
            isFlexibleSchedule -> "As Needed / Flexible"
            daysRemaining < 0 -> "Overdue by ${-daysRemaining}d"
            daysRemaining == 0 -> "Due Today"
            daysRemaining == 1 -> "Due in 1 day (Tomorrow)"
            else -> "Due in $daysRemaining days"
        }
}

data class CashProjectionSummary(
    val lookaheadDays: Int = 10,
    val totalRequiredAmount: Double = 0.0,
    val salaryTotal: Double = 0.0,
    val rentTotal: Double = 0.0,
    val utilitiesTotal: Double = 0.0,
    val subscriptionsTotal: Double = 0.0,
    val otherTotal: Double = 0.0,
    val allDebits: List<UpcomingDebitItem> = emptyList(),
    val debitsInWindow: List<UpcomingDebitItem> = emptyList(),
    val urgentCount: Int = 0,
    val notificationsEnabled: Boolean = true,
    val alertThresholdDays: Int = 3
)

object CashProjectionEngine {

    /**
     * Calculates upcoming recurring debits and liquidity projections based on current date,
     * configured lookahead days, payees list, categories, and transaction history.
     */
    fun calculateProjection(
        payees: List<PayeeEntity>,
        categories: List<CategoryEntity>,
        transactions: List<TransactionWithDetails>,
        lookaheadDays: Int = 10,
        notificationsEnabled: Boolean = true,
        alertThresholdDays: Int = 3,
        nowTimestamp: Long = System.currentTimeMillis()
    ): CashProjectionSummary {
        val catMap = categories.associateBy { it.id }
        val recurringPayees = payees.filter { it.isRecurring || (it.defaultAmount != null && it.defaultAmount > 0) }

        val todayCal = Calendar.getInstance().apply { timeInMillis = nowTimestamp }
        val currentYear = todayCal.get(Calendar.YEAR)
        val currentMonth = todayCal.get(Calendar.MONTH)
        val currentDay = todayCal.get(Calendar.DAY_OF_MONTH)

        // Month boundary for current cycle check
        val currentMonthStartCal = Calendar.getInstance().apply {
            set(currentYear, currentMonth, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val currentMonthStart = currentMonthStartCal.timeInMillis

        val upcomingList = mutableListOf<UpcomingDebitItem>()

        for (payee in recurringPayees) {
            val amount = payee.defaultAmount ?: 0.0
            if (amount <= 0.0) continue

            val type = RecurringDebitType.fromString(payee.recurringType, payee.categoryTag, payee.name)
            val dueDayRaw = payee.dueDayOfMonth ?: when (type) {
                RecurringDebitType.RENT -> 1
                RecurringDebitType.SALARY -> 5
                RecurringDebitType.UTILITY -> 10
                RecurringDebitType.SUBSCRIPTION -> 15
                RecurringDebitType.EMI -> 20
                else -> 1
            }

            val maxDaysInCurrentMonth = todayCal.getActualMaximum(Calendar.DAY_OF_MONTH)
            val effectiveDueDay = dueDayRaw.coerceIn(1, maxDaysInCurrentMonth)

            // Check if this payee was paid in the current month/cycle
            val isPaidThisCycle = transactions.any { txnItem ->
                val txnDate = txnItem.transaction.transactionDate
                val matchesPayee = txnItem.transaction.payeeId == payee.id ||
                    (payee.defaultCategoryId != null && txnItem.transaction.categoryId == payee.defaultCategoryId &&
                        txnItem.transaction.merchantName?.contains(payee.name, ignoreCase = true) == true)
                matchesPayee && txnDate >= currentMonthStart
            }

            // Calculate next due date
            val dueCal = Calendar.getInstance().apply {
                timeInMillis = nowTimestamp
                set(Calendar.HOUR_OF_DAY, 9)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            if (isPaidThisCycle) {
                // Next due date is next month
                dueCal.add(Calendar.MONTH, 1)
                val maxNextMonthDays = dueCal.getActualMaximum(Calendar.DAY_OF_MONTH)
                dueCal.set(Calendar.DAY_OF_MONTH, dueDayRaw.coerceIn(1, maxNextMonthDays))
            } else {
                // Not yet paid in current cycle
                dueCal.set(Calendar.DAY_OF_MONTH, effectiveDueDay)
            }

            val diffMillis = dueCal.timeInMillis - nowTimestamp
            val daysRemaining = (diffMillis / (1000L * 60 * 60 * 24)).toInt()

            val isDueInWindow = !isPaidThisCycle && (
                payee.isFlexibleSchedule || (daysRemaining in -30..lookaheadDays)
            )

            val catName = payee.defaultCategoryId?.let { catMap[it]?.name } ?: payee.categoryTag ?: type.title

            upcomingList.add(
                UpcomingDebitItem(
                    payeeId = payee.id,
                    name = payee.name,
                    recurringType = type,
                    amount = amount,
                    frequency = payee.recurringFrequency ?: "Monthly",
                    dueDayOfMonth = dueDayRaw,
                    nextDueDate = dueCal.timeInMillis,
                    daysRemaining = daysRemaining,
                    isDueWithinWindow = isDueInWindow,
                    isPaidInCurrentCycle = isPaidThisCycle,
                    isFlexibleSchedule = payee.isFlexibleSchedule,
                    defaultCategoryId = payee.defaultCategoryId,
                    defaultCategoryName = catName,
                    defaultPaymentMode = payee.defaultPaymentMode,
                    defaultPaymentSourceId = payee.defaultPaymentSourceId,
                    phoneOrContact = payee.phoneOrContact,
                    notes = payee.notes
                )
            )
        }

        // Sort: Unpaid strict urgent & upcoming first, then unpaid flexible, then paid
        val sortedList = upcomingList.sortedWith(
            compareBy<UpcomingDebitItem> { it.isPaidInCurrentCycle }
                .thenBy { it.isFlexibleSchedule }
                .thenBy { it.daysRemaining }
        )

        val inWindowList = sortedList.filter { it.isDueWithinWindow }

        var totalReq = 0.0
        var salaryReq = 0.0
        var rentReq = 0.0
        var utilReq = 0.0
        var subReq = 0.0
        var otherReq = 0.0
        var urgentCount = 0

        for (item in inWindowList) {
            totalReq += item.amount
            when (item.recurringType) {
                RecurringDebitType.SALARY -> salaryReq += item.amount
                RecurringDebitType.RENT -> rentReq += item.amount
                RecurringDebitType.UTILITY -> utilReq += item.amount
                RecurringDebitType.SUBSCRIPTION -> subReq += item.amount
                RecurringDebitType.EMI, RecurringDebitType.VENDOR, RecurringDebitType.OTHER -> otherReq += item.amount
            }
            // Only count strict, non-flexible items that are approaching due date as urgent
            if (!item.isFlexibleSchedule && item.daysRemaining <= alertThresholdDays) {
                urgentCount++
            }
        }

        return CashProjectionSummary(
            lookaheadDays = lookaheadDays,
            totalRequiredAmount = totalReq,
            salaryTotal = salaryReq,
            rentTotal = rentReq,
            utilitiesTotal = utilReq,
            subscriptionsTotal = subReq,
            otherTotal = otherReq,
            allDebits = sortedList,
            debitsInWindow = inWindowList,
            urgentCount = urgentCount,
            notificationsEnabled = notificationsEnabled,
            alertThresholdDays = alertThresholdDays
        )
    }
}
