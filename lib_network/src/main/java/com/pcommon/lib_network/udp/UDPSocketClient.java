package com.pcommon.lib_network.udp;


import android.util.Log;

import androidx.annotation.Keep;

import com.elvishew.xlog.XLog;
import com.pcommon.lib_network.BuildConfig;
import com.pcommon.lib_network.Utils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;


/**
 * Created by melo on 2017/9/20.
 */
@Keep
public class UDPSocketClient {
    private OnSocketMsgArrivedListener msgArrivedListener;
    private OnStateChangeLister onStateChangeLister;
    private static UDPSocketClient instance;
    private static final String TAG = "UDPSocketClient";
    // 单个CPU线程池大小
    private static final int BUFFER_LENGTH = 8 * 1024;
    private final byte[] receiveByte = new byte[BUFFER_LENGTH];
    private static final String BROADCAST_IP = "255.255.255.255";

    private int clientPort = 1999;
    private volatile boolean isThreadRunning = false;

    private DatagramSocket datagramSocket;


    private volatile long lastReceiveTime = 0;
    private static final long TIME_OUT = 12 * 1000;
    private static final long HEARTBEAT_MESSAGE_DURATION = 10 * 1000;
    private static final String HEARTBEAT_MSG = "HB-Hi";

    private HeartbeatTimer timer;
    private static final Object lock = new Object();

    public boolean isStarted() {
        if (datagramSocket == null || datagramSocket.isClosed() || !datagramSocket.isBound()) {
            return false;
        }
        return isThreadRunning;
    }

    private UDPSocketClient() {
        // 记录创建对象时的时间
        lastReceiveTime = System.currentTimeMillis();
    }

    private UDPSocketClient(int port) {
        this();
        Log.d(TAG, "UDPSocketClient() called with: port = [" + port + "]");
        setClientPort(port);
    }

    public static UDPSocketClient getInstance(int port) {
        UDPSocketClient client = getInstance();
        client.setClientPort(port);
        return client;
    }

    public static UDPSocketClient getInstance() {
        synchronized (lock) {
            if (instance == null) {
                instance = new UDPSocketClient();
            }
        }
        return instance;
    }


    public static UDPSocketClient newInstance(int port) {
        return new UDPSocketClient(port);
    }

    public void setClientPort(int clientPort) {
        Log.d(TAG, "setClientPort() called with: clientPort = [" + clientPort + "]");
        this.clientPort = clientPort;
    }

    public int getClientPort() {
        return clientPort;
    }

    public void startUDPSocket(int port) {
        XLog.i(TAG + ":startUDPSocket() called with: port = [" + port + "]");
        setClientPort(port);
        startUDPSocket();
    }

    public void stopUDPSocket() {
        XLog.i(TAG + ":stopUDPSocket() called");
        isThreadRunning = false;
        if (datagramSocket != null) {
            if (!isStarted()) return;
            datagramSocket.close();
            XLog.d(TAG + ";stopUDPSocket() called socket closed");
        }

        if (timer != null) {
            timer.exit();
        }
        if (onStateChangeLister != null) {
            onStateChangeLister.onStop();
        }
    }

    public void start(OnSocketMsgArrivedListener listener) {
        setMsgArrivedListener(listener);
        startUDPSocket();
    }

    public void startUDPSocket() {
        XLog.d(TAG + ",startUDPSocket() called  isThreadRunning=" + isThreadRunning);
        if (isThreadRunning) {
            stopUDPSocket();
        }
        try {
            datagramSocket = new DatagramSocket(null);
            datagramSocket.setReuseAddress(true);
            datagramSocket.bind(new InetSocketAddress(clientPort));
            XLog.i(TAG + ":startUDPSocket() ,create a new DatagramSocket bind:" + clientPort);
            startSocketThread();
        } catch (Exception e) {
            XLog.e(TAG + ":startUDPSocket() error =" + e.getMessage());
            if (datagramSocket != null) {
                datagramSocket.disconnect();
                datagramSocket.close();
            }
            if (onStateChangeLister != null) {
                onStateChangeLister.onStop();
            }
            e.printStackTrace();
        }

    }

    public OnSocketMsgArrivedListener getMsgArrivedListener() {
        return msgArrivedListener;
    }

    public void setMsgArrivedListener(OnSocketMsgArrivedListener msgArrivedListener) {
        if (msgArrivedListener == null) {
            XLog.w(TAG + "setMsgArrivedListener() called with: msgArrivedListener = [" + null + "]");
        }
        this.msgArrivedListener = msgArrivedListener;
    }

    /**
     * 开启接收数据的线程
     */
    private void startSocketThread() {
        XLog.i(TAG + ",startSocketThread() called");
        isThreadRunning = true;
        Thread clientThread = new Thread(() -> {
            XLog.i(TAG + ":clientThread start to working,port:" + clientPort);
            receiveMessage();
            isThreadRunning = false;
            XLog.e(TAG + ":clientThread work broken!");
            if (onStateChangeLister != null) {
                onStateChangeLister.onStop();
            }
        }, TAG + ":receive socket data thread");
        clientThread.start();
        XLog.d(TAG + "startSocketThread() clientThread start");
        if (onStateChangeLister != null) {
            onStateChangeLister.onStart();
        }
    }

    /**
     * 处理接受到的消息
     */
    private void receiveMessage() {
        while (isStarted() && !Thread.interrupted()) {
            try {
                DatagramPacket receivePacket = new DatagramPacket(receiveByte, BUFFER_LENGTH);
                datagramSocket.receive(receivePacket);
                lastReceiveTime = System.currentTimeMillis();
                if (receivePacket.getAddress() == null) continue;
                String strReceive = new String(receivePacket.getData(), 0, receivePacket.getLength(), StandardCharsets.UTF_8);
                if (!BuildConfig.DEBUG && HEARTBEAT_MSG.equals(strReceive)) return;
                String host = (receivePacket.getAddress() == null) ? "null" : receivePacket.getAddress().getHostAddress();
                if (msgArrivedListener != null) {
                    msgArrivedListener.onSocketMsgArrived(strReceive, host, receivePacket.getPort());
                } else {
                    XLog.e(TAG + ":receiveMessage,but msgArrivedListener is null ! ");
                }
            } catch (Exception e) {
                XLog.d("UDP数据包接收失败！线程停止 e:" + e.getMessage());
                stopUDPSocket();
            }
        }
    }


    public void clear() {
        XLog.e(TAG + ":clear() called!");
        stopUDPSocket();
        onStateChangeLister = null;
        msgArrivedListener = null;
    }


    public boolean isStaredHeartbeatTimer() {
        return timer != null && timer.isStart();
    }

    public void stopHeartbeatTimer() {
        if (timer != null) timer.exit();
        timer = null;
    }


    /**
     * 启动心跳
     */
    public void startHeartbeatTimer(long delay, long period, HeartbeatListener listener) {
        XLog.d(TAG + ",startHeartbeatTimer() called with: delay = [" + delay + "], period = [" + period + "]");
        stopHeartbeatTimer();
        sendHBtoSelf();
        timer = new HeartbeatTimer();
        timer.setOnScheduleListener(() -> {
            if (isStarted()) {
                long duration = System.currentTimeMillis() - lastReceiveTime;
                if (duration > period * 3) {//若超过两分钟都没收到我的心跳包，则认为对方不在线。
                    XLog.w(TAG + "startHeartbeatTimer(),-------心跳超时---------");
                    // 刷新时间，重新进入下一个心跳周期
                    lastReceiveTime = System.currentTimeMillis();
                    if (listener != null) listener.onTimeout(duration);
                } else if (duration >= period) {
                    sendHBtoSelf();
                }
            }
        });
        timer.startTimer(delay, period);
    }


    private void sendHBtoSelf() {
        if (datagramSocket != null) {
            String ip = getHostAddress();
            sendMessage(HEARTBEAT_MSG, ip, clientPort);
        }
    }


    public String getHostAddress() {
        return Utils.LOCALHOST4.getHostAddress();
    }

    public void sendBroadcast(final String message) {
        sendBroadcast(message, clientPort);
    }

    public void sendBroadcast(final String message, final int port) {
        UDPBroadcaster.sendBroadcast(datagramSocket, message, BROADCAST_IP, port);
    }

    public void sendMessage(final String message, final String ip, final int port) {
        UDPBroadcaster.sendBroadcast(datagramSocket, message, ip, port);
    }

    public OnStateChangeLister getOnStateChangeLister() {
        return onStateChangeLister;
    }

    public void setOnStateChangeLister(OnStateChangeLister onStateChangeLister) {
        this.onStateChangeLister = onStateChangeLister;
    }

    public interface OnSocketMsgArrivedListener {
        void onSocketMsgArrived(String msg, String ip, int pot);
    }

    public interface OnStateChangeLister {
        void onStart();

        void onStop();
    }


    @Override
    public String toString() {
        return "UDPSocketClient{" +
                "CLIENT_PORT=" + clientPort +
                ", lastReceiveTime=" + lastReceiveTime +
                ", timer=" + timer +
                '}';
    }
}
