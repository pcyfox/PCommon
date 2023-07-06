package com.taike.lib_im.netty;

import static com.taike.lib_im.netty.MessageType.PING_MSG;
import static com.taike.lib_im.netty.MessageType.PONG_MSG;

import android.text.TextUtils;
import android.util.Log;

import io.netty.channel.Channel;
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
    private final boolean isNeedSendPong;
    private final boolean isNeedSendPing;
    private String heartBeatData;

    private long count;
    private long readerIdleCount = 0;
    private int maxReaderIdleCount = 3;

    public CustomHeartbeatHandler(String name, String heartBeatData, boolean isNeedSendPing, boolean isNeedSendPong) {
        if (NettyConfig.isPrintLog)
            Log.d(TAG, "CustomHeartbeatHandler() called with: name = [" + name + "], heartBeatData = [" + heartBeatData + "], isNeedSendPing = [" + isNeedSendPing + "], isNeedSendPong = [" + isNeedSendPong + "]");
        this.name = name;
        this.heartBeatData = heartBeatData;
        this.isNeedSendPong = isNeedSendPong;
        this.isNeedSendPing = isNeedSendPing;
    }

    public void reset() {
        count = 0;
        readerIdleCount = 0;
    }

    public void setMaxReaderIdleCount(int maxReaderIdleCount) {
        this.maxReaderIdleCount = maxReaderIdleCount;
    }

    public void setHeartBeatData(String heartBeatData) {
        this.heartBeatData = heartBeatData;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, NettyProtocolBean nettyProtocolBean) {
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        super.channelRead(ctx, msg);
        if (msg instanceof NettyProtocolBean) {
            NettyProtocolBean bean = (NettyProtocolBean) msg;
            onChannelRead(ctx, bean);
            MessageType type = bean.getType();
            if (NettyConfig.isPrintLog)
                Log.d(TAG, "channelRead() called with: context = [" + ctx.channel().remoteAddress() + "],bean= [" + bean + "]");
            switch (type) {
                case PING_MSG:
                    if (NettyConfig.isPrintLog)
                        Log.d(TAG, "channelRead():" + name + " <******************* get ping msg from " + ctx.channel().remoteAddress());
                    if (isNeedSendPong) sendPongMsg(ctx);
                    break;
                case PONG_MSG:
                    ctx.flush();
                    if (NettyConfig.isPrintLog)
                        Log.d(TAG, "channelRead():" + name + " <------------------- get pong msg from " + ctx.channel().remoteAddress());
                    break;
                case CUSTOM_MSG:
                    handleData(ctx, bean);
                    break;
                default:
                    Log.w(TAG, "channelRead() called with:  unknown type = [" + type + "]");
            }
        }
    }

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

            if (NettyConfig.isPrintLog)
                Log.d(TAG, "userEventTriggered() called with: ctx = [" + ctx + "], idleEvent= [" + idleEvent + "]");
        }
    }

    protected abstract void handleData(ChannelHandlerContext channelHandlerContext, NettyProtocolBean data);


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
        if (NettyConfig.isPrintLog)
            Log.d(TAG, "sendPingMsg() called with: ctx = [" + ctx + "], heartBeatData = [" + heartBeatData + "],heartbeatCount=" + heartbeatCount);
    }

    protected void sendPongMsg(ChannelHandlerContext context) {
        NettyUtils.writeAndFlush(name, context.channel(), PONG_MSG);
        heartbeatCount++;
        if (NettyConfig.isPrintLog)
            Log.d(TAG, "sendPongMsg() called with: context = [" + context + "],heartbeatCount=" + heartbeatCount);
    }


    protected void handleReaderIdle(ChannelHandlerContext ctx) {
        readerIdleCount++;
        if (readerIdleCount >= maxReaderIdleCount) {//you are out
            Channel channel = ctx.channel();
            if (channel.isOpen()) {
                channel.close();
                if (NettyConfig.isPrintLog) {
                    Log.e(TAG, "handleReaderIdle() close channel when read timeout, with: ctx = [" + ctx + "],readerIdleCount=" + readerIdleCount);
                }
            }
        }
    }

    protected void onChannelRead(ChannelHandlerContext ctx, NettyProtocolBean bean) {
        readerIdleCount = 0;
        MessageType type = bean.getType();
        if (type == MessageType.PING_MSG && isNeedSendPong) {
            sendPongMsg(ctx);
        }
    }

    protected void handleWriterIdle(ChannelHandlerContext ctx) {
        if (!isNeedSendPing) return;
        if (TextUtils.isEmpty(heartBeatData)) heartBeatData = "" + count++;
        sendPingMsg(ctx, heartBeatData);
    }

    protected void handleAllIdle(ChannelHandlerContext ctx) {

    }
}