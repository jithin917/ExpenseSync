package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Payment Source: Credit Card (CC), GPAY / UPI, Cash, or Savings Bank (SB)
 * Stored securely locally.
 */
@Entity(tableName = "payment_sources")
data class PaymentSourceEntity(
    @PrimaryKey
    val id: String,
    val type: String, // "CC", "UPI", "CASH", "SB"
    val bankName: String,
    val cardName: String? = null, // e.g. "Corporate Platinum", "Regalia", "GPAY"
    val accountNumber: String? = null, // Masked representation e.g. "**** 5678" or UPI ID
    val ifscOrRouting: String? = null, // For SB
    val last4Digits: String? = null, // For CC e.g. "4892"
    val isDefault: Boolean = false,
    val syncStatus: String = "SYNCED",
    val updatedAt: Long = System.currentTimeMillis()
) {
    val displayLabel: String
        get() = when (type.uppercase()) {
            "CC" -> {
                val cardDesc = if (!cardName.isNullOrBlank()) "$cardName " else ""
                "$bankName ${cardDesc}CC **** ${last4Digits ?: "0000"}"
            }
            "UPI" -> {
                val upiDesc = if (!cardName.isNullOrBlank() && !bankName.contains(cardName, ignoreCase = true)) " ($cardName)" else ""
                "$bankName$upiDesc"
            }
            "CASH" -> {
                bankName
            }
            else -> {
                val mask = accountNumber?.takeLast(4) ?: "XXXX"
                "$bankName SB **** $mask"
            }
        }

    val tallyLedgerName: String
        get() = displayLabel
}
