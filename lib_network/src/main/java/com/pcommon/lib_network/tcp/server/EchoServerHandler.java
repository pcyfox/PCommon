package com.pcommon.lib_network.tcp.server;

import android.util.Log;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;


@ChannelHandler.Sharable
public class EchoServerHandler extends SimpleChannelInboundHandler<String> {

    private static final String TAG = "EchoServerHandler";
    private final NettyServerListener<String> mListener;

    public EchoServerHandler(NettyServerListener<String> listener) {
        this.mListener = listener;
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
//    	System.out.println("channelReadComplete");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx,
                                Throwable cause) {
        cause.printStackTrace();                //5
        ctx.close();
        //6
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        if (msg.equals("Heartbeat")) {
            Log.d(TAG, "Heartbeat");
            return; //客户端发送来的心跳数据
        }
        if( mListener != null){
            mListener.onMessageResponseServer(msg, ctx.channel().id().asShortText());
        }
    }

    /**
     * 连接成功
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        mListener.onChannelConnect(ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        mListener.onChannelDisConnect(ctx.channel());
    }
}
