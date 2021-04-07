package com.taike.lib_im.tcp.server;

import android.text.TextUtils;
import android.util.Log;


import com.taike.lib_im.tcp.Constant;

import java.net.InetSocketAddress;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.CharsetUtil;


/**
 * TCP 服务端
 * 目前服务端支持连接多个客户端
 */
public class NettyTcpServer {

    private static final String TAG = "NettyTcpServer";
    private final int port = 1098;

    private static NettyTcpServer instance = null;
    private NettyServerListener<String> listener;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private volatile boolean isServerStart;
    private String packetSeparator = Constant.PACKET_SEPARATOR;//防粘包分割符
    private int maxPacketLong = 1024 * 1024 * 3;

    public void setPacketSeparator(String separator) {
        this.packetSeparator = separator;
    }

    public void setMaxPacketLong(int maxPacketLong) {
        this.maxPacketLong = maxPacketLong;
    }


    public static NettyTcpServer getInstance() {
        if (instance == null) {
            synchronized (NettyTcpServer.class) {
                if (instance == null) {
                    instance = new NettyTcpServer();
                }
            }
        }
        return instance;
    }

    private NettyTcpServer() {
    }

    public void start() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                bossGroup = new NioEventLoopGroup(1);
                workerGroup = new NioEventLoopGroup();
                try {
                    ServerBootstrap b = new ServerBootstrap();
                    b.group(bossGroup, workerGroup)
                            .channel(NioServerSocketChannel.class) // 5
                            .localAddress(new InetSocketAddress(port)) // 6
                            .childOption(ChannelOption.SO_KEEPALIVE, true)
                            .childOption(ChannelOption.SO_REUSEADDR, true)
                            .childOption(ChannelOption.TCP_NODELAY, true)
                            .childHandler(new ChannelInitializer<SocketChannel>() { // 7

                                @Override
                                public void initChannel(SocketChannel ch) throws Exception {
                                    ch.pipeline().addLast(new StringEncoder(CharsetUtil.UTF_8));
                                    if (!TextUtils.isEmpty(packetSeparator)) {
                                        ByteBuf delimiter = Unpooled.buffer();
                                        delimiter.writeBytes(packetSeparator.getBytes());
                                        ch.pipeline().addLast(new DelimiterBasedFrameDecoder(maxPacketLong, delimiter));
                                    } else {
                                        ch.pipeline().addLast(new LineBasedFrameDecoder(maxPacketLong));
                                    }
                                    ch.pipeline().addLast(new StringEncoder(CharsetUtil.UTF_8));
                                    ch.pipeline().addLast(new StringDecoder(CharsetUtil.UTF_8));
                                    ch.pipeline().addLast(new IdleStateHandler(20, 20, 30));
                                    ch.pipeline().addLast(new LengthFieldPrepender(4/*表示数据长度所占的字节数*/));
                                    if (listener != null) {
                                        ch.pipeline().addLast(new EchoServerHandler(listener));
                                        ch.pipeline().addLast(new TimeoutServerHandler(listener));
                                    }

                                }
                            });

                    // Bind and start to accept incoming connections.
                    ChannelFuture f = b.bind().sync(); // 8
                    Log.e(TAG, NettyTcpServer.class.getName() + " started and listen on " + f.channel().localAddress());
                    isServerStart = true;
                    if (listener != null) {
                        listener.onStartServer();
                    }
                    // Wait until the server socket is closed.
                    // In this example, this does not happen, but you can do that to gracefully
                    // shut down your server.
                    f.channel().closeFuture().sync(); // 9
                } catch (Exception e) {
                    Log.e(TAG, e.getLocalizedMessage());
                    e.printStackTrace();
                } finally {
                    isServerStart = false;
                    if (listener != null) {
                        listener.onStopServer();
                    }
                    workerGroup.shutdownGracefully();
                    bossGroup.shutdownGracefully();
                }
            }
        }.start();

    }

    public void disconnect() {
        workerGroup.shutdownGracefully();
        bossGroup.shutdownGracefully();
    }

    public void setListener(NettyServerListener listener) {
        this.listener = listener;
    }


    public boolean isServerStart() {
        return isServerStart;
    }


    // 异步发送消息
    public boolean sendMsgToChannel(String data, Channel channel, ChannelFutureListener listener) {
        boolean flag = channel != null && channel.isActive();
        String separator = TextUtils.isEmpty(packetSeparator) ? System.getProperty("line.separator") : packetSeparator;
        if (flag) {
            channel.writeAndFlush(data + separator).addListener(listener);
        }
        return flag;
    }

    // 同步发送消息
    public boolean sendMsgToChannel(String data, Channel channel) {
        boolean flag = channel != null && channel.isActive();
        if (flag) {
            String separator = TextUtils.isEmpty(packetSeparator) ? System.getProperty("line.separator") : packetSeparator;
            ChannelFuture channelFuture = channel.writeAndFlush(data + separator).awaitUninterruptibly();
            return channelFuture.isSuccess();
        }
        return false;
    }

}
