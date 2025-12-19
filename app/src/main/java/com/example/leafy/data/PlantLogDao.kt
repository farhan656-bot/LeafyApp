package com.example.leafy.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PlantLogDao {

    // ✅ cukup SATU insertLog saja (hapus duplikat!)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: PlantLogEntity)

    @Query("SELECT * FROM plant_logs WHERE plantId = :plantId ORDER BY createdAt DESC")
    suspend fun getLogsByPlant(plantId: Int): List<PlantLogEntity>

    @Query("SELECT * FROM plant_logs ORDER BY createdAt DESC")
    suspend fun getAllLogs(): List<PlantLogEntity>

    @Query("SELECT * FROM plant_logs WHERE id = :logId LIMIT 1")
    suspend fun getLogById(logId: Int): PlantLogEntity?

    @Query("DELETE FROM plant_logs WHERE id = :logId")
    suspend fun deleteLog(logId: Int)

    @Query("SELECT * FROM plant_logs ORDER BY createdAt DESC")
    fun observeAllLogs(): Flow<List<PlantLogEntity>>

    @Query("SELECT * FROM plant_logs WHERE plantId = :plantId ORDER BY createdAt DESC")
    fun observeLogsByPlantId(plantId: Int): Flow<List<PlantLogEntity>>
}
