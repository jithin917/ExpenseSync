package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val parentId: String? = null,
    val iconName: String = "category",
    val colorHex: String = "#0284C7",
    val isActive: Boolean = true,
    val syncStatus: String = "SYNCED",
    val updatedAt: Long = System.currentTimeMillis()
)
