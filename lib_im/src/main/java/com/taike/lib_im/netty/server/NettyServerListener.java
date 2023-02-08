package com.taike.lib_im.netty.server;


import io.netty.channel.Channel;


public interface NettyServerListener<T> {

    public final static byte STATUS_CONNECT_SUCCESS = 1;
    public final static byte STATUS_CONNECT_CLOSED = 0;
    public final static byte STATUS_CONNECT_ERROR = -1;

    /**
     *
     * @param msg
     */
    void onMessageResponseServer(T msg, Channel channel);

    /**
     * server开启成功
     */
    void onStartServer();

    /**
     * server关闭
     */
    void onStopServer();

    /**
     * 与客户端建立连接
     *
     * @param channel
     */
    void onChannelConnect(Channel channel);

    /**
     * 与客户端断开连接
     * @param
     */
    void onChannelDisConnect(Channel channel);

}
