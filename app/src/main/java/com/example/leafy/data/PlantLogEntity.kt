package com.example.leafy.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "plant_logs",
    foreignKeys = [
        ForeignKey(
            entity = PlantEntity::class,
            parentColumns = ["id"],
            childColumns = ["plantId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("plantId")]
)
data class PlantLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val plantId: Int,
    val photoUri: String?,          // foto dari galeri/kamera
    val note: String?,              // catatan harian
    val createdAt: Long = System.currentTimeMillis(),

    // nanti untuk modul lokasi (GPS)
    val latitude: Double? = null,
    val longitude: Double? = null
)
