package com.taike.lib_im.netty.client.handler;

import android.text.TextUtils;
import android.util.Log;

import com.elvishew.xlog.XLog;
import com.taike.lib_im.netty.client.listener.NettyClientListener;
import com.taike.lib_im.netty.client.status.ConnectState;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;


public class NettyClientHandler extends SimpleChannelInboundHandler<String> {
    private static final String TAG = "NettyClientHandler";
    private final boolean isSendheartBeat;
    private final NettyClientListener<String> listener;
    private final String index;
    private final Object heartBeatData;
    private final String packetSeparator;
    private final NettyClientCallback nettyClientCallback;

    public NettyClientHandler(NettyClientListener<String> listener, String index, boolean isSendheartBeat, Object heartBeatData, String separator, NettyClientCallback nettyClientCallback) {
        this.listener = listener;
        this.index = index;
        this.isSendheartBeat = isSendheartBeat;
        this.heartBeatData = heartBeatData;
        this.packetSeparator = TextUtils.isEmpty(separator) ? System.getProperty("line.separator") : separator;
        this.nettyClientCallback = nettyClientCallback;
    }

    /**
     * <p>设定IdleStateHandler心跳检测每x秒进行一次读检测，
     * 如果x秒内ChannelRead()方法未被调用则触发一次userEventTrigger()方法 </p>
     *
     * @param ctx ChannelHandlerContext
     * @param evt IdleStateEvent
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.WRITER_IDLE) {
                if (isSendheartBeat) {
                    if (heartBeatData == null) {
                        //发送心跳
                        ctx.channel().writeAndFlush("Heartbeat" + packetSeparator);
                    } else {
                        if (heartBeatData instanceof String) {
                            ctx.channel().writeAndFlush(heartBeatData + packetSeparator);
                        } else if (heartBeatData instanceof byte[]) {
                            ByteBuf buf = Unpooled.copiedBuffer((byte[]) heartBeatData);
                            ctx.channel().writeAndFlush(buf);
                        } else {
                            Log.e(TAG, "userEventTriggered: heartBeatData type error");
                        }
                    }
                } else {
                    Log.e(TAG, "不发送心跳");
                }
            }
        }
    }

    /**
     * <p>客户端上线</p>
     *
     * @param ctx ChannelHandlerContext
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        //    Log.e(TAG, "------->channelActive");
//        NettyTcpClient.getInstance().setConnectStatus(true);
        XLog.d("netty客户端---->channelActive");
        if (nettyClientCallback != null) {
            nettyClientCallback.onConnect();
        }
        listener.onClientStatusConnectChanged(ConnectState.STATUS_CONNECT_SUCCESS, index);
    }

    /**
     * <p>客户端下线</p>
     *
     * @param ctx ChannelHandlerContext
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        //    Log.e(TAG, "------->channelInactive");
        XLog.d("netty客户端---->channelInactive");
        if (nettyClientCallback != null) {
            nettyClientCallback.onDisconnect();
        }
        listener.onClientStatusConnectChanged(ConnectState.STATUS_CONNECT_CLOSED, index);
    }

    /**
     * 客户端收到消息
     *
     * @param channelHandlerContext ChannelHandlerContext
     * @param msg                   消息
     */
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, String msg) {
        listener.onMessageResponseClient(msg, index);
    }

    /**
     * @param ctx   ChannelHandlerContext
     * @param cause 异常
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        XLog.e("netty客户端---->exceptionCaught:" + cause.getMessage());
        listener.onClientStatusConnectChanged(ConnectState.STATUS_CONNECT_ERROR, index);
        cause.printStackTrace();
        ctx.close();
    }
}
