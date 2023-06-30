package com.taike.lib_im.netty.server.handler;

import android.util.Log;

import com.taike.lib_im.netty.CustomHeartbeatHandler;
import com.taike.lib_im.netty.NettyConfig;
import com.taike.lib_im.netty.NettyProtocolBean;
import com.taike.lib_im.netty.server.NettyServerListener;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleStateEvent;


public class NettyServerHandler extends CustomHeartbeatHandler {
    private static final String TAG = "EchoServerHandler";
    private final NettyServerListener<String> listener;
    private int allIdleTimes = 0;

    public NettyServerHandler(NettyServerListener<String> listener, boolean isNeedSendPong) {
        super("server", isNeedSendPong);
        this.listener = listener;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (NettyConfig.isPrintLog)
            Log.e(TAG, "exceptionCaught() called with: ctx = [" + ctx + "], cause = [" + cause + "]");
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    protected void handleData(ChannelHandlerContext channelHandlerContext, NettyProtocolBean data) {
        listener.onMessageResponseServer(data.getContent(), channelHandlerContext.channel());
    }

    @Override
    protected void handleAllIdle(ChannelHandlerContext ctx) {
        super.handleAllIdle(ctx);
        if (++allIdleTimes >= 2) {
            if (NettyConfig.isPrintLog)
                Log.w(TAG, "handleAllIdle() called and start to close with: ctx = [" + ctx + "],allIdleTimes=" + allIdleTimes);
            ctx.close();
        }
    }

    /**
     * 连接成功
     *
     * @param ctx
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        if (NettyConfig.isPrintLog) Log.d(TAG, "channelActive() called with: ctx = [" + ctx + "]");
        if (listener != null) listener.onChannelConnect(ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        if (NettyConfig.isPrintLog)
            Log.e(TAG, "channelInactive() called with: ctx = [" + ctx + "]");
        if (listener != null) listener.onChannelDisConnect(ctx.channel());
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        super.userEventTriggered(ctx, evt);
        if (listener != null && evt instanceof IdleStateEvent) {
            listener.onIdleEventTriggered(ctx.channel(), (IdleStateEvent) evt);
        }
    }
}
