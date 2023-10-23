package com.pcommon.lib_network.udp;

import android.util.Log;

import com.elvishew.xlog.XLog;
import com.pcommon.lib_network.BuildConfig;
import com.pcommon.lib_network.ThreadTool;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class UDPBroadcaster {
    private static final String TAG = "UDPBroadcaster";


    public static void sendBroadcast(final DatagramSocket datagramSocket, final String message, final String ip, final int port) {
        if (BuildConfig.DEBUG)
            Log.d(TAG, ":sendBroadcast() called with: message = [" + message + "], ip = [" + ip + "], port = [" + port + "]");

        ThreadTool.getTreadPool().execute(() -> {
            DatagramSocket socket = datagramSocket;
            try {
                //说明通过指定端口创建的socket失败
                if (datagramSocket == null) {
                    try {
                        //使用系统分配端口创建socket，保证消息能正常发送
                        socket = new DatagramSocket();
                    } catch (SocketException e) {
                        XLog.e(TAG + ";sendBroadcast() called with: message = [" + message + "], port = [" + port + "] create socket error:" + e.getMessage());
                        e.printStackTrace();
                        return;
                    }
                }

                InetAddress targetAddress = InetAddress.getByName(ip);
                DatagramPacket packet = new DatagramPacket(message.getBytes(), message.getBytes().length, targetAddress, port);
                try {
                    socket.send(packet);
                    XLog.d(TAG + ":sendBroadcast message:" + message);
                } catch (IOException e) {
                    XLog.e(TAG + "sendBroadcast error:" + e.getMessage());
                    e.printStackTrace();
                }
            } catch (UnknownHostException e) {
                XLog.e(TAG + "sendBroadcast error:" + e.getMessage());
                e.printStackTrace();
            }
        });
    }

}
