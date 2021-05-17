package com.pcommon.lib_network.upload

import android.util.Log
import io.reactivex.Observable
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody.Companion.FORM
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.util.*

object UploadUtils {
    private val TAG = "UploadUtils"

    fun upload(
        url: String,
        headers: Headers,
        filePath: String,
        fileName: String
    ): Observable<String?> {
        Log.d(
            TAG,
            "upload() called with: url = $url, headers = $headers, filePath = $filePath, fileName = $fileName"
        )
        val client = OkHttpClient()
        val requestBody: RequestBody = MultipartBody.Builder()
            .setType(FORM)
            .addFormDataPart(
                "file", fileName,
                File(filePath).asRequestBody("multipart/form-data".toMediaTypeOrNull())
            )
            .build()
        val request: Request = Request.Builder()
            .headers(headers)
            .url(url)
            .post(requestBody)
            .build()
        return Observable.create {
            try {
                val response = client.newCall(request).execute()
                if (!response.isSuccessful || response.body == null) {
                    it.onNext("")
                    return@create
                }
                val ret = response.body!!.string()
                Log.d(TAG, "upload() ret=$ret")
                it.onNext(ret)
            } catch (e: Exception) {
                it.onNext("")
            }
        }
    }

    fun upload(
        url: String,
        authorization: String,
        filePath: String,
        fileName: String
    ): Observable<String?> {
        val headers = Headers.Builder().add("Authorization", authorization).build()
        return upload(url, headers, filePath, fileName)
    }
}