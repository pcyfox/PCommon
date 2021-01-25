package com.pcommon.lib_utils;

import android.os.Looper;

/**
 * pcy
 */
public class ThreadCheck {
    public static boolean isMainThread() {
        return Looper.getMainLooper() == Looper.myLooper();
    }
}
