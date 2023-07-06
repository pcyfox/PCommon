package com.pcommon.test

import android.os.Bundle
import android.util.Log
import android.view.View
import com.pcommon.lib_common.base.BaseActivity
import com.pcommon.test.databinding.ActivityNettyClientBinding
import com.taike.lib_im.netty.client.NettyTcpClient
import com.taike.lib_im.netty.client.listener.NettyClientListener
import com.taike.lib_im.netty.client.status.ConnectState
import kotlinx.android.synthetic.main.activity_netty_client.*

class NettyClientActivity(override val layoutId: Int = R.layout.activity_netty_client) :
    BaseActivity<ActivityNettyClientBinding, TestViewModel>(TestViewModel::class.java) {

    private val TAG = "NettyClientActivity"
    private lateinit var client: NettyTcpClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        client =
            NettyTcpClient.Builder().setMaxReconnectTimes(3000).setHeartBeatInterval(10)
                .setNeedSendPong(false)
                .setListener(object :
                    NettyClientListener<String> {
                    override fun onMessageResponseClient(msg: String?, index: String?) {
                        Log.d(
                            TAG,
                            "onMessageResponseClient() called with: msg = $msg, index = $index"
                        )
                    }

                    override fun onClientStatusConnectChanged(
                        state: ConnectState,
                        index: String?
                    ) {
                        Log.d(
                            TAG,
                            "onClientStatusConnectChanged() called with: state = $state, index = $index"
                        )
                        // if (state != ConnectState.STATUS_CONNECT_SUCCESS) client.connect()
                    }
                }).setHeartBeatData("test").setIndex("A1").build()
    }

    fun onStart(view: View) {
        client.connect(etHost.text.toString(), 9527)
    }

    fun onSend(view: View) {
        val text = etInput.text.toString()
        val ret = client.sendMsgToServer(text)
        Log.d(TAG, "onSend() called with: ret= $ret")
    }

    fun onStop(view: View) {
        client.disconnect()
    }
}