package com.pcommon.test

import android.util.Log
import com.df.lib_config.DeskConfig
import com.df.lib_config.DeskConfigManager
import com.pcommon.lib_common.base.BaseActivity
import com.pcommon.lib_common.ext.postDelayed
import com.pcommon.lib_common.ext.startActivityExt
import com.pcommon.lib_network.udp.UDPSocketClient
import com.pcommon.lib_vidget.widget.CustomSourceImageView
import com.pcommon.test.databinding.ActivityTestBinding
import kotlinx.android.synthetic.main.activity_test.*


class TestActivity(override val layoutId: Int = R.layout.activity_test) :
    BaseActivity<ActivityTestBinding, TestViewModel>(TestViewModel::class.java) {
    private val TAG = "TestActivity"
    private var udpSocketClient: UDPSocketClient? = null
    override var isShowNetWorkChangNotice: Boolean = true

    override fun initData() {
        super.initData()
        //DeskConfigManager.getInstance().updateData()
        testUDP()
    }

    override fun initView() {
        postDelayed(2000) {
            //btnNum.text = DeskConfig.getInstance().deskNumber
//            testCrash()
        }
        CustomSourceImageView.setDefLoadLogoDir("https://lmg.jj20.com/up/allimg/1112/112GPR626/1Q12FR626-9-1200.jpg")
    }

    override fun initListener() {
        super.initListener()
        btnSend.setOnClickListener {
            udpSocketClient?.sendBroadcast("{\"action\":\"SET_DESK_NUMBER\",\"data\":\"\",\"delay\":0,\"deskNumber\":[],\"isShowTip\":true}")
        }
        btnTestLoadFile.setOnClickListener { startActivityExt(TestLoadFileActivity::class.java) }
        btnCustomSourceImageView.setOnClickListener { startActivityExt(TestCustomSourceImageActivity::class.java) }
        btnSHowProgress.setOnClickListener { showProgress("x系统正在启动中！", true) }
    }

    private fun testCrash() {
    }

    private fun testUDP() {
        UDPSocketClient.newInstance(9993).start { m, ip, port ->
            Log.d(TAG, "testUDP() called with: m = $m, ip = $ip, port = $port")
        }
    }

}