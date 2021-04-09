package com.pcommon.test

import com.pcommon.edu.R
import com.pcommon.edu.databinding.ActivityTestBinding
import com.pcommon.lib_common.base.BaseActivity
import com.pcommon.lib_common.manager.ConnectivityManagerHelper

class TestActivity(override val layoutId: Int = R.layout.activity_test) : BaseActivity<ActivityTestBinding, TestViewModel>(TestViewModel::class.java) {
    private val TAG = "TestActivity"
    override fun initView() {
        super.initView()
        viewModel?.test()
        test()
    }

    fun test() {
        ConnectivityManagerHelper.INSTANCE.init(this)
    }
}