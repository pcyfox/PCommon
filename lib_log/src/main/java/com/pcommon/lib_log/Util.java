package com.pcommon.lib_log;

public class Util {
    private Util() {
    }

    public static long getStringMemorySize(long length) {
        return 40 + 2 * length;
    }
}
