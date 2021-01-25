package com.pcommon.lib_utils;

import android.os.Build;
import android.view.View;
import android.view.Window;

public class CloseBarUtil {
    /**
     * 关闭底部导航条
     */
    public static void closeBar() {
        try {
            // 需要root 权限
            Build.VERSION_CODES vc = new Build.VERSION_CODES();
            Build.VERSION vr = new Build.VERSION();
            String ProcID = "79";
            if (vr.SDK_INT >= vc.ICE_CREAM_SANDWICH) {
                ProcID = "42"; // ICS AND NEWER
            }
            // 需要root 权限
            Process proc = Runtime.getRuntime().exec(
                    new String[]{
                            "su",
                            "-c",
                            "service call activity " + ProcID
                                    + " s16 com.android.systemui"}); // WAS 79
            proc.waitFor();//挂起该进程
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 显示底部导航条
     */
    public static void showBar() {
        try {
            Process proc = Runtime.getRuntime().exec(
                    new String[]{"am", "startservice", "-n",
                            "com.android.systemui/.SystemUIService"});
            proc.waitFor();
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    //    //disable为是否禁用导航栏true为禁用
//    public static void statusBarDisable(boolean disable, Context mContext) {
//
//        try {
//            int DISABLE_NAVIGATION = StatusBarManager.DISABLE_EXPAND
//                    | StatusBarManager.DISABLE_BACK
//                    | StatusBarManager.DISABLE_NOTIFICATION_ICONS
//                    | StatusBarManager.DISABLE_NOTIFICATION_ALERTS
//                    | StatusBarManager.DISABLE_NOTIFICATION_TICKER
//                    | StatusBarManager.DISABLE_SYSTEM_INFO
//                    | StatusBarManager.DISABLE_NAVIGATION
//                    | StatusBarManager.DISABLE_CLOCK;
//            int DISABLE_NONE = 0x00000000;
//            //获得ServiceManager类
//            Class<?> ServiceManager = Class.forName("android.os.ServiceManager");
//
//            //获得ServiceManager的getService方法
//            Method getService = ServiceManager.getMethod("getService", java.lang.String.class);
//
//            //调用getService获取RemoteService
//            Object oRemoteService = getService.invoke(null,"statusbar");
//
//            //获得IStatusBarService.Stub类
//            Class<?> cStub = Class.forName("com.android.internal.statusbar.IStatusBarService$Stub");
//            //获得asInterface方法
//            Method asInterface = cStub.getMethod("asInterface", android.os.IBinder.class);
//            //调用asInterface方法获取IStatusBarService对象
//            Object oIStatusBarService = asInterface.invoke(null, oRemoteService);
//            //获得disable()方法
//            Method disableMethod = oIStatusBarService.getClass().getMethod("disable",int.class, IBinder.class,String.class);
//            //调用disable()方法
//            if(disable){
//                disableMethod.invoke(oIStatusBarService,DISABLE_NAVIGATION,new Binder(),mContext.getPackageName());
//            }else{
//                disableMethod.invoke(oIStatusBarService,DISABLE_NONE,new Binder(),mContext.getPackageName());
//            }
//        }catch (Exception e) {
//            Log.e("status", e.toString(), e);
//        }
//    }
    public static void hideBottomUIMenu(Window window) {
        //隐藏虚拟按键，并且全屏
        if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) { // lower api
            View v = window.getDecorView();
            v.setSystemUiVisibility(View.GONE);
        } else if (Build.VERSION.SDK_INT >= 19) {
            //for new api versions.
            View decorView = window.getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }
}
