package com.taike.lib_im.netty;

import com.taike.lib_im.BuildConfig;

final public class NettyConfig {


    private NettyConfig() {
    }


    public static boolean isPrintLog = BuildConfig.DEBUG;


    public static int CLIENT_HEART_BEAT_TIME_SECONDS = 6;
    public static int SERVER_IDLE_TIME_SECONDS = CLIENT_HEART_BEAT_TIME_SECONDS * 2;

    public static final int MAX_FRAME_LENGTH = 1024 * 1024;  //最大长度
    public static final int START_CODE_LEN = 5;

    public final static int RECONNECT_INTERVAL_TIME = 5000;
    public static int MAX_RECONNECT_TIMES = Integer.MAX_VALUE;

    public static final int LENGTH_FIELD_LENGTH = 4;  //长度字段所占的字节数
    public static final int LENGTH_FIELD_OFFSET = 1;  //长度偏移
    public static final int LENGTH_ADJUSTMENT = -5;
    public static final int INITIAL_BYTES_TO_STRIP = 0;

}
