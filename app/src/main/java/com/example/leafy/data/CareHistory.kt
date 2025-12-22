package com.example.leafy.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "care_history")
data class CareHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val plantId: Int,
    val careTimestamp: Long,
    val careType: String
)