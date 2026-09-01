package com.example.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

data class TransactionWithDetails(
    @Embedded val transaction: TransactionEntity,
    @Relation(
        parentColumn = "categoryId",
        entityColumn = "id"
    )
    val category: CategoryEntity?,
    @Relation(
        parentColumn = "sourceId",
        entityColumn = "id"
    )
    val paymentSource: PaymentSourceEntity?,
    @Relation(
        parentColumn = "payeeId",
        entityColumn = "id"
    )
    val payee: PayeeEntity? = null,
    @Relation(
        parentColumn = "id",
        entityColumn = "transactionId"
    )
    val receipts: List<ReceiptEntity> = emptyList()
)
