package com.pcommon.lib_network.tcp.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;

public class TimeoutServerHandler extends ChannelInboundHandlerAdapter {

    private static final String TAG = "TimeoutServerHandler";
    private final NettyServerListener<String> mListener;

    public TimeoutServerHandler(NettyServerListener<String> listener) {
        this.mListener = listener;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            String eventType = null;
            switch (event.state()) {
                case READER_IDLE:
                    eventType = "读空闲";
                    mListener.onChannelDisConnect(ctx.channel());//读空闲才判断超时
                    break;
                case WRITER_IDLE:
                    eventType = "写空闲";
                    break;
                case ALL_IDLE:
                    eventType = "读写空闲";
                    break;
            }
            //XLog.d(TAG + "  " + ctx.channel().remoteAddress() + " 超时事件：" + eventType);
            //ctx.channel().close();
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}