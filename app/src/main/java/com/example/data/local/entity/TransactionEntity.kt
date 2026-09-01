package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = PaymentSourceEntity::class,
            parentColumns = ["id"],
            childColumns = ["sourceId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = PayeeEntity::class,
            parentColumns = ["id"],
            childColumns = ["payeeId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("categoryId"),
        Index("sourceId"),
        Index("payeeId"),
        Index("transactionDate")
    ]
)
data class TransactionEntity(
    @PrimaryKey
    val id: String,
    val amount: Double,
    val transactionDate: Long, // timestamp in ms
    val categoryId: String?,
    val sourceId: String?,
    val payeeId: String? = null,
    val notes: String? = null,
    val merchantName: String? = null,
    val hasGstBill: Boolean = false,
    val gstNumber: String? = null,
    val gstRate: Double? = null,
    val voucherType: String = "Payment", // Standard Tally voucher type
    val syncStatus: String = "PENDING", // PENDING, SYNCED, FAILED
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
