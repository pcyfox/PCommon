package com.pcommon.lib_network.udp;


import android.util.Log;

import androidx.annotation.Keep;

import com.elvishew.xlog.XLog;
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

    private int CLIENT_PORT = 1999;
    private volatile boolean isThreadRunning = false;

    private DatagramSocket datagramSocket;
    private DatagramPacket receivePacket;

    private long lastReceiveTime = 0;
    private static final long TIME_OUT = 12 * 1000;
    private static final long HEARTBEAT_MESSAGE_DURATION = 10 * 1000;
    private HeartbeatTimer timer;
    private static final Object lock = new Object();


    public boolean isStarted() {
        if (datagramSocket == null || datagramSocket.isClosed()) {
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
        CLIENT_PORT = port;
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
        CLIENT_PORT = clientPort;
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
            //datagramSocket.disconnect();
            if (!datagramSocket.isClosed())
                datagramSocket.close();
            XLog.d(TAG + ";stopUDPSocket() called socket closed");
            datagramSocket = null;
        }
        isThreadRunning = false;
        receivePacket = null;
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
        XLog.d(TAG + ":startUDPSocket() called  isThreadRunning=" + isThreadRunning);
        if (isThreadRunning) {
            stopUDPSocket();
        }
        try {
            // 创建接受数据的 packet
            receivePacket = new DatagramPacket(receiveByte, BUFFER_LENGTH);
            datagramSocket = new DatagramSocket(null);
            XLog.i(TAG + ":startUDPSocket()  create a new  DatagramSocket ");
            datagramSocket.setReuseAddress(true);
            datagramSocket.bind(new InetSocketAddress(CLIENT_PORT));
            XLog.i(TAG + ":startUDPSocket()   DatagramSocket  bind :" + CLIENT_PORT);
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
     * 开启发送数据的线程
     */
    private void startSocketThread() {
        XLog.i(TAG + ":startSocketThread() called");
        isThreadRunning = true;
        Thread clientThread = new Thread(() -> {
            XLog.i(TAG + ":clientThread start to working,port:" + CLIENT_PORT);
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
        Log.d(TAG, "receiveMessage() called,isThreadRunning=" + isThreadRunning);
        while (isThreadRunning && !Thread.interrupted()) {
            try {
                if (receivePacket == null || datagramSocket == null || datagramSocket.isClosed()) {
                    XLog.e(TAG + ":receiveMessage  return,because receivePacket == null || datagramSocket == null || datagramSocket.isClosed()");
                    return;
                }
                datagramSocket.receive(receivePacket);
                lastReceiveTime = System.currentTimeMillis();
            } catch (IOException e) {
                XLog.d("UDP数据包接收失败！线程停止 e:" + e.getMessage());
                stopUDPSocket();
                e.printStackTrace();
                return;
            }
            if (receivePacket == null || receivePacket.getLength() == 0 || receivePacket.getAddress() == null) {
                XLog.d("无法接收UDP数据或者接收到的UDP数据为空");
                continue;
            }
            try {
                String host = (receivePacket.getAddress() == null) ? "null" : receivePacket.getAddress().getHostAddress();
                String strReceive = new String(receivePacket.getData(), 0, receivePacket.getLength(), StandardCharsets.UTF_8);
                if (msgArrivedListener != null) {
                    msgArrivedListener.onSocketMsgArrived(strReceive, host, receivePacket.getPort());
                } else {
                    XLog.e(TAG + ":receiveMessage,but msgArrivedListener is null ! ");
                }
                // 每次接收完UDP数据后，重置长度。否则可能会导致下次收到数据包被截断。
                if (receivePacket != null) {
                    receivePacket.setLength(BUFFER_LENGTH);
                    //XLog.d(TAG + ",接收到广播数据:\n form:" + host + ":" + receivePacket.getPort() + "\n content:\n" + strReceive);
                }
            } catch (Exception e) {
                XLog.e(TAG + ",receiveMessage() error:" + e.getMessage());
                e.printStackTrace();
            }
        }
    }


    public void clear() {
        XLog.e(TAG + ":clear() called!");
        stopUDPSocket();
        onStateChangeLister = null;
        msgArrivedListener = null;
    }


    /**
     * 启动心跳，timer 间隔十秒
     */
    private void startHeartbeatTimer() {
        if (timer != null) {
            timer.exit();
        }
        timer = new HeartbeatTimer();
        timer.setOnScheduleListener(() -> {
            XLog.d("timer is onSchedule...");
            long duration = System.currentTimeMillis() - lastReceiveTime;
            XLog.d("duration:" + duration);
            if (duration > TIME_OUT) {//若超过两分钟都没收到我的心跳包，则认为对方不在线。
                XLog.d("超时，对方已经下线");
                // 刷新时间，重新进入下一个心跳周期
                lastReceiveTime = System.currentTimeMillis();
            } else if (duration > HEARTBEAT_MESSAGE_DURATION) {//若超过十秒他没收到我的心跳包，则重新发一个。
                String string = "hello,this is a heartbeat message";
                sendBroadcast(string);
            }
        });
        timer.startTimer(0, 1000 * 10);
    }


    public String getHostAddress() {
        return Utils.LOCALHOST4.getHostAddress();
    }

    public void sendBroadcast(final String message) {
        sendBroadcast(message, CLIENT_PORT);
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
                "CLIENT_PORT=" + CLIENT_PORT +
                ", lastReceiveTime=" + lastReceiveTime +
                ", timer=" + timer +
                '}';
    }
}
