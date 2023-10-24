package com.pcommon.test

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.pcommon.lib_network.udp.UDPSocketClient

class TestUDPSocketActivity : AppCompatActivity() {
    private val TAG = "TestUDPSocketActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_udpsockect)
    }

    fun onSendClick(view: View) {
        UDPSocketClient.getInstance()?.run {
            sendBroadcast(
                "{\"action\":\"SET_DESK_NUMBER\",\"data\":\"\",\"delay\":0,\"deskNumber\":[],\"isShowTip\":true}",
                9978
            )
        }
    }

    fun onStopClick(view: View) {
        Log.d(TAG, "onStopClick() called with: view = $view")
        UDPSocketClient.getInstance().stopUDPSocket()
    }

    fun onStartClick(view: View) {
        startUDP()
    }

    private fun startUDP() {
        Log.d(TAG, "testUDP() called")
        UDPSocketClient.getInstance(9978).start { m, ip, port ->
            Log.d(TAG, "testUDP() called with rev: m = $m, ip = $ip, port = $port")
        }

    }

    fun onStartHBClick(view: View) {
        UDPSocketClient.getInstance()?.run {
            if (isStaredHeartbeatTimer) stopHeartbeatTimer() else {
                UDPSocketClient.getInstance().startHeartbeatTimer(200, 10_000) {
                    Log.e(TAG, "testUDP() ,on hb timeout! $it")
                }
            }
        }

    }
}