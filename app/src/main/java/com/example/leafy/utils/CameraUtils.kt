package com.example.leafy.utils

import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun createImageUri(context: Context): Uri {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val baseDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES) ?: context.filesDir
    val dir = File(baseDir, "LeafyLogs")
    if (!dir.exists()) dir.mkdirs()

    val file = File(dir, "LEAFY_$timeStamp.jpg")

    return FileProvider.getUriForFile(context, "${context.packageName}.provider", file)

}
