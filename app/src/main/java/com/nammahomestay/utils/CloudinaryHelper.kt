package com.nammahomestay.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

object CloudinaryHelper {
    private const val CLOUD_NAME = "namma-homestay-cloud" // Replace with actual Cloudinary cloud name
    private const val UPLOAD_PRESET = "namma_unsigned_preset" // Replace with actual unsigned upload preset
    private const val UPLOAD_URL = "https://api.cloudinary.com/v1_1/`$CLOUD_NAME/image/upload"

    private val client = OkHttpClient()

    suspend fun uploadImage(imageBytes: ByteArray): String? = withContext(Dispatchers.IO) {
        try {
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("upload_preset", UPLOAD_PRESET)
                .addFormDataPart(
                    "file",
                    "upload.jpg",
                    imageBytes.toRequestBody("image/jpeg".toMediaType())
                )
                .build()

            val request = Request.Builder()
                .url(UPLOAD_URL)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                if (responseBody != null) {
                    val json = JSONObject(responseBody)
                    return@withContext json.getString("secure_url")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext null
    }
}
