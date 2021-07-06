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
        headers: Headers? = null,
        formDataParts: Map<String, String>? = null,
        filePath: String,
        fileName: String,
        okHttpClient: OkHttpClient? = null
    ): Observable<String?> {
        Log.d(
            TAG,
            "upload() called with: url = $url, headers = $headers, filePath = $filePath, fileName = $fileName"
        )
        val client = okHttpClient ?: OkHttpClient()
        val requestBody: RequestBody = MultipartBody.Builder()
            .setType(FORM)
            .addFormDataPart(
                "file", fileName,
                File(filePath).asRequestBody("multipart/form-data".toMediaTypeOrNull())
            ).apply {
                formDataParts?.forEach {
                    addFormDataPart(it.key, it.value)
                }
            }
            .build()

        val request: Request = Request.Builder()
            .url(url)
            .apply {
                headers?.run {
                    headers(this)
                }
            }
            .post(requestBody)
            .build()

        return Observable.create {
            try {
                val response = client.newCall(request).execute()
                if (!response.isSuccessful || response.body == null) {
                    Log.e(TAG, "upload() fail,$fileName")
                    it.onNext("")
                    return@create
                }
                val ret = response.body!!.string()
                Log.d(TAG, "upload() ret=$ret")
                it.onNext(ret)
            } catch (e: Exception) {
                e.printStackTrace()
                it.onNext(e.toString())
            }
        }
    }

    fun upload(
        url: String,
        authorization: String,
        filePath: String,
        fileName: String
    ): Observable<String?> {
        val header = Headers.Builder().add("Authorization", authorization).build()
        return upload(
            url,
            headers = header,
            formDataParts = null,
            filePath,
            fileName,
            okHttpClient = null
        )
    }
}