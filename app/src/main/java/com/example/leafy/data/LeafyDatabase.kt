package com.example.leafy.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [UserEntity::class, PlantEntity::class, PlantLogEntity::class, CareHistory::class, Notification::class], version = 5, exportSchema = false)
abstract class LeafyDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun plantDao(): PlantDao
    abstract fun plantLogDao(): PlantLogDao
    abstract fun careHistoryDao(): CareHistoryDao
    abstract fun notificationDao(): NotificationDao

    companion object {
        @Volatile
        private var INSTANCE: LeafyDatabase? = null

        fun getDatabase(context: Context): LeafyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LeafyDatabase::class.java,
                    "leafy_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
