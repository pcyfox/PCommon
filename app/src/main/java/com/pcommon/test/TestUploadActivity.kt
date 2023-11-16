package com.pcommon.test

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.blankj.utilcode.constant.PermissionConstants
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.PermissionUtils
import com.blankj.utilcode.util.ThreadUtils
import com.pcommon.bean.RespData
import com.pcommon.bean.UploadParam
import com.pcommon.lib_network.upload.ProgressListener
import com.pcommon.lib_network.upload.UploadResult
import com.pcommon.lib_network.upload.UploadUtils
import com.pcommon.lib_utils.IPUtils
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_test_update.btnStart
import kotlinx.android.synthetic.main.activity_test_update.tvProgress
import java.io.File


class TestUploadActivity : AppCompatActivity() {
    private val TAG = "TestUploadActivity"
    private var ip = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_update)

        btnStart.setOnClickListener {
            PermissionUtils.permission(PermissionConstants.STORAGE)
                .callback { isAllGranted, granted, deniedForever, denied ->
                    if (isAllGranted) testUpload()
                }.request()
        }

    }

    private fun testUpload() {
        ip = IPUtils.getIpAddress(this)
        val testDir = File("/sdcard/DeviceLog/")
        Log.d(TAG, "testUpload() called,testDir=$testDir ")
        if (testDir.exists() && testDir.isDirectory) {
            val logs = FileUtils.listFilesInDir(testDir).filter { it.isFile }
            ThreadUtils.getSinglePool().execute {
                uploadLogs(logs, "http://192.168.100.230:15000/log/upload")
            }
        }
    }

    private fun uploadLogs(logs: List<File>, url: String) {
        Log.d(TAG, "uploadLogs() called with: logs = ${logs.size}, url = $url")
        val uploadTasks = logs.map { buildUploadTask(it, url) }
        Observable.concat(uploadTasks).subscribe(object : Observer<UploadResult> {
            override fun onSubscribe(d: Disposable) {
            }

            override fun onError(e: Throwable) {
                Log.d(TAG, "onError() called with: e = $e")
            }

            override fun onComplete() {
                Log.d(TAG, "onComplete() called")
            }

            override fun onNext(t: UploadResult) {
                Log.d(TAG, "onNext() called with: t = $t")
                if (t.isResponseOk) {
                    val data = GsonUtils.fromJson(t.respBody, RespData<Any>()::class.java)
                    if (data.isOk()) {
                        Log.d(TAG, "upload success file = ${t.filePath}")
                    }
                }

            }
        })
    }


    private fun buildUploadTask(file: File, url: String): Observable<UploadResult> {
        val param = hashMapOf<String, String>()
        param["column"] = "1"
        param["row"] = "1"
        param["deviceType"] = "1"
        param["ip"] = ip
        param["labId"] = "9527"
        param["logType"] = "1"
        param["version"] = "V1.0.0"

        UploadUtils.progressListener =
            ProgressListener { currentBytes, totalBytes, costTime, file, tag ->
                val text =
                    "file = $file\ncurrentBytes = $currentBytes\ntotalBytes = $totalBytes\ncostTime = $costTime\nprogress = ${(currentBytes / totalBytes.toFloat()) * 100}%"
                tvProgress?.post {
                    tvProgress?.text = text
                }
            }

        return UploadUtils.upload(
            url = url,
            params = param,
            filePath = file.absolutePath,
            fileName = file.name,
            filePartName = "logFile"
        )
    }
}