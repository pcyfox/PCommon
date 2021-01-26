package com.pcommon.manager;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.blankj.utilcode.util.AppUtils;
import com.jeremyliao.liveeventbus.LiveEventBus;
import com.pcommon.AppConfig;
import com.pcommon.lib_common.BuildConfig;
import com.pcommon.lib_common.config.DeskConfig;
import com.pcommon.lib_log.XLogHelper;
import com.pcommon.lib_log.printer.CloudLogPrinter;
import com.pcommon.lib_log.printer.PrintLogReq;
import com.pcommon.lib_utils.CrashHandlerUtils;
import com.pcommon.lib_utils.Util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

import me.jessyan.autosize.AutoSizeConfig;
import me.jessyan.autosize.unit.Subunits;

/**
 * 第三方库管理类
 */
public final class TrdServiceManager {
    private TrdServiceManager() {
    }

    private static final String TAG = "TrdService";

    public static void initBugly(Context appContext, String buglyAppId, String channel, String clientId, boolean isDebug) {
        Log.d(TAG, "initTxBugly() called with: appContext = [" + appContext + "], buglyAppId = [" + buglyAppId + "], channel = [" + channel + "], clientId = [" + clientId + "], isDebug = [" + isDebug + "]");
        if (TextUtils.isEmpty(buglyAppId)) {
            return;
        }

//        BuglyStrategy strategy = new BuglyStrategy()
//                .setAppPackageName(appContext.getPackageName())
//                .setAppChannel(channel)
//                .setDeviceID(clientId)
//                .setCrashHandleCallback(new BuglyStrategy.a() {
//                    @Override
//                    public synchronized Map<String, String> onCrashHandleStart(int crashType, String errorType, String errorMessage, String errorStack) {
//                        //本地崩溃记录
//                        CrashHandlerUtils utils = CrashHandlerUtils.getInstance();
//                        String sb = " errorType:" +
//                                errorType +
//                                " errorMessage:" +
//                                errorMessage +
//                                "\n errorStack" +
//                                errorStack;
//                        utils.savaInfoToSD(appContext, new Throwable(sb));
//                        return super.onCrashHandleStart(crashType, errorType, errorMessage, errorStack);
//                    }
//                });
//
//        Bugly.init(appContext, buglyAppId, isDebug, strategy);
//        //  CrashReport.testJavaCrash();
//        Bugly.setIsDevelopmentDevice(appContext, isDebug);
//        CrashReport.setIsDevelopmentDevice(appContext, isDebug);
    }


    public static void initLiveEventBus() {
        LiveEventBus.get()
                .config()
                // .supportBroadcast(this)//配置支持跨进程、跨APP通信
                //true：整个生命周期（从onCreate到onDestroy）都可以实时收到消息
                //false：激活状态（Started）可以实时收到消息，非激活状态（Stoped）无法实时收到消息，需等到Activity重新变成激活状态，方可收到消息
                .lifecycleObserverAlwaysActive(false)
                //配置在没有Observer关联的时候是否自动清除LiveEvent以释放内存
                .autoClear(true);
    }

    public static void initLog(String clientName, String ELKUrl) {
        PrintLogReq printLogReq = new PrintLogReq();
        CloudLogPrinter cloudLogPrinter = CloudLogPrinter.getInstance();
        Map<String, String> header = cloudLogPrinter.getHeader();
        header.put("display", Build.DISPLAY);
        header.put("manufacturer", Build.MANUFACTURER);
        header.put("model", Build.MODEL);

        String deviceId = DeskConfig.getInstance().getDeviceId();
        if (TextUtils.isEmpty(deviceId)) {
            deviceId = Util.genClientId();
            DeskConfig.getInstance().setDeviceId(deviceId);
        }
        String location = DeskConfig.getInstance().getLocation();
        if (!TextUtils.isEmpty(location)) {
            try {
                header.put("device_location", URLEncoder.encode(location,"utf-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        header.put("device_id", deviceId);
        header.put("app_version_code", "" + AppUtils.getAppVersionCode());
        header.put("app_version_name", "" + AppUtils.getAppVersionName());
        header.put("is_debug", "" + BuildConfig.DEBUG);
        cloudLogPrinter.init(printLogReq, ELKUrl, clientName);
        XLogHelper.initLog(cloudLogPrinter, AppConfig.getLogPath(), clientName, clientName);
    }

    public static void initCrashHandler(Context context) {
        CrashHandlerUtils.getInstance().init(context);
    }

    /**
     * 上传上次APP未上传的log
     */
    public static void uploadCacheLog() {
        Log.d(TAG, "uploadCacheLog() called");
        XLogHelper.uploadCache();
    }


    public static void iniARouter() {
//        if (BuildConfig.DEBUG) {           // These two lines must be written before init, otherwise these configurations will be invalid in the init process
//            ARouter.openLog();     // Print log
//            ARouter.openDebug();   // Turn on debugging mode (If you are running in InstantRun mode, you must turn on debug mode! Online version needs to be closed, otherwise there is a security risk)
//        }
//        ARouter.init(AppApplication.application);
    }

    public static void initAutoSize() {
        AutoSizeConfig.getInstance().getUnitsManager()
                .setSupportSubunits(Subunits.NONE)
                .setDesignWidth(1080);
    }

    public static void initX5() {
        //初始化X%内核
    }

}
