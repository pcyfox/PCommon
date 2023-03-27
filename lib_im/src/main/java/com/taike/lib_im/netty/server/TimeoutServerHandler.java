package com.taike.lib_im.netty.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;

public class TimeoutServerHandler extends ChannelInboundHandlerAdapter {

    private final NettyServerListener<String> mListener;

    public TimeoutServerHandler(NettyServerListener<String> listener) {
        this.mListener = listener;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            mListener.onIdleEventTriggered(ctx.channel(), event);
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}