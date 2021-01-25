package com.pcommon.lib_common.application;

import android.app.Application;
import android.content.ComponentCallbacks;
import android.content.Context;
import android.content.res.Configuration;

/**
 * 抽象意义上的Application,非Context子类
 */

public class AbstractApplication {
    public AbstractApplication() {
    }

    private static Application application;

    public static Application getApplication() {
        return application;
    }

    public void setApplication(BaseAbstractApplication application) {
        AbstractApplication.application = application;
    }

    public void attachBaseContext(Context base) {

    }

    public void onCreate() {
    }


    public void onTerminate() {
    }


    public void onConfigurationChanged(Configuration newConfig) {
    }


    public void onLowMemory() {

    }


    public void onTrimMemory(int level) {

    }


    public void registerComponentCallbacks(ComponentCallbacks callback) {

    }


    public void unregisterComponentCallbacks(ComponentCallbacks callback) {

    }


    public void registerActivityLifecycleCallbacks(Application.ActivityLifecycleCallbacks callback) {

    }


    public void unregisterActivityLifecycleCallbacks(Application.ActivityLifecycleCallbacks callback) {

    }


    public void registerOnProvideAssistDataListener(Application.OnProvideAssistDataListener callback) {

    }


    public void unregisterOnProvideAssistDataListener(Application.OnProvideAssistDataListener callback) {

    }
}
