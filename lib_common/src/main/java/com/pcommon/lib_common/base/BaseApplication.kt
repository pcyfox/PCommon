package com.pcommon.lib_common.base

import android.app.Activity
import android.app.Application
import android.os.Build
import android.os.Bundle
import android.util.Log
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

@RequiresApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
abstract class BaseApplication : BaseAbstractApplication(), Application.ActivityLifecycleCallbacks {
    private val TAG = "BaseApplication"
    override fun onActivityPaused(activity: Activity?) {

    }

    override fun onActivityResumed(activity: Activity?) {

    }

    override fun onActivityStarted(activity: Activity?) {
        XLog.i("$TAG:onActivityStarted() called with: activity = $activity")

    }

    override fun onActivityDestroyed(activity: Activity?) {
        val activityList = ActivityUtils.getActivityList()
        XLog.d(TAG + "onActivityDestroyed() called activity size=" + activityList.size)
        if (activityList.isNullOrEmpty()) {
            Log.e(TAG, "onActivityDestroyed() called with: app be killed")
        }
    }

    override fun onActivitySaveInstanceState(activity: Activity?, outState: Bundle?) {

    }

    override fun onActivityStopped(activity: Activity?) {

    }

    override fun onActivityCreated(activity: Activity?, savedInstanceState: Bundle?) {

    }

    @RequiresApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(this)
    }

    @RequiresApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    override fun onTerminate() {
        super.onTerminate()
        Log.e(TAG, "onTerminate() called")
        unregisterActivityLifecycleCallbacks(this)
    }


}
