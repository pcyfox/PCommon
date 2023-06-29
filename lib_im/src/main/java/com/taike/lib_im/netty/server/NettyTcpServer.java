package com.taike.lib_im.netty.server;

import android.util.Log;

import com.elvishew.xlog.XLog;
import com.taike.lib_im.BuildConfig;
import com.taike.lib_im.netty.MessageType;
import com.taike.lib_im.netty.NettyConfig;
import com.taike.lib_im.netty.NettyUtils;
import com.taike.lib_im.netty.ProtocolDecoder;
import com.taike.lib_im.netty.server.handler.NettyServerHandler;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;


/**
 * TCP 服务端
 * 目前服务端支持连接多个客户端
 */
public class NettyTcpServer {
    private static final String TAG = "NettyTcpServer";
    private int port = 1098;

    private NettyServerListener<String> listener;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    private volatile boolean isServerStarted;

    private int maxFrameLength = NettyConfig.MAX_FRAME_LENGTH;
    private int idleTimeSeconds = NettyConfig.SERVER_IDLE_TIME_SECONDS;
    private boolean isNeedSendPong = true;

    private static final class InstanceHolder {
        private static final NettyTcpServer instance = new NettyTcpServer();
    }

    public static NettyTcpServer getInstance() {
        return InstanceHolder.instance;
    }

    private NettyTcpServer() {
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getPort() {
        return port;
    }

    public void setMaxFrameLength(int maxFrameLength) {
        this.maxFrameLength = maxFrameLength;
    }

    public void start(int port) {
        this.port = port;
        start();
    }


    public void start() {
        new Thread(this::startServer).start();
    }

    private void startServer() {
        if (bossGroup != null || isServerStarted) return;
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup(4);
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class) // 5
                .localAddress(new InetSocketAddress(port)) // 6
                .childOption(ChannelOption.SO_KEEPALIVE, true).childOption(ChannelOption.SO_REUSEADDR, true).childOption(ChannelOption.TCP_NODELAY, true).childHandler(new ChannelInitializer<SocketChannel>() { // 7
                    @Override
                    public void initChannel(SocketChannel ch) {
                        if (BuildConfig.DEBUG)
                            Log.d(TAG, "initChannel() called with: ch = [" + ch + "]");
                        ChannelPipeline pipeline = ch.pipeline();
                        //解析报文
                        pipeline.addLast(new ProtocolDecoder(maxFrameLength));
                        pipeline.addLast(new IdleStateHandler(idleTimeSeconds , idleTimeSeconds, idleTimeSeconds * 2L, TimeUnit.SECONDS));
                        pipeline.addLast(new NettyServerHandler(listener, isNeedSendPong));
                    }
                });
        try {
            // Bind and start to accept incoming connections.
            ChannelFuture future = bootstrap.bind().sync(); // 8
            XLog.i(TAG + ",started and listen on localAddress:" + future.channel().localAddress());
            isServerStarted = true;
            if (listener != null) listener.onStartServer();
            // Wait until the server socket is closed.
            // In this example, this does not happen, but you can do that to gracefully
            // shut down your server.
            future.channel().closeFuture().sync(); // 9
        } catch (Exception e) {
            XLog.e(TAG, "start server error!,Exception:" + e);
            e.printStackTrace();
        } finally {
            disconnect();
        }
    }

    public void disconnect() {
        if (workerGroup != null) workerGroup.shutdownGracefully();
        if (bossGroup != null) bossGroup.shutdownGracefully();
        isServerStarted = false;
        workerGroup = null;
        bossGroup = null;
        if (listener != null) listener.onStopServer();
    }

    public void setListener(NettyServerListener<String> listener) {
        this.listener = listener;
    }

    public void setNeedSendPong(boolean needSendPong) {
        isNeedSendPong = needSendPong;
    }

    public boolean isServerStarted() {
        return isServerStarted;
    }

    public void setIdleTimeSeconds(int idleTimeSeconds) {
        this.idleTimeSeconds = idleTimeSeconds;
    }

    // 异步发送消息
    public boolean sendMsgToChannel(String data, Channel channel, ChannelFutureListener listener) {
        boolean flag = channel != null && channel.isActive();
        if (flag) {
            NettyUtils.writeAndFlush(data, channel, MessageType.CUSTOM_MSG).addListener(listener);
        }
        return flag;
    }

    // 同步发送消息
    public boolean sendMsgToChannel(String data, Channel channel) {
        boolean flag = channel != null && channel.isActive();
        if (flag) {
            return NettyUtils.writeAndFlush(data, channel, MessageType.CUSTOM_MSG).isSuccess();
        }
        return false;
    }

}
