package com.example.leafy.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "plants")
data class PlantEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val waterFrequency: String,
    val waterDay: String,
    val fertilizerFrequency: String? = null,
    val fertilizerDay: String? = null,
    val location: String? = null,
    val notes: String? = null,
    val imageUri: String? = null,
    val lastWatered: Long? = null
)
