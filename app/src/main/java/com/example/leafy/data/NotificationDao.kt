package com.example.leafy.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {

    // --- PERBAIKAN DI SINI ---
    // Ubah namanya menjadi 'insert' agar cocok dengan PlantDetailScreen
    @Insert
    suspend fun insert(notification: Notification)
    // -------------------------

    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<Notification>>

    // Fungsi penting untuk menghitung notifikasi belum dibaca
    @Query("SELECT COUNT(*) FROM notifications WHERE isRead = 0")
    fun getUnreadCount(): Flow<Int>

    @Query("UPDATE notifications SET isRead = 1 WHERE isRead = 0")
    suspend fun markAllAsRead()
}