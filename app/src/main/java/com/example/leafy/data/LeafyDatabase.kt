package com.example.leafy.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.leafy.data.CareHistory

// --- PERUBAHAN 1: Tambahkan Notification::class dan Ubah version ke 2 ---
@Database(
    entities = [UserEntity::class, PlantEntity::class, Notification::class, CareHistory::class], // Pastikan semua entity ada
    version = 3,
    exportSchema = false
)
abstract class LeafyDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun plantDao(): PlantDao

    // --- PERUBAHAN 2: Tambahkan DAO Notifikasi & CareHistory ---
    abstract fun notificationDao(): NotificationDao
    abstract fun careHistoryDao(): CareHistoryDao // Perbaikan: Nama fungsi diubah ke lower camel case

    companion object {
        @Volatile
        private var INSTANCE: LeafyDatabase? = null

        fun getDatabase(context: Context): LeafyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LeafyDatabase::class.java,
                    "leafy_database"
                )
                    .fallbackToDestructiveMigration() // Ini akan menghapus data lama saat versi naik
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}
