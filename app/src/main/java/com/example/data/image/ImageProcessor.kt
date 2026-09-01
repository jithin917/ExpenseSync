package com.example.data.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.core.content.FileProvider
import android.media.ExifInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class ImageProcessor(private val context: Context) {

    private val receiptsDir: File by lazy {
        val dir = File(context.filesDir, "receipts")
        if (!dir.exists()) dir.mkdirs()
        dir
    }

    /**
     * Create a temporary file URI for camera capture
     */
    fun createTempCameraUri(): Uri {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val tempFile = File.createTempFile("TEMP_BILL_${timeStamp}_", ".jpg", context.cacheDir)
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            tempFile
        )
    }

    /**
     * Process, rotate (from EXIF), resize (max 1080px) and compress (JPEG 75%)
     * to keep receipt images crisp and under 300KB.
     */
    suspend fun processAndSaveImage(sourceUri: Uri): ProcessedImageResult = withContext(Dispatchers.IO) {
        try {
            var inputStream: InputStream? = context.contentResolver.openInputStream(sourceUri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (originalBitmap == null) {
                return@withContext ProcessedImageResult.Error("Could not decode image")
            }

            // Handle orientation
            val orientation = getOrientation(sourceUri)
            val orientedBitmap = rotateBitmap(originalBitmap, orientation)

            // Resize if greater than 1080px
            val maxDimension = 1080
            val scaledBitmap = if (orientedBitmap.width > maxDimension || orientedBitmap.height > maxDimension) {
                val ratio = minOf(
                    maxDimension.toFloat() / orientedBitmap.width,
                    maxDimension.toFloat() / orientedBitmap.height
                )
                val width = (orientedBitmap.width * ratio).toInt()
                val height = (orientedBitmap.height * ratio).toInt()
                Bitmap.createScaledBitmap(orientedBitmap, width, height, true)
            } else {
                orientedBitmap
            }

            val fileName = "receipt_${UUID.randomUUID()}.jpg"
            val destinationFile = File(receiptsDir, fileName)

            FileOutputStream(destinationFile).use { out ->
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 75, out)
            }

            val fileSizeKb = destinationFile.length() / 1024

            ProcessedImageResult.Success(
                filePath = destinationFile.absolutePath,
                fileSizeKb = fileSizeKb,
                uri = Uri.fromFile(destinationFile)
            )
        } catch (e: Exception) {
            ProcessedImageResult.Error(e.message ?: "Unknown error while processing receipt")
        }
    }

    private fun getOrientation(uri: Uri): Int {
        return try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val exif = ExifInterface(stream)
                exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            } ?: ExifInterface.ORIENTATION_NORMAL
        } catch (e: Exception) {
            ExifInterface.ORIENTATION_NORMAL
        }
    }

    private fun rotateBitmap(bitmap: Bitmap, orientation: Int): Bitmap {
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
            else -> return bitmap
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
}

sealed class ProcessedImageResult {
    data class Success(val filePath: String, val fileSizeKb: Long, val uri: Uri) : ProcessedImageResult()
    data class Error(val message: String) : ProcessedImageResult()
}
