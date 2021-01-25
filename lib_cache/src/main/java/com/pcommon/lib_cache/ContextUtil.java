package com.pcommon.lib_cache;

import android.annotation.SuppressLint;
import android.content.Context;

import java.lang.reflect.Method;

public class ContextUtil {


    /**
     * 取得Context对象
     * PS:必须在主线程调用
     *
     * @return Context
     */
    public static Context getContext() {
        Context CONTEXT_INSTANCE = null;
        try {
            @SuppressLint("PrivateApi")
            Class<?> ActivityThread = Class.forName("android.app.ActivityThread");
            Method method = ActivityThread.getMethod("currentActivityThread");
            Object currentActivityThread = method.invoke(ActivityThread);//获取currentActivityThread 对象
            Method method2 = currentActivityThread.getClass().getMethod("getApplication");
            CONTEXT_INSTANCE = (Context) method2.invoke(currentActivityThread);//获取 Context对象
        } catch (Exception e) {
            e.printStackTrace();
        }
        return CONTEXT_INSTANCE;
    }
}
