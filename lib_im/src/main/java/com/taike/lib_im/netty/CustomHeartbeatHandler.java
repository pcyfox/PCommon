package com.taike.lib_im.netty;

import static com.taike.lib_im.netty.MessageType.PING_MSG;
import static com.taike.lib_im.netty.MessageType.PONG_MSG;

import android.text.TextUtils;
import android.util.Log;

import com.taike.lib_im.BuildConfig;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * @version 1.0
 */
public abstract class CustomHeartbeatHandler extends SimpleChannelInboundHandler<NettyProtocolBean> {
    private static final String TAG = "CustomHeartbeatHandler";
    protected String name;
    private int heartbeatCount = 0;
    private boolean isNeedSendPong = false;

    public CustomHeartbeatHandler(String name, boolean isNeedSendPong) {
        this.name = name;
        this.isNeedSendPong = isNeedSendPong;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, NettyProtocolBean nettyProtocolBean) throws Exception {
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        super.channelRead(ctx, msg);
        if (msg instanceof NettyProtocolBean) {
            NettyProtocolBean bean = (NettyProtocolBean) msg;
            MessageType type = bean.getType();
            if (BuildConfig.DEBUG)
                Log.d(TAG, "channelRead() called with: context = [" + ctx.channel().remoteAddress() + "],bean= [" + bean + "]");
            switch (type) {
                case PING_MSG:
                    if (isNeedSendPong) sendPongMsg(ctx);
                    break;
                case PONG_MSG:
                    ctx.flush();
                    if (BuildConfig.DEBUG)
                        Log.d(TAG, "channelRead():" + name + "-------------------> get pong msg from " + ctx.channel().remoteAddress());
                    break;
                case CUSTOM_MSG:
                    handleData(ctx, bean);
                    break;
                default:
                    Log.w(TAG, "channelRead() called with:  unknown type = [" + type + "]");
            }
        }
    }


    protected void sendPingMsg(ChannelHandlerContext context) {
        sendPingMsg(context, name + heartbeatCount);
    }

    public void sendPingMsg(ChannelHandlerContext ctx, String heartBeatData) {
        if (TextUtils.isEmpty(heartBeatData)) {
            sendPingMsg(ctx);
            return;
        }
        NettyUtils.writeAndFlush(heartBeatData, ctx.channel(), PING_MSG);
        heartbeatCount++;
        if (BuildConfig.DEBUG)
            Log.d(TAG, "sendPingMsg() called with: ctx = [" + ctx + "], heartBeatData = [" + heartBeatData + "],heartbeatCount=" + heartbeatCount);
    }

    private void sendPongMsg(ChannelHandlerContext context) {
        NettyUtils.writeAndFlush(name, context.channel(), PONG_MSG);
        heartbeatCount++;
        if (BuildConfig.DEBUG)
            Log.d(TAG, "sendPongMsg() called with: context = [" + context + "],heartbeatCount=" + heartbeatCount);
    }

    protected abstract void handleData(ChannelHandlerContext channelHandlerContext, NettyProtocolBean data);

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        // IdleStateHandler 所产生的 IdleStateEvent 的处理逻辑.
        String idleEvent = "";
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent e = (IdleStateEvent) evt;
            switch (e.state()) {
                case READER_IDLE:
                    idleEvent = "读超时";
                    handleReaderIdle(ctx);
                    break;
                case WRITER_IDLE:
                    idleEvent = "写超时";
                    handleWriterIdle(ctx);
                    break;
                case ALL_IDLE:
                    idleEvent = "读、写超时";
                    handleAllIdle(ctx);
                    break;
                default:
                    break;
            }

            if (BuildConfig.DEBUG)
                Log.d(TAG, "userEventTriggered() called with: ctx = [" + ctx + "], idleEvent= [" + idleEvent + "]");
        }
    }

    protected void handleReaderIdle(ChannelHandlerContext ctx) {
    }

    protected void handleWriterIdle(ChannelHandlerContext ctx) {
    }

    protected void handleAllIdle(ChannelHandlerContext ctx) {
    }

}