package com.vm.backgroundremove.objectremove.util

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object Utils {

    private const val RESIZE_IMAGE_NAME_NEW = "ImageResize"
    const val NAME_IMAGE = "resized_image.jpg"
    fun getRealPathFromURI(contentUri: Uri, activity: Activity): String? {
        val cursor = activity.contentResolver.query(contentUri, null, null, null, null)
        cursor?.use {
            val index = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            it.moveToFirst()
            return it.getString(index)
        }
        return null
    }

    fun scaleBitmap(bitmap: Bitmap): Bitmap {
        if (bitmap.width <= 512)
            return bitmap

        val w = bitmap.width
        val h = bitmap.height
        val aspRat = w / h.toFloat()
        val width = 512
        val height = width / aspRat
        return Bitmap.createScaledBitmap(bitmap, width, height.toInt(), false)
    }

    fun getFileFromScaledBitmap(context: Context, reducedBitmap: Bitmap, fileName: String): File? {
        // chat luong hinh anh la 100
        val quality = 100
        val byteArrayOutputStream = ByteArrayOutputStream()
        reducedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, byteArrayOutputStream)
        val bitmapData = byteArrayOutputStream.toByteArray()

        // luu anh vao trong cache
        val cacheDir = File(context.cacheDir, RESIZE_IMAGE_NAME_NEW)
        if (!cacheDir.isDirectory) {
            cacheDir.mkdirs()
        }
        val file = File(cacheDir, fileName)
        file.createNewFile()

        return try {
            val fileOutputStream = FileOutputStream(file)
            fileOutputStream.write(bitmapData)
            fileOutputStream.flush()
            fileOutputStream.close()
            file
        } catch (ex: IOException) {
            null
        }
    }
    fun getBitmapFromPath(imagePath: String): Bitmap? {
        return BitmapFactory.decodeFile(imagePath)
    }
    fun createBitmapFromPath(path: String?): Bitmap {
        // Create a BitmapFactory.Options object
        val options = BitmapFactory.Options()

        // Set the inJustDecodeBounds property to true
        options.inJustDecodeBounds = true

        // Decode the image file
        BitmapFactory.decodeFile(path, options)

        // Get the dimensions of the image
        val imageHeight = options.outHeight
        val imageWidth = options.outWidth

        // Set the inJustDecodeBounds property to false
        options.inJustDecodeBounds = false

        // Decode the image file again
        val bitmap = BitmapFactory.decodeFile(path, options)

        // Return the new bitmap
        return bitmap
    }

    fun saveBitmapToCache(context: Context, bitmap: Bitmap): File? {
        val cachePath = File(context.filesDir, "cache_images")
        if (!cachePath.exists()) {
            cachePath.mkdirs()
        }

        val file = File(cachePath, NAME_IMAGE)
        try {
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.flush()
            outputStream.close()
            return file
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
    }
}