package com.pcommon.test

import android.util.Log
import com.elvishew.xlog.XLog
import com.pcommon.edu.R
import com.pcommon.edu.databinding.ActivityTestBinding
import com.pcommon.lib_common.base.BaseActivity
import com.pcommon.lib_common.manager.ConnectivityManagerHelper
import com.pcommon.lib_log.LogCacheManager
import com.pcommon.lib_log.printer.CloudLogPrinter

class TestActivity(override val layoutId: Int = R.layout.activity_test) :
    BaseActivity<ActivityTestBinding, TestViewModel>(TestViewModel::class.java) {
    private val TAG = "TestActivity"
    override fun initView() {
        super.initView()
        viewModel?.test()
        test()
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
}