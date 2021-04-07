package com.taike.lib_im.tcp.client.listener;


/**
 * @author Created by LittleGreens on 2019/7/30
 * <p>发送状态监听</p>
 */
public interface MessageStateListener {
     void isSendSuccss(boolean isSuccess);
}
