package com.pcommon.application

import android.content.Context
import android.util.Log
import androidx.multidex.MultiDex
import com.blankj.utilcode.constant.PermissionConstants
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.PermissionUtils
import com.blankj.utilcode.util.Utils
import com.pcommon.AppConfig
import com.pcommon.lib_cache.DiskCacheManager
import com.pcommon.lib_common.application.CommApplication
import com.pcommon.lib_common.base.BaseApplication
import com.pcommon.lib_network.RequestManager
import com.pcommon.lib_network.download.DownloadManager
import com.pcommon.lib_utils.Util
import com.pcommon.manager.TrdServiceManager


/**
 * @author LP
 */
class AppApplication : BaseApplication() {
    private val TAG = "AppApplication"
    private val ELK_URL = "http://192.168.1.203"
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    //注册各个子模块的Application（非Context，只是一种代理而已）
    override fun initModuleApplication() {
        registerApplicationLogic(CommApplication::class.java)
        //IMMLeaks.fixFocusedViewLeak(this)
    }



    //TODO：这里面的大多初始化方法其实都可以移动到SplashActivity中执行，反正SplashActivity中有三秒时间无事可做，这样可以优化APP启动速度
    override fun onCreate() {
        super.onCreate()
        application = this
        TrdServiceManager.initAutoSize()
        Thread {
            initLog()
            Utils.init(this)
            TrdServiceManager.initLiveEventBus()
            // val qppId = if (BuildConfig.APP_TYPE == 1) "ff38bef9d2" else "2c247e5666"
            // TrdServiceManager.initBugly(this, qppId, "0", Util.genClientId(), BuildConfig.DEBUG)
            TrdServiceManager.initX5()
            TrdServiceManager.initCrashHandler(this)
            initRequestManager()
            initCache()
        }.start()
    }

    private fun initLog() {
        val ELKPort = 80
        TrdServiceManager.initLog("TK-EDU-STU", "$ELK_URL:$ELKPort")
        Log.d(TAG, "initLog() called--------------")
        if (PermissionUtils.isGranted(PermissionConstants.STORAGE)) {
            Thread {
                Thread.sleep(30 * 1000)//延迟处理，避免影响APP启动流畅度,以及重启后未连上网
                TrdServiceManager.uploadCacheLog(application)
            }.start()
        }
    }

    private fun initCache() {
        DiskCacheManager.INSTANCE().init(AppConfig.getCachePath())
    }


    private fun initRequestManager() {
        RequestManager.get().iniRetrofit(
            Util.genClientId(),
            AppConfig.getBaseUrl(),
            "" + AppUtils.getAppVersionCode(),
            AppUtils.getAppVersionName(),
            AppUtils.getAppPackageName()
        )
        RequestManager.get().setHeaderInterceptorFilter {
            //oss下载请求不能添加请求头
            it.toString().contains(".oss-cn")
        }
        RequestManager.get().addUpdateLogRequests(ELK_URL, false)
        //DownloadManager与RequestManager共享同一个okHttpClient，以优化APP性能
        val downloadManager = DownloadManager.getInstance()
        downloadManager.okHttpClient = RequestManager.get().httpClient
        downloadManager.defStoreDir = AppConfig.getDownloadStorePath()
    }

    companion object {
        lateinit var application: BaseApplication
    }
}
