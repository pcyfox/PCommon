package com.pcommon.lib_network;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadTool {
    private static ExecutorService mThreadPool;

    private static void init() {
        int cpuNumbers = Runtime.getRuntime().availableProcessors();
        final int POOL_SIZE = cpuNumbers * 2;
        mThreadPool = Executors.newFixedThreadPool(POOL_SIZE);
    }

    public static ExecutorService getTreadPool() {
        if (mThreadPool == null) {
            init();
        }
        return mThreadPool;
    }

}
