package com.pcommon.lib_network.udp;

import android.util.Log;

import androidx.annotation.Keep;

import com.elvishew.xlog.XLog;
import com.pcommon.lib_network.Utils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


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
    private static final int POOL_SIZE = 2;
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
    private final ExecutorService mThreadPool;
    private HeartbeatTimer timer;
    private static final Object lock = new Object();


    public boolean isStarted() {
        if (datagramSocket == null || datagramSocket.isClosed()) {
            return false;
        }
        return isThreadRunning;
    }

    private UDPSocketClient() {
        int cpuNumbers = Runtime.getRuntime().availableProcessors();
        // 根据CPU数目初始化线程池
        mThreadPool = Executors.newFixedThreadPool(cpuNumbers * POOL_SIZE);
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
                }

                XLog.d(TAG + "接收到广播数据 form:" + host + ":" + receivePacket.getPort() + "\ncontent:" + strReceive);
            } catch (Exception e) {
                XLog.i(TAG + ":receiveMessage() e:" + e.getMessage());
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

    public void sendBroadcast(final String message, final String ip, final int port) {
        //XLog.i(TAG + ":sendBroadcast() called with: message = [" + message + "], ip = [" + ip + "], port = [" + port + "]");
        //说明通过指定端口创建的socket失败
        if (datagramSocket == null) {
            try {
                //使用系统分配端口创建socket，保证消息能正常发送
                datagramSocket = new DatagramSocket();
            } catch (SocketException e) {
                XLog.e(TAG + ";sendBroadcast() called with: message = [" + message + "], port = [" + port + "] create socket error:" + e.getMessage());
                e.printStackTrace();
                return;
            }
        }

        mThreadPool.execute(() -> {
            try {
                InetAddress targetAddress = InetAddress.getByName(ip);
                DatagramPacket packet = new DatagramPacket(message.getBytes(), message.getBytes().length, targetAddress, port);
                try {
                    datagramSocket.send(packet);
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

    public void sendBroadcast(final String message, final int port) {
        sendBroadcast(message, BROADCAST_IP, port);
    }


    public void sendMessage(final String message, final String Ip, final int port) {
        mThreadPool.execute(() -> {
            try {
                InetAddress targetAddress = InetAddress.getByName(Ip);
                DatagramPacket packet = new DatagramPacket(message.getBytes(), message.length(), targetAddress, port);
                datagramSocket.send(packet);
                // 数据发送事件
                XLog.d(TAG + ":client send to " + Ip + ":" + port + " over, msg:\n" + message);
            } catch (IOException e) {
                e.printStackTrace();
            }

        });
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


}
