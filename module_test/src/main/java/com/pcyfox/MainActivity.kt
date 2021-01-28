package com.pcyfox

import androidx.lifecycle.ViewModelProvider
import com.elvishew.xlog.XLog
import com.pcommon.lib_common.base.BaseActivity
import com.pcommon.lib_common.base.BaseViewModel
import com.pcommon.lib_common.ext.toastLong
import com.pcyfox.module_test.R
import com.pcyfox.module_test.databinding.ActivityMainBinding

class MainActivity(override val layoutId: Int = R.layout.activity_main) : BaseActivity<ActivityMainBinding, BaseViewModel>() {
    override fun createViewModel(): BaseViewModel {
        return ViewModelProvider(this).get(BaseViewModel::class.java)
    }

    override fun initView() {
        super.initView()
        testLog()
    }


    private fun testLog() {
        for (i in 0..10) {
            XLog.d("log.d test log i=$i")
            XLog.i("log.i test log i=$i")
        }
    }
}