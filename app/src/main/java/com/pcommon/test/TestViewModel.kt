package com.pcommon.test

import android.app.Application
import android.util.Log
import com.blankj.utilcode.util.ToastUtils
import com.pcommon.lib_common.base.BaseViewModel

class TestViewModel(app: Application) : BaseViewModel(app) {
    private val TAG = "TestViewModel"
    fun test() {
        ToastUtils.showShort("Test View Model!")
        Log.d(TAG, "test() called")
    }

}