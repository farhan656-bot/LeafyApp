package com.example.leafy.utils

import android.content.Context
import android.net.Uri
import android.os.Environment
import java.io.File

object StorageUtils {

    fun copyUriToAppStorage(context: Context, sourceUri: Uri): String? {
        val input = context.contentResolver.openInputStream(sourceUri) ?: return null

        val dir = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "Leafy")
        if (!dir.exists()) dir.mkdirs()

        val outFile = File(dir, "plant_${System.currentTimeMillis()}.jpg")
        outFile.outputStream().use { output ->
            input.use { it.copyTo(output) }
        }

        // file://... supaya aman & tidak hilang saat app dibuka ulang
        return Uri.fromFile(outFile).toString()
    }
}
