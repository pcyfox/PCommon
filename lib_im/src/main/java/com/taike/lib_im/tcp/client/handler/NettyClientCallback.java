package com.taike.lib_im.tcp.client.handler;

/**
 * 连接状态由handler回调通知client
 */
public interface NettyClientCallback {

    void onConnect();

    void onDisconnect();

}
