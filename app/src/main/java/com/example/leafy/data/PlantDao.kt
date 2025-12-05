package com.example.leafy.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
@Dao
interface PlantDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlant(plant: PlantEntity)


    @Update
    suspend fun updatePlant(plant: PlantEntity)

    @Query("SELECT * FROM plants ORDER BY id DESC")
    suspend fun getAllPlants(): List<PlantEntity>

    @Query("SELECT * FROM plants ORDER BY id DESC")
    fun observePlants(): Flow<List<PlantEntity>>

    @Query("SELECT * FROM plants WHERE id = :id")
    suspend fun getPlantById(id: Int): PlantEntity?

    @Query("SELECT * FROM plants WHERE id = :id")
    fun observePlantById(id: Int): Flow<PlantEntity?>
    @Query("DELETE FROM plants WHERE id = :id")
    suspend fun deletePlant(id: Int)


    @Query("UPDATE plants SET lastWatered = :lastWatered WHERE id = :id")
    suspend fun updateLastWatered(id: Int, lastWatered: Long)
}
