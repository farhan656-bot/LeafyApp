package com.example.leafy.utils

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object StorageUtils {


    fun saveBitmapToCache(context: Context, bitmap: Bitmap): Uri? {

        val imagesFolder = File(context.cacheDir, "images")
        if (!imagesFolder.exists()) imagesFolder.mkdirs()

        val file = File(imagesFolder, "cached_image_${System.currentTimeMillis()}.png")

        return try {

            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                out.flush()
            }


            FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    fun copyUriToAppStorage(context: Context, uri: Uri): String? {
        val imagesFolder = File(context.filesDir, "images")
        if (!imagesFolder.exists()) imagesFolder.mkdirs()

        val fileName = "plant_image_${System.currentTimeMillis()}.jpg"
        val destFile = File(imagesFolder, fileName)

        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(destFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }

            destFile.absolutePath
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }


    fun deleteImageFile(path: String?): Boolean {
        if (path.isNullOrBlank()) return false
        return try {
            val file = File(path)
            if (file.exists()) file.delete() else false
        } catch (e: Exception) {
            false
        }
    }
}