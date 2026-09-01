package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "receipts",
    foreignKeys = [
        ForeignKey(
            entity = TransactionEntity::class,
            parentColumns = ["id"],
            childColumns = ["transactionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("transactionId")]
)
data class ReceiptEntity(
    @PrimaryKey
    val id: String,
    val transactionId: String,
    val localFilePath: String,
    val cloudUrl: String? = null,
    val fileSizeKb: Long = 0,
    val mimeType: String = "image/jpeg",
    val syncStatus: String = "PENDING",
    val createdAt: Long = System.currentTimeMillis()
)
