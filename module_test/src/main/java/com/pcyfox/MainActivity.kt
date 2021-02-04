package com.pcyfox

import android.util.Log
import androidx.lifecycle.ViewModelProvider
import com.blankj.utilcode.constant.PermissionConstants
import com.blankj.utilcode.util.PermissionUtils
import com.elvishew.xlog.XLog
import com.pcommon.lib_common.base.BaseActivity
import com.pcommon.lib_common.base.BaseViewModel
import com.pcommon.lib_common.ext.toastFail
import com.pcommon.lib_common.ext.toastOk
import com.pcommon.lib_network.udp.CheckSelfListener
import com.pcommon.lib_network.udp.UDPSocketClient
import com.pcyfox.module_test.R
import com.pcyfox.module_test.databinding.ActivityMainBinding
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit


class MainActivity(override val layoutId: Int = R.layout.activity_main) : BaseActivity<ActivityMainBinding, BaseViewModel>() {
    private val TAG = "MainActivity"
    override fun createViewModel(): BaseViewModel {
        return ViewModelProvider(this).get(BaseViewModel::class.java)
    }

    override fun onPostResume() {
        PermissionUtils.permission(PermissionConstants.STORAGE).request()
        super.onPostResume()
        //testLog()
        testUDPSocket()
    }

    private fun testLog() {
        for (i in 0..10) {
            XLog.d("log.d test log i=$i")
            XLog.i("log.i test log i=$i")
        }
    }


    private fun testUDPSocket() {
        val listener = object : CheckSelfListener() {
            override fun onCheckResult(isOK: Boolean, msg: String?) {
                Log.d(TAG, "onCheckResult() called with: isOK = $isOK, msg = $msg")
                if (isOK) {
                    toastOk("check socket OK!")
                } else {
                    toastFail(msg)
                }
            }
        }

        UDPSocketClient.getInstance().startUDPSocket()
        val d = Observable.interval(10, TimeUnit.SECONDS).subscribeOn(Schedulers.io()).subscribe {
            UDPSocketClient.getInstance().checkBySelf(listener)
        }
    }


}