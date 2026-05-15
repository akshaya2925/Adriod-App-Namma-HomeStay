package com.nammahomestay.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.ByteArrayOutputStream

object ImageUploadHelper {
    fun compressImage(context: Context, uri: Uri): ByteArray? {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream) ?: return null
            
            val maxDimension = 800
            val width = originalBitmap.width
            val height = originalBitmap.height
            
            var newWidth = width
            var newHeight = height
            
            if (width > maxDimension || height > maxDimension) {
                if (width > height) {
                    newWidth = maxDimension
                    newHeight = (maxDimension * height) / width
                } else {
                    newHeight = maxDimension
                    newWidth = (maxDimension * width) / height
                }
            }
            
            val resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true)
            val baos = ByteArrayOutputStream()
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos)
            return baos.toByteArray()
        } catch (e: Exception) {
            return null
        }
    }
}
