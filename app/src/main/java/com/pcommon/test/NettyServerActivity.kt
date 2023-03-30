package com.pcommon.test

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.elvishew.xlog.XLog
import com.taike.lib_im.netty.server.NettyServerListener
import com.taike.lib_im.netty.server.NettyTcpServer
import io.netty.channel.Channel
import io.netty.handler.timeout.IdleState
import io.netty.handler.timeout.IdleStateEvent

class NettyServerActivity : AppCompatActivity() {
    private val TAG = "NettyServerActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_netty_server)
        startServer()
    }


    fun startServer() {
        NettyTcpServer.getInstance().run {
            setListener(object : NettyServerListener<String> {
                override fun onMessageResponseServer(msg: String?, channel: Channel?) {
                    Log.d(
                        TAG, "onMessageResponseServer() called with: msg = $msg, channel = $channel"
                    )
                }

                override fun onStartServer() {
                    Log.d(TAG, "onStartServer() called")
                }

                override fun onStopServer() {
                    Log.d(TAG, "onStopServer() called")
                }

                override fun onChannelConnect(channel: Channel?) {
                    Log.d(TAG, "onChannelConnect() called with: channel = $channel")
                }

                override fun onChannelDisConnect(channel: Channel?) {
                    Log.d(TAG, "onChannelDisConnect() called with: channel = $channel")
                }

                override fun onIdleEventTriggered(channel: Channel?, event: IdleStateEvent?) {
                    event?.state()?.run {
                        val state = when (this) {
                            IdleState.READER_IDLE -> "读空闲"
                            IdleState.WRITER_IDLE -> "写空闲"
                            IdleState.ALL_IDLE -> {
                                "读写空闲"
                            }
                        }
                        XLog.d(
                            TAG + ",ip:" + channel?.remoteAddress() + "channel:" + channel?.id()
                                ?.asShortText() + ",isActive:" + channel?.isActive + ",超时事件：" + state
                        )

                    }
                }
            })

            start(9527)
        }
    }

    fun onStop(view: View) {
        NettyTcpServer.getInstance().disconnect()

    }

    fun onStart(view: View) {
        startServer()
    }
}