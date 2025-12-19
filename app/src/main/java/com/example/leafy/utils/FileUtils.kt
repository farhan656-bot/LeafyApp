package com.example.leafy.utils

import android.content.Context
import android.net.Uri
import android.os.Environment
import java.io.File

fun copyUriToAppStorage(context: Context, sourceUri: Uri): String? {
    val input = context.contentResolver.openInputStream(sourceUri) ?: return null

    val baseDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES) ?: context.filesDir
    val dir = File(baseDir, "Leafy")
    if (!dir.exists()) dir.mkdirs()

    val outFile = File(dir, "plant_${System.currentTimeMillis()}.jpg")
    input.use { ins ->
        outFile.outputStream().use { outs ->
            ins.copyTo(outs)
        }
    }

    return Uri.fromFile(outFile).toString()
}
