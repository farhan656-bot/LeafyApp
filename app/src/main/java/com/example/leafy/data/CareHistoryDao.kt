package com.example.leafy.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CareHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCareHistory(history: CareHistory)

    @Query("SELECT * FROM care_history WHERE plantId = :plantId ORDER BY careTimestamp DESC")
    fun getHistoryForPlant(plantId: Int): kotlinx.coroutines.flow.Flow<List<CareHistory>>

    @Query("SELECT COUNT(*) FROM care_history WHERE careType = 'water' AND careTimestamp >= :sinceTimestamp")
    suspend fun getWateringCountSince(sinceTimestamp: Long): Int

    @Query("DELETE FROM care_history WHERE careTimestamp < :timestamp")
    suspend fun clearHistoryBefore(timestamp: Long)
}
