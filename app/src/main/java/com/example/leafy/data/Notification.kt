package com.example.leafy.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class Notification(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val message: String,
    val timestamp: Long,
    val title: String,
    val isRead: Boolean = false // Ini penting untuk menghitung notifikasi baru
)