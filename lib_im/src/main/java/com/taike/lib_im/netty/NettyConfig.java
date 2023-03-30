package com.taike.lib_im.netty;

final public class NettyConfig {
    private NettyConfig() {
    }

    public static int CLIENT_IDLE_TIME_SECONDS = 3;
    public static int SERVER_IDLE_TIME_SECONDS = CLIENT_IDLE_TIME_SECONDS * 2;

    public static final int MAX_FRAME_LENGTH = 1024 * 1024;  //最大长度

    public static final int START_CODE_LEN = 5;


    public final static int RE_CONNECT_INTERVAL_TIME = 3000;
    public static int MAX_CONNECT_TIMES = Integer.MAX_VALUE;

    public static final int LENGTH_FIELD_LENGTH = 4;  //长度字段所占的字节数
    public static final int LENGTH_FIELD_OFFSET = 1;  //长度偏移
    public static final int LENGTH_ADJUSTMENT = -5;
    public static final int INITIAL_BYTES_TO_STRIP = 0;

}
