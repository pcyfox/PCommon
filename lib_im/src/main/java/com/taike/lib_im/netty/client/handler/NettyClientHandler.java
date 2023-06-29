package com.taike.lib_im.netty.client.handler;

import android.text.TextUtils;

import com.taike.lib_im.netty.CustomHeartbeatHandler;
import com.taike.lib_im.netty.NettyProtocolBean;
import com.taike.lib_im.netty.client.listener.NettyClientListener;
import com.taike.lib_im.netty.client.status.ConnectState;

import io.netty.channel.ChannelHandlerContext;


public class NettyClientHandler extends CustomHeartbeatHandler {
    private static final String TAG = "NettyClientHandler";

    private final NettyClientListener<String> listener;
    private final String index;
    private String heartBeatData;
    private int count = 0;
    private final boolean isNeedSendPing;
    private int readerIdleCount = 0;


    public NettyClientHandler(String index, String heartBeatData, boolean isNeedSendPing, NettyClientListener<String> listener) {
        super(index, false);
        this.isNeedSendPing = isNeedSendPing;
        this.listener = listener;
        this.index = index;
        this.heartBeatData = heartBeatData;
    }

    public void reset() {
        count = 0;
        readerIdleCount = 0;
    }


    @Override
    protected void handleData(ChannelHandlerContext channelHandlerContext, NettyProtocolBean data) {
        if (listener != null) listener.onMessageResponseClient(data.getContent(), index);
    }

    @Override
    protected void handleWriterIdle(ChannelHandlerContext ctx) {
        super.handleWriterIdle(ctx);
        if (!isNeedSendPing) return;
        if (TextUtils.isEmpty(heartBeatData)) heartBeatData = "" + count++;
        sendPingMsg(ctx, heartBeatData);
    }

    @Override
    protected void handleReaderIdle(ChannelHandlerContext ctx) {
        super.handleReaderIdle(ctx);
        readerIdleCount++;
        if (readerIdleCount >= 3) {//you are out
            ctx.channel().close();
        }
    }

    /**
     * <p>客户端上线</p>
     *
     * @param ctx ChannelHandlerContext
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        if (listener != null)
            listener.onClientStatusConnectChanged(ConnectState.STATUS_CONNECT_SUCCESS, index);
    }

    /**
     * <p>客户端下线</p>
     *
     * @param ctx ChannelHandlerContext
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        if (listener != null)
            listener.onClientStatusConnectChanged(ConnectState.STATUS_CONNECT_CLOSED, index);
    }


    /**
     * @param ctx   ChannelHandlerContext
     * @param cause 异常
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (listener != null)
            listener.onClientStatusConnectChanged(ConnectState.STATUS_CONNECT_ERROR, index);
        cause.printStackTrace();
        ctx.close();
    }

}
