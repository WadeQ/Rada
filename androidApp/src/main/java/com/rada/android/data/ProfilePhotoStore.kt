package com.rada.android.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class ProfilePhotoStore(private val context: Context) {

    suspend fun save(uri: Uri): String? = withContext(Dispatchers.IO) {
        runCatching {
            val resolver = context.contentResolver
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
            val opts = BitmapFactory.Options().apply {
                inSampleSize = sampleSize(minOf(bounds.outWidth, bounds.outHeight))
            }
            val decoded = resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opts) }
                ?: return@runCatching null
            val rotation = resolver.openInputStream(uri)?.use {
                when (ExifInterface(it).getAttributeInt(ExifInterface.TAG_ORIENTATION, 1)) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                    else -> 0f
                }
            } ?: 0f

            val side = minOf(decoded.width, decoded.height)
            val matrix = Matrix().apply { postRotate(rotation) }
            val square = Bitmap.createBitmap(
                decoded, (decoded.width - side) / 2, (decoded.height - side) / 2, side, side, matrix, true
            )

            val dir = File(context.filesDir, "profile").apply { mkdirs() }
            dir.listFiles()?.forEach { it.delete() }
            val out = File(dir, "photo_${System.currentTimeMillis()}.jpg")
            out.outputStream().use { square.compress(Bitmap.CompressFormat.JPEG, 90, it) }
            out.absolutePath
        }.getOrNull()
    }

    suspend fun delete() = withContext(Dispatchers.IO) {
        File(context.filesDir, "profile").listFiles()?.forEach { it.delete() }
    }

    private fun sampleSize(shortSide: Int): Int {
        var sample = 1
        while (shortSide / (sample * 2) >= TARGET_PX) sample *= 2
        return sample
    }

    private companion object {
        const val TARGET_PX = 512
    }
}
