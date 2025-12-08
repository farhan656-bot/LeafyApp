package com.example.leafy.screens

import com.example.leafy.data.PlantEntity
import java.util.Locale
import java.util.concurrent.TimeUnit

/** Teks jadwal siram + pupuk untuk list & detail */
fun buildScheduleText(plant: PlantEntity): String {
    val waterPart = if (plant.waterFrequency.isNotBlank() || plant.waterDay.isNotBlank()) {
        val freq = plant.waterFrequency.ifBlank { "-" }
        val day = plant.waterDay
            .takeIf { it.isNotBlank() }
            ?.let { " ${it.lowercase(Locale.getDefault())}" } ?: ""
        "Siraman: $freq$day"
    } else {
        "Siraman: -"
    }

    val fertilizerPart =
        if (!plant.fertilizerFrequency.isNullOrBlank() || !plant.fertilizerDay.isNullOrBlank()) {
            val freq = plant.fertilizerFrequency?.ifBlank { "-" } ?: "-"
            val day = plant.fertilizerDay
                ?.takeIf { it.isNotBlank() }
                ?.let { " ${it.lowercase(Locale.getDefault())}" } ?: ""
            " | Pupuk: $freq$day"
        } else {
            ""
        }

    return waterPart + fertilizerPart
}

/** Teks keterangan terakhir disiram */
fun formatLastWateredLabel(lastWatered: Long?): String {
    if (lastWatered == null) return "Belum pernah disiram"
    val daysAgo =
        TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - lastWatered).toInt()
    return when {
        daysAgo <= 0 -> "Disiram hari ini"
        daysAgo == 1 -> "Terakhir disiram 1 hari lalu"
        else -> "Terakhir disiram $daysAgo hari lalu"
    }
}

/** Konversi frekuensi ke interval hari */
fun frequencyToIntervalDays(frequency: String): Int? {
    return when (frequency.lowercase(Locale.getDefault())) {
        "1x sehari", "2x sehari" -> 1
        "1x seminggu" -> 7
        "pilih waktu", "" -> null
        else -> 1
    }
}
