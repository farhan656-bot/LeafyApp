package com.example.leafy.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.leafy.data.CareHistory


@Database(
    entities = [UserEntity::class, PlantEntity::class, Notification::class, CareHistory::class],
    version = 3,
    exportSchema = false
)
abstract class LeafyDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun plantDao(): PlantDao


    abstract fun notificationDao(): NotificationDao
    abstract fun careHistoryDao(): CareHistoryDao

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
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}
