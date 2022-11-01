package com.pcommon.lib_utils;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Keep;

import com.blankj.utilcode.util.ActivityUtils;
import com.elvishew.xlog.LogLevel;
import com.elvishew.xlog.XLog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.Thread.UncaughtExceptionHandler;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

@Keep
public class CrashHandlerUtils implements UncaughtExceptionHandler {
    public static final int CRASH_LOG_LEVEL = LogLevel.NONE;
    private static final String TAG = "CrashHandler";
    private Context mContext;
    private static final String SDCARD_ROOT = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();
    private static final CrashHandlerUtils mInstance = new CrashHandlerUtils();
    private String path;

    private CrashHandlerUtils() {

    }

    /**
     * 单例模式，保证只有一个CustomCrashHandler实例存在
     *
     * @return
     */
    public static CrashHandlerUtils getInstance() {
        return mInstance;
    }

    /**
     * 异常发生时，系统回调的函数，我们在这里处理一些操作
     */
    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        ex.printStackTrace();
        // 将一些信息保存到SDcard中
        String f = saveInfoToSD(mContext, ex);
        // 提示用户程序即将退出
        showToast(mContext, "很抱歉，程序遭遇异常，即将退出！" + (f == null ? "-1" : ""));
        try {
            Thread.sleep(2500);
            killAppProcess();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void killAppProcess() {
        ActivityUtils.finishAllActivities();
        //注意：不能先杀掉主进程，否则逻辑代码无法继续执行，需先杀掉相关进程最后杀掉主进程
        ActivityManager mActivityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> mList = mActivityManager.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo runningAppProcessInfo : mList) {
            if (runningAppProcessInfo.pid != android.os.Process.myPid()) {
                android.os.Process.killProcess(runningAppProcessInfo.pid);
            }
        }
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }

    /**
     * 为我们的应用程序设置自定义Crash处理
     */
    public void init(Context context) {
        mContext = context;
        path = SDCARD_ROOT + File.separator + context.getPackageName() + ".crash" + File.separator;
        Log.d(TAG, "init() called with store path=:" + path);
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    /**
     * @param context
     * @param msg
     */
    private void showToast(final Context context, final String msg) {
        new Thread(() -> {
            Looper.prepare();
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
            Looper.loop();
        }).start();
    }

    /**
     * 获取一些简单的信息,软件版本，手机版本，型号等信息存放在HashMap中
     *
     * @param context
     * @return
     */
    private HashMap<String, String> obtainSimpleInfo(Context context) {
        HashMap<String, String> map = new HashMap<String, String>();
        PackageManager mPackageManager = context.getPackageManager();
        PackageInfo mPackageInfo = null;
        try {
            mPackageInfo = mPackageManager.getPackageInfo(context.getPackageName(), PackageManager.GET_ACTIVITIES);

        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        if (mPackageInfo == null) return map;
        map.put("VersionName", "" + mPackageInfo.versionName);
        map.put("VersionCode", "" + mPackageInfo.versionCode);
        map.put("CrashTime", "" + formatTime(System.currentTimeMillis()));
        map.put("MODEL", "" + Build.MODEL);
        map.put("SDK_INT", "" + Build.VERSION.SDK_INT);
        map.put("PRODUCT", "" + Build.PRODUCT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            map.put("VERSION_RELEASE", "" + Build.VERSION.RELEASE);
            map.put("VERSION_BASE_OS", "" + Build.VERSION.BASE_OS);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            map.put("SUPPORTED_ABIS ", "" + Arrays.toString(Build.SUPPORTED_ABIS));
        }
        map.put("BRAND ", "" + Build.BRAND);
        map.put("HARDWARE ", "" + Build.HARDWARE);

        return map;
    }

    /**
     * 获取系统未捕捉的错误信息
     *
     * @param throwable
     * @return
     */
    private String obtainExceptionInfo(Throwable throwable) {
        StringWriter mStringWriter = new StringWriter();
        PrintWriter mPrintWriter = new PrintWriter(mStringWriter);
        throwable.printStackTrace(mPrintWriter);
        mPrintWriter.close();
        //  Log.e(TAG, mStringWriter.toString());
        return mStringWriter.toString();
    }


    private String getCrashHeader(Context context) {
        Map<String, String> info = obtainSimpleInfo(context);
        StringBuilder stringBuilder = new StringBuilder("*************************** Crash *******************************\n");
        for (Map.Entry<String, String> header : info.entrySet()) {
            stringBuilder.append(header.getKey()).append(":").append(header.getValue()).append("\n");
        }
        stringBuilder.append("**************************************************************\n");
        return stringBuilder.toString();
    }

    public String saveInfoToSD(Context context, Throwable ex) {
        String fileName = null;
        String crashLog = getCrashHeader(context) + obtainExceptionInfo(ex) + "\n\n";
        XLog.log(CRASH_LOG_LEVEL, crashLog);
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            FileOutputStream fos = null;
            try {
                File dir = new File(path);
                if (!dir.canRead()) {
                    return "";
                }
                if (!dir.exists()) {
                    if (!dir.mkdir()) {
                        return "";
                    }
                }
                fileName = dir + File.separator + formatTime(System.currentTimeMillis()) + ".log";
                fos = new FileOutputStream(fileName);
                fos.write(crashLog.getBytes());
                fos.flush();
                fos.close();
                Log.e(TAG, "-------------------saveInfoToSD() called with: file= [" + fileName + "] ");
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return fileName;
    }

    /**
     * 将毫秒数转换成yyyy-MM-dd-HH-mm-ss
     *
     * @param milliseconds
     * @return
     */
    private String formatTime(long milliseconds) {
        System.setProperty("user.timezone", "Asia/Shanghai");
        TimeZone tz = TimeZone.getTimeZone("Asia/Shanghai");
        TimeZone.setDefault(tz);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        return format.format(new Date(milliseconds));
    }
}