package com.pcommon.test

import android.util.Log
import com.pcommon.lib_common.base.BaseActivity
import com.pcommon.lib_common.ext.postDelayed
import com.pcommon.lib_common.ext.startActivityExt
import com.pcommon.lib_network.udp.UDPSocketClient
import com.pcommon.lib_utils.IPUtils
import com.pcommon.lib_vidget.widget.CustomSourceImageView
import com.pcommon.test.databinding.ActivityTestBinding
import kotlinx.android.synthetic.main.activity_test.*


class TestActivity(override val layoutId: Int = R.layout.activity_test) :
    BaseActivity<ActivityTestBinding, TestViewModel>(TestViewModel::class.java) {
    private val TAG = "TestActivity"
    override var isShowNetWorkChangNotice: Boolean = true

    override fun initData() {
        super.initData()
        //DeskConfigManager.getInstance().updateData()
    }

    override fun initView() {
        postDelayed(2000) {
            //btnNum.text = DeskConfig.getInstance().deskNumber
//            testCrash()
        }
        CustomSourceImageView.setDefLoadLogoDir("https://lmg.jj20.com/up/allimg/1112/112GPR626/1Q12FR626-9-1200.jpg")
    }

    override fun onResumeOver() {
        super.onResumeOver()
        tvIp.text = IPUtils.getIpAddress(this)
    }

    override fun initListener() {
        super.initListener()
        btnSend.setOnClickListener {
            startActivityExt(TestUDPSocketActivity::class.java)
        }
        btnTestLoadFile.setOnClickListener { startActivityExt(TestLoadFileActivity::class.java) }
        btnCustomSourceImageView.setOnClickListener { startActivityExt(TestCustomSourceImageActivity::class.java) }
        btnSHowProgress.setOnClickListener { showProgress("x系统正在启动中！", true) }
    }

    private fun testCrash() {
    }


}