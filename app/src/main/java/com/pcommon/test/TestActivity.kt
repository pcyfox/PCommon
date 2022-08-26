package com.pcommon.test

import android.util.Log
import com.blankj.utilcode.constant.PermissionConstants
import com.blankj.utilcode.util.PermissionUtils
import com.elvishew.xlog.LogLevel
import com.elvishew.xlog.XLog
import com.pcommon.edu.R
import com.pcommon.edu.databinding.ActivityTestBinding
import com.pcommon.lib_common.base.BaseActivity
import com.pcommon.lib_common.config.DeskConfig
import com.pcommon.lib_common.manager.ConnectivityManagerHelper
import com.pcommon.lib_log.LogCacheManager
import com.pcommon.lib_log.printer.CloudLogPrinter
import com.pcommon.lib_network.OKHttpUtils
import com.pcommon.lib_network.RequestManager
import com.pcommon.lib_vidget.widget.CustomSourceImageView
import kotlinx.android.synthetic.main.activity_test.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.File
import java.io.IOException

class TestActivity(override val layoutId: Int = R.layout.activity_test) :
    BaseActivity<ActivityTestBinding, TestViewModel>(TestViewModel::class.java) {
    private val TAG = "TestActivity"
    override fun initView() {
        super.initView()
        //  viewModel?.test()
        // test()
        //testProgressDialog()
//        testLoadDeskConfig()
        //  testHttp()
        //    ivCs.setDefDir("https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fimg.jj20.com%2Fup%2Fallimg%2F1113%2F052420110515%2F200524110515-2-1200.jpg&refer=http%3A%2F%2Fimg.jj20.com&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=auto?sec=1653634832&t=6a8b202fda06aed794cd48653159f674")


        PermissionUtils.permission(PermissionConstants.STORAGE)
            .callback { isAllGranted, granted, deniedForever, denied ->
                Log.d(
                    TAG,
                    "permission() called with: isAllGranted = $isAllGranted, granted = $granted, deniedForever = $deniedForever, denied = $denied"
                )

                CustomSourceImageView.getDefLoadLogoDir()?.run {
                    val ret = File(this).mkdirs()
                    Log.d(TAG, "create DefLoadLogoDir ret:$ret")
                }

            }.request()
        //showProgress("正在搜索中，请稍等...")
        //  showProgress()

    }

    fun test() {
        ConnectivityManagerHelper.INSTANCE.init(this)
        CloudLogPrinter.getInstance().setAutoUpdateLog(true)
        val dir = LogCacheManager.getInstance().logDirFile
        Log.d(TAG, "test() called log dir =$dir")
        for (i in 0..100) {
            XLog.i("$TAG:test() called --------------$i")
        }
    }


    private fun testHttp() {
        val c = RequestManager.get().httpClient;
        OKHttpUtils.get(c, "https://my.oschina.net/fonddream/blog/5216845", object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d(TAG, "onFailure() called with: call = $call, e = $e")
            }

            override fun onResponse(call: Call, response: Response) {
                Log.d(
                    TAG,
                    "onResponse() called with: call = $call, response = ${response.body?.string()}"
                )
            }
        })

    }


    fun testProgressDialog() {
        showProgress(2000)
        hideProgress(4000)

//        Thread {
//            for (i in 1..100) {
//                runOnUiThread {
//                    showProgress("$i%")
//                }
//                Thread.sleep(100)
//            }
//
//           runOnUiThread {
//               hideProgress()
//           }
//
//        }.start()
    }

    private fun testLoadDeskConfig() {
        DeskConfig.getInstance().deskNumber = "1-1"
        Log.d(
            TAG,
            "testLoadDeskConfig ------------->deskNumber=" + DeskConfig.getInstance().deskNumber
        )

        Log.d(
            TAG,
            "testLoadDeskConfig ------------->deskNumber=D1,find raw deskNumber=" + DeskConfig.getInstance().mappingData.findDeskLineAndColumn(
                "D1"
            )
        )
    }

}