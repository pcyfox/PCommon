package com.pcommon.lib_common.base

import android.app.Activity
import android.app.Application
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.Keep
import androidx.annotation.RequiresApi
import com.blankj.utilcode.util.ActivityUtils
import com.elvishew.xlog.XLog
import com.pcommon.lib_common.application.BaseAbstractApplication


/**
 * @author xiaoqqq
 * @package com.ihealthcare.doctor_app
 * @date 2018/11/26
 * @describe todo
 */

@Keep
@RequiresApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
abstract class BaseApplication : BaseAbstractApplication(), Application.ActivityLifecycleCallbacks {
    private val TAG = "BaseApplication"

    override fun onActivityDestroyed(activity: Activity) {
        val activityList = ActivityUtils.getActivityList()
        if (activityList.isNullOrEmpty()) {
            XLog.w("$TAG:onActivityDestroyed() called with: app be killed")
        }
    }


    @RequiresApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(this)
    }



    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
    }

    override fun onActivityStarted(activity: Activity) {
    }

    override fun onActivityResumed(activity: Activity) {
    }

    override fun onActivityPaused(activity: Activity) {
    }

    override fun onActivityStopped(activity: Activity) {
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
    }


    @RequiresApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    override fun onTerminate() {
        super.onTerminate()
        unregisterActivityLifecycleCallbacks(this)
    }


}
