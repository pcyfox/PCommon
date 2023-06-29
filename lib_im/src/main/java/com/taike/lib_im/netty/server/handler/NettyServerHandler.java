package com.taike.lib_im.netty.server.handler;

import com.taike.lib_im.netty.CustomHeartbeatHandler;
import com.taike.lib_im.netty.NettyProtocolBean;
import com.taike.lib_im.netty.server.NettyServerListener;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;


@ChannelHandler.Sharable
public class NettyServerHandler extends CustomHeartbeatHandler {

    private static final String TAG = "EchoServerHandler";
    private final NettyServerListener<String> listener;

    public NettyServerHandler(NettyServerListener<String> listener, boolean isNeedSendPong) {
        super("server", isNeedSendPong);
        this.listener = listener;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
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
        ctx.close();
    }


    /**
     * 连接成功
     *
     * @param ctx
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        if (listener != null) listener.onChannelConnect(ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        if (listener != null) listener.onChannelDisConnect(ctx.channel());
    }

}
