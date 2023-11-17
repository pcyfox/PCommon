package com.pcommon.lib_network.upload

import android.util.Log
import androidx.annotation.UiThread
import androidx.annotation.WorkerThread
import io.reactivex.Observable
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Headers
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.MultipartBody.Companion.FORM
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.File
import java.io.IOException

data class UploadResult(
    var isResponseOk: Boolean,
    var filePath: String,
    var respBody: String = "",
    var errorMsg: String = "",
    var costTime: Long = 0,
    var tag: Any? = null
)

object UploadUtils {
    private val TAG = "UploadUtils"
    var myHttpClient: OkHttpClient? = null
        get() {
            if (field == null) field = OkHttpClient();
            return field
        }

    var progressListener: ProgressListener? = null


    fun upload(
        url: String,
        reqHeaders: Headers? = null,
        formDataParts: Map<String, String>? = null,
        body: String? = null,
        filePath: String,
        fileName: String,
        filePartName: String = "file",
        tag: Any? = null,
        okHttpClient: OkHttpClient? = null
    ): Observable<UploadResult> {
        Log.d(
            TAG,
            "upload() called with: url = $url, reqHeaders = $reqHeaders, formDataParts = $formDataParts, body = $body, filePath = $filePath, fileName = $fileName, filePartName = $filePartName, tag = $tag, okHttpClient = $okHttpClient"
        )

        val startTime = System.currentTimeMillis()
        val client = okHttpClient ?: myHttpClient!!

        var requestBody: RequestBody = MultipartBody
            .Builder()
            .setType(FORM)
            .addFormDataPart(
                filePartName,
                fileName,
                File(filePath).asRequestBody("multipart/form-data".toMediaTypeOrNull())
            ).apply {
                formDataParts?.forEach {
                    addFormDataPart(it.key, it.value)
                }
                if (!body.isNullOrEmpty()) {
                    addPart(body.toRequestBody())
                }
            }.build()

        progressListener?.run {
            requestBody = ProgressRequestBody(requestBody, this, filePath, tag)
        }

        val request: Request = Request.Builder().url(url).apply {
            reqHeaders?.run {
                headers(this)
            }
        }.post(requestBody).build()


        return Observable.create {
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    it.onNext(UploadResult(false, filePath, errorMsg = e.toString(), tag = tag))
                    it.onComplete()
                }

                override fun onResponse(call: Call, response: Response) {
                    val costTime = System.currentTimeMillis() - startTime
                    val result = UploadResult(
                        false, filePath = filePath, costTime = costTime, tag = tag
                    )
                    if (!response.isSuccessful) {
                        it.onNext(result.apply {
                            errorMsg = "response is unsuccessful! code: ${response.code}"
                        })
                        it.onComplete()
                        return
                    }
                    if (response.body == null) {
                        it.onNext(result.apply { errorMsg = "response body is null!" })
                        it.onComplete()
                        return
                    }
                    it.onNext(result.apply {
                        isResponseOk = true
                        respBody = response.body!!.string()
                    })
                    it.onComplete()
                }
            })
        }
    }

    fun upload(
        url: String,
        authorization: Pair<String, String>,
        filePath: String,
        fileName: String,
        filePartName: String = "file",
        tag: Any? = null
    ): Observable<UploadResult> {
        val header = Headers.Builder().add(authorization.first, authorization.second).build()
        return upload(
            url,
            reqHeaders = header,
            formDataParts = null,
            body = null,
            filePath = filePath,
            fileName = fileName,
            filePartName = filePartName,
            tag = tag,
            okHttpClient = null
        )
    }


    fun upload(
        url: String,
        params: Map<String, String>,
        filePath: String,
        fileName: String,
        filePartName: String = "file",
        tag: Any? = null
    ): Observable<UploadResult> {
        return upload(
            url,
            formDataParts = params,
            filePath = filePath,
            fileName = fileName,
            filePartName = filePartName,
            tag = tag,
        )
    }

    fun upload(
        url: String,
        reqBody: String?,
        filePath: String,
        fileName: String,
        filePartName: String,
        tag: Any?,
    ): Observable<UploadResult> {
        return upload(
            url,
            reqHeaders = null,
            formDataParts = null,
            body = reqBody,
            filePath = filePath,
            fileName = fileName,
            filePartName = filePartName,
            tag = tag,
            okHttpClient = null
        )
    }

}