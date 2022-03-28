package com.pcommon.lib_network.log;

public interface LogPrintInterceptor {
    boolean onLogPrint(int logLevel, String tag, String msg);
}
