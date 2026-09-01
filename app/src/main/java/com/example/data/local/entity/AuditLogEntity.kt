package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey
    val id: String,
    val transactionId: String,
    val action: String, // CREATED, UPDATED, DELETED, SYNCED
    val details: String,
    val timestamp: Long = System.currentTimeMillis()
)
