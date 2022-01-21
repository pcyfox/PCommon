package com.pcommon.lib_common.utils

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.pcommon.lib_network.OKHttpUtils
import com.pcommon.lib_network.download.DownLoadCallback
import com.pcommon.lib_network.download.DownloadInfo
import com.pcommon.lib_network.download.DownloadManager
import com.pcommon.lib_network.entity.BaseRespEntity
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException

object OSSDownloadUtil {
    private const val TAG = "OSSDownloadUtil"
    fun downLoad(
        rawUrl: String,
        dir: String? = null,
        isUseCache: Boolean = true,
        liveData: MutableLiveData<DownloadData>
    ) {
        Log.d(TAG, "downLoad() called with: rawUrl = $rawUrl, dir = $dir, liveData = $liveData")
        val file: String? = null
        OKHttpUtils.get(rawUrl, object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                liveData.postValue(DownloadData(rawUrl, file))
            }

            override fun onResponse(call: Call, response: Response) {
                val requestBody = response.body?.string() ?: return
                try {
                    val resp = Gson().fromJson(requestBody, BaseRespEntity<String>().javaClass)
                    Log.d(TAG, "onResponse() called with: resp = $requestBody ")
                    if (resp.isOK() && resp.data != null) {
                        val downloadUrl = resp.data
                        val index = downloadUrl!!.indexOf("?Expires");
                        val cacheKey =
                            if (index > 0) downloadUrl.subSequence(0, index).toString() else rawUrl
                        DownloadManager.getInstance().downloadToDir(
                            downloadUrl,
                            dir,
                            cacheKey,
                            isUseCache,
                            object : DownLoadCallback() {
                                override fun onFinish(file: String?) {
                                    Log.d(TAG, "onFinish() called with: file = $file")
                                    liveData.postValue(DownloadData(rawUrl, file))
                                }

                                override fun onError(msg: String?) {
                                    super.onError(msg)
                                    liveData.postValue(DownloadData(rawUrl, file))
                                }
                            })
                    } else {
                        liveData.postValue(DownloadData(rawUrl, file))
                    }
                } catch (e: Exception) {
                    liveData.postValue(DownloadData(rawUrl, file))
                }
            }
        })
    }

    fun cancel(url: String?) {
        url?.run {
            DownloadManager.getInstance().cancelDownload(DownloadInfo.Key(this))
        }
    }
}

data class DownloadData(val inputUrl: String, val file: String?)