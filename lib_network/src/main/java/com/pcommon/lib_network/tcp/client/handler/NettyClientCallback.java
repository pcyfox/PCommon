package com.pcommon.lib_network.tcp.client.handler;

/**
 * 连接状态由handler回调通知client
 */
public interface NettyClientCallback {

    void onConnect();

    void onDisconnect();

}
