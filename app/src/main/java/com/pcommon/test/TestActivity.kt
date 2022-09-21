package com.pcommon.test

import android.util.Log
import com.pcommon.edu.R
import com.pcommon.edu.databinding.ActivityTestBinding
import com.pcommon.lib_common.base.BaseActivity
import com.pcommon.lib_network.udp.UDPSocketClient
import kotlinx.android.synthetic.main.activity_test.*


class TestActivity(override val layoutId: Int = R.layout.activity_test) :
    BaseActivity<ActivityTestBinding, TestViewModel>(TestViewModel::class.java) {
    private val TAG = "TestActivity"
    private var udpSocketClient: UDPSocketClient? = null


    override fun initData() {
        super.initData()
        udpSocketClient = UDPSocketClient.newInstance(9993)
        udpSocketClient?.setMsgArrivedListener { msg, ip, pot ->
            Log.d(TAG, "initData() called with: msg = $msg, ip = $ip, pot = $pot")
        }
        udpSocketClient?.startUDPSocket()
    }

    override fun initView() {

    }

    override fun initListener() {
        super.initListener()
        btnSend.setOnClickListener {
            udpSocketClient?.sendBroadcast("{\"action\":\"SET_DESK_NUMBER\",\"data\":\"\",\"delay\":0,\"deskNumber\":[],\"isShowTip\":true}")
        }
    }

}