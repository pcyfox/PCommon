package com.taike.lib_im.netty.client;

import android.text.TextUtils;
import android.util.Log;

import com.elvishew.xlog.XLog;
import com.taike.lib_im.BuildConfig;
import com.taike.lib_im.netty.MessageType;
import com.taike.lib_im.netty.MyIdleStateHandler;
import com.taike.lib_im.netty.NettyConfig;
import com.taike.lib_im.netty.NettyUtils;
import com.taike.lib_im.netty.ProtocolDecoder;
import com.taike.lib_im.netty.client.handler.NettyClientHandler;
import com.taike.lib_im.netty.client.listener.MessageStateListener;
import com.taike.lib_im.netty.client.listener.NettyClientListener;
import com.taike.lib_im.netty.client.status.ConnectState;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * Created by littleGreens on 2018-11-10.
 * TCP 客户端
 */
public class NettyTcpClient {
    private static final String TAG = "NettyTcpClient";
    private EventLoopGroup group;
    private Channel channel;

    private volatile boolean isConnected = false;
    private volatile boolean isConnecting = false;

    private volatile boolean isAutoReconnecting = true;
    private ChannelFuture channelFuture = null;

    private String host;
    private int tcpPort;

    private final String mIndex;
    private int reConnectTimes = 0;

    private boolean isNeedSendPing = true;

    private Bootstrap bootstrap;
    private final ExecutorService threadPool;

    private final Object lock = new Object();
    /**
     * 最大重连次数
     */
    private int maxConnectTimes = NettyConfig.MAX_RECONNECT_TIMES;

    private long reconnectIntervalTime = NettyConfig.RECONNECT_INTERVAL_TIME;
    private int maxFrameLength = NettyConfig.MAX_FRAME_LENGTH;

    /**
     * 心跳间隔时间
     */
    private long heartBeatInterval = NettyConfig.CLIENT_HEART_BEAT_TIME_SECONDS;//单位秒

    /**
     * 是否发送心跳
     */
    private boolean isSendHeartBeat = true;

    /**
     * 心跳数据，可以是String类型，也可以是byte[].
     */
    private String heartBeatData;


    public void setMaxFrameLength(int maxFrameLength) {
        this.maxFrameLength = maxFrameLength;
    }

    private NettyTcpClient(String host, int tcp_port, String index) {
        this.host = host;
        this.tcpPort = tcp_port;
        this.mIndex = index;
        threadPool = Executors.newSingleThreadExecutor();
    }


    public int getReConnectTimes() {
        return maxFrameLength;
    }

    public long getReconnectIntervalTime() {
        return reconnectIntervalTime;
    }

    public String getHost() {
        return host;
    }

    public int getTcpPort() {
        return tcpPort;
    }

    public String getIndex() {
        return mIndex;
    }

    public long getHeartBeatInterval() {
        return heartBeatInterval;
    }

    public boolean isSendHeartBeat() {
        return isSendHeartBeat;
    }

    public void setAutoReconnecting(boolean autoReconnecting) {
        isAutoReconnecting = autoReconnecting;
    }

    public void setNeedSendPing(boolean needSendPing) {
        isNeedSendPing = needSendPing;
    }

    private NettyClientListener<String> listener;

    private void buildBootstrap() {
        if (bootstrap != null) {
            return;
        }
        group = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(group).option(ChannelOption.TCP_NODELAY, true);//屏蔽Nagle算法
        bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 2000).channel(NioSocketChannel.class).handler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel ch) {
                isConnecting = false;
                if (BuildConfig.DEBUG)
                    Log.d(TAG, "initChannel() called with: ch = [" + ch + "]");
                ChannelPipeline pipeline = ch.pipeline();
                //解析报文
                pipeline.addLast(new ProtocolDecoder(maxFrameLength));
                if (isSendHeartBeat) {
                    IdleStateHandler idleStateHandler = new IdleStateHandler(heartBeatInterval, heartBeatInterval, heartBeatInterval * 2, TimeUnit.SECONDS);
                    pipeline.addLast("idle", idleStateHandler);//3s未发送数据，回调userEventTriggered
                }
                NettyClientHandler nettyClientHandler = new NettyClientHandler(mIndex, heartBeatData, isNeedSendPing, new NettyClientListener<>() {
                    @Override
                    public void onMessageResponseClient(String msg, String index) {
                        if (listener != null)
                            listener.onMessageResponseClient(msg, index);
                    }

                    @Override
                    public void onClientStatusConnectChanged(ConnectState statusCode, String index) {
                        isConnected = statusCode == ConnectState.STATUS_CONNECT_SUCCESS;
                        if (!isConnected && isAutoReconnecting) reconnect();
                        if (listener != null)
                            listener.onClientStatusConnectChanged(statusCode, index);
                    }
                });
                pipeline.addLast("handler", nettyClientHandler);
            }
        });
    }

    private void connectServer() {
        if (isConnected || isConnecting) {
            return;
        }
        buildBootstrap();
        try {
            isConnecting = true;
            Log.d(TAG, "connectServer() called,isConnected=" + isConnected);
            channelFuture = bootstrap.connect(host, tcpPort).addListener((ChannelFutureListener) channelFuture1 -> {
                isConnecting = false;
                if (channelFuture1.isSuccess()) {
                    reConnectTimes = 0;
                    isConnected = true;
                    XLog.i(TAG + ",connectServer():连接成功! ip:" + host + ",port:" + tcpPort);
                    channel = channelFuture1.channel();
                    if (listener != null)
                        listener.onClientStatusConnectChanged(ConnectState.STATUS_CONNECT_SUCCESS, mIndex);
                } else {
                    XLog.w(TAG + ",connectServer():连接失败! ip:" + host + ",port:" + tcpPort);
                    isConnected = false;
                    if (listener != null)
                        listener.onClientStatusConnectChanged(ConnectState.STATUS_CONNECT_ERROR, mIndex);
                    if (isAutoReconnecting) reconnect();
                }
            }).sync();
            // Wait until the connection is closed.
            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            XLog.e(TAG + ",connectServer() fail, Exception:\n" + e);
            isConnecting = false;
            isConnected = false;
            if (isAutoReconnecting) {
                reconnect();
            } else {
                bootstrap.validate();
                if (null != channelFuture) {
                    if (channelFuture.channel() != null && channelFuture.channel().isOpen()) {
                        channelFuture.channel().close();
                    }
                    channelFuture.cancel(true);
                }
                group.shutdownGracefully();
            }

            if (listener != null) {
                listener.onClientStatusConnectChanged(ConnectState.STATUS_CONNECT_CLOSED, mIndex);
            }
        }
    }


    public void connect(String host, int port) {
        Log.d(TAG, "connect() called with: host = [" + host + "], port = [" + port + "]");
        if (TextUtils.isEmpty(host) || port < 0) return;
        this.host = host;
        this.tcpPort = port;
        connect();
    }

    public void connect() {
        Log.d(TAG, "connect() called");
        synchronized (lock) {
            if (isConnected || isConnecting) {
                return;
            }
            threadPool.submit(() -> {
                connectServer();
            });
        }
    }


    public void disconnect() {
        XLog.w(TAG, "disconnect() called!");
        isConnected = false;
        reConnectTimes = 0;
        if (group != null) {
            group.shutdownGracefully();
        }
        if (bootstrap != null) bootstrap.validate();
    }

    public void reconnect() {
        synchronized (lock) {
            if (isConnected || isConnecting || reConnectTimes >= maxConnectTimes) {
                return;
            }
            try {
                Thread.sleep(reconnectIntervalTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            XLog.w(TAG + ":reconnect(),重新连接,第%d次", ++reConnectTimes);
            connectServer();
        }
    }


    /**
     * 异步发送
     *
     * @param data     要发送的数据
     * @param listener 发送结果回调
     * @return 方法执行结果
     */
    public void sendMsgToServer(String data, final MessageStateListener listener) {
        boolean isOk = channel != null && isConnected;
        if (!isOk) {
            listener.isSendSuccss(false);
            return;
        }
        NettyUtils.writeAndFlush(data, channel, MessageType.CUSTOM_MSG).addListener((ChannelFutureListener) channelFuture -> listener.isSendSuccss(channelFuture.isSuccess()));
    }

    /**
     * 同步发送
     *
     * @param data 要发送的数据
     * @return 方法执行结果
     */
    public boolean sendMsgToServer(String data) {
        boolean isOk = channel != null && isConnected;
        if (!isOk) return false;
        return NettyUtils.writeAndFlush(data, channel, MessageType.CUSTOM_MSG).isSuccess();
    }


    public void sendMsgToServer(byte[] data, final MessageStateListener listener) {
        sendMsgToServer(new String(data), listener);
    }


    public boolean isConnected() {
        return isConnected;
    }

    public void setConnectStatus(boolean status) {
        this.isConnected = status;
    }

    public void setListener(NettyClientListener<String> listener) {
        this.listener = listener;
    }


    /**
     * 构建者，创建NettyTcpClient
     */
    public static class Builder {

        private boolean isNeedSendPing = true;
        /**
         * 最大重连次数
         */
        private int maxReConnectTimes = NettyConfig.MAX_RECONNECT_TIMES;

        /**
         * 重连间隔
         */
        private long reconnectIntervalTime = 5000;
        /**
         * 服务器地址
         */
        private String host;
        /**
         * 服务器端口
         */
        private int tcp_port;
        /**
         * 客户端标识，(因为可能存在多个连接)
         */
        private String mIndex;

        /**
         * 是否发送心跳
         */
        private boolean isSendHeartBeat = true;
        /**
         * 心跳时间间隔
         */
        private long heartBeatInterval = NettyConfig.CLIENT_HEART_BEAT_TIME_SECONDS;

        /**
         * 心跳数据，可以是String类型，也可以是byte[].
         */
        private String heartBeatData;

        private int maxFrameLength = NettyConfig.MAX_FRAME_LENGTH;

        private boolean isAutoReconnecting = true;

        private NettyClientListener<String> listener;

        public Builder() {
        }

        public Builder setNeedSendPing(boolean needSendPing) {
            isNeedSendPing = needSendPing;
            return this;
        }

        public Builder setAutoReconnecting(boolean autoReconnecting) {
            isAutoReconnecting = autoReconnecting;
            return this;
        }

        public Builder setMaxFrameLength(int maxFrameLength) {
            this.maxFrameLength = maxFrameLength;
            return this;
        }

        public Builder setMaxReconnectTimes(int maxReconnectTimes) {
            this.maxReConnectTimes = maxReconnectTimes;
            return this;
        }

        public Builder setReconnectIntervalTime(long reconnectIntervalTime) {
            this.reconnectIntervalTime = reconnectIntervalTime;
            return this;
        }


        public Builder setHost(String host) {
            this.host = host;
            return this;
        }

        public Builder setTcpPort(int tcp_port) {
            this.tcp_port = tcp_port;
            return this;
        }

        public Builder setIndex(String mIndex) {
            this.mIndex = mIndex;
            return this;
        }

        public Builder setHeartBeatInterval(long intervalTime) {
            this.heartBeatInterval = intervalTime;
            return this;
        }

        public Builder setSendHeartBeat(boolean isSendHeartBeat) {
            this.isSendHeartBeat = isSendHeartBeat;
            return this;
        }

        public Builder setHeartBeatData(String heartBeatData) {
            this.heartBeatData = heartBeatData;
            return this;
        }

        public Builder setListener(NettyClientListener<String> listener) {
            this.listener = listener;
            return this;
        }

        public NettyTcpClient build() {
            NettyTcpClient nettyTcpClient = new NettyTcpClient(host, tcp_port, mIndex);
            nettyTcpClient.reconnectIntervalTime = this.reconnectIntervalTime;
            nettyTcpClient.heartBeatInterval = this.heartBeatInterval;
            nettyTcpClient.isSendHeartBeat = this.isSendHeartBeat;
            nettyTcpClient.heartBeatData = this.heartBeatData;
            nettyTcpClient.maxFrameLength = this.maxFrameLength;
            nettyTcpClient.maxConnectTimes = this.maxReConnectTimes;
            nettyTcpClient.isAutoReconnecting = this.isAutoReconnecting;
            nettyTcpClient.listener = this.listener;
            nettyTcpClient.isNeedSendPing = this.isNeedSendPing;
            return nettyTcpClient;
        }
    }

    public Channel getChannel() {
        return channel;
    }
}
