package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "payees")
data class PayeeEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val categoryTag: String? = null,
    val defaultCategoryId: String? = null,
    val defaultPaymentSourceId: String? = null,
    val defaultPaymentMode: String? = null, // e.g. "GPAY UPI / CASH", "Credit Card (CC)", "UPI", "CASH", "CC", "SB"
    val defaultAmount: Double? = null, // Recurring or default fixed amount e.g. 15000 for rent, 499 for recharge
    val isRecurring: Boolean = false,
    val recurringFrequency: String? = "Monthly", // "Monthly", "Quarterly", "Annual", "Fixed"
    val dueDayOfMonth: Int? = null, // Day of month when debit is due (1..31)
    val recurringType: String? = null, // "SALARY", "RENT", "UTILITY", "SUBSCRIPTION", "EMI", "VENDOR", "OTHER"
    val isFlexibleSchedule: Boolean = false, // If true, charged as and when required (e.g. secondary SIM, on-demand recharge) - no strict overdue alerts
    val lastPaidDate: Long? = null, // Timestamp when this was last paid
    val hasGstBill: Boolean = false,
    val gstNumber: String? = null,
    val phoneOrContact: String? = null,
    val notes: String? = null,
    val isDefault: Boolean = false,
    val syncStatus: String = "PENDING",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val displaySummary: String
        get() = buildString {
            append(name)
            if (!categoryTag.isNullOrBlank()) {
                append(" ($categoryTag)")
            }
            if (defaultAmount != null && defaultAmount > 0) {
                val amtStr = if (defaultAmount % 1.0 == 0.0) "₹${defaultAmount.toLong()}" else "₹%.2f".format(defaultAmount)
                val freq = if (isRecurring && !recurringFrequency.isNullOrBlank()) "/$recurringFrequency" else ""
                append(" • $amtStr$freq")
            }
            if (!defaultPaymentMode.isNullOrBlank()) {
                append(" • Mode: $defaultPaymentMode")
            }
            if (hasGstBill && !gstNumber.isNullOrBlank()) {
                append(" • GST: $gstNumber")
            }
        }
}
