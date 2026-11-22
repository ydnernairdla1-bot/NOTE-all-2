package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "note_categories")
data class NoteCategoryEntity(
    @PrimaryKey val name: String,
    val createdAt: Long = System.currentTimeMillis(),
    val isDefault: Boolean = false
)
