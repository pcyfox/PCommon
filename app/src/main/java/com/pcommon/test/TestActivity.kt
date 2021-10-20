package com.pcommon.test

import android.util.Log
import com.blankj.utilcode.constant.PermissionConstants
import com.blankj.utilcode.util.PermissionUtils
import com.elvishew.xlog.XLog
import com.pcommon.edu.R
import com.pcommon.edu.databinding.ActivityTestBinding
import com.pcommon.lib_common.base.BaseActivity
import com.pcommon.lib_common.config.DeskConfig
import com.pcommon.lib_common.manager.ConnectivityManagerHelper
import com.pcommon.lib_log.LogCacheManager
import com.pcommon.lib_log.printer.CloudLogPrinter

class TestActivity(override val layoutId: Int = R.layout.activity_test) :
    BaseActivity<ActivityTestBinding, TestViewModel>(TestViewModel::class.java) {
    private val TAG = "TestActivity"
    override fun initView() {
        super.initView()

        testRequestPermission()
        //  viewModel?.test()
        // test()
        //testProgressDialog()

        testLoadDeskConfig()

    }


    private fun testRequestPermission(){
        PermissionUtils.permission(PermissionConstants.STORAGE).callback(object:PermissionUtils.SingleCallback{
            override fun callback(
                isAllGranted: Boolean,
                granted: MutableList<String>,
                deniedForever: MutableList<String>,
                denied: MutableList<String>
            ) {

                testCashUtils()
            }
        }).request()
    }

    private fun testCashUtils() {
        val i = 11111 / 0;
        Log.d(TAG, "initView() called $i")
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