package com.taike.lib_im.netty;

public enum MessageType {
    PING_MSG(1),
    PONG_MSG(2),
    CUSTOM_MSG(3);

    public final int value;

    MessageType(int flag) {
        this.value = flag;
    }

}
