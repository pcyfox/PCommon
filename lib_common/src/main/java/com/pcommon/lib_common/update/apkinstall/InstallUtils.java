package com.pcommon.lib_common.update.apkinstall;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class InstallUtils {



    private static final String TAG = "InstallOperationHelper";

    public static final String PACKAGE_NAME = "com.example.installtest";
    /**
     * 属性的Preference文件名
     */
    public static final String GMS_PROPERTY = "com.taike.student";
    /**
     * 缺省的String型值
     */
    public static final String DEFAULT_STRING = null;
    /**
     * Intent传参时的key值：传递应用包名
     */
    public static final String PACKAGE_NAME_KEY = "packageName";



    /**
     * 静默安装
     *
     * @param filePath 要安装应用APK的存储路径
     * @return true:成功  false:失败
     */
    public static boolean slientInstall(String filePath) {
        String cmd = "pm install -r " + filePath;
        boolean result = executePmCommand(cmd);
        return result;
    }


    /**
     * 更新设备dpi
     * @param dpi
     * @return
     */
    public static boolean updateDpi(int dpi) {
        String cmd = "wm density " + dpi;
        boolean result = executePmCommand(cmd);
        return result;
    }

    /**
     * 静默卸载
     *
     * @param packageName
     * @return
     */
    public static boolean slientUninstall(String packageName) {
        String cmd = "pm uninstall " + packageName;
        boolean result = executePmCommand(cmd);
        return result;
    }

    /**
     * 执行pm命令
     *
     * @param cmd 要执行的命令字符串组合
     * @return true:成功  false:失败
     */
    private static boolean executePmCommand(String cmd) {
        boolean result;
        Process process = null;
        DataOutputStream dos = null;
        BufferedReader successResult = null;
        BufferedReader errorResult = null;
        StringBuilder successMsg = null;
        StringBuilder errorMsg = null;

        try {
            //获取root权限
            process = Runtime.getRuntime().exec("su");
            dos = new DataOutputStream(process.getOutputStream());
            dos.write(cmd.getBytes());
            //每写入一条命令就要换行,写入"\n"即可
            dos.writeBytes("\n");
            //写入exit后离开命令执行的环境
            dos.writeBytes("exit\n");
            dos.flush();
            //执行命令
            process.waitFor();
            //获取返回结果
            successMsg = new StringBuilder();
            errorMsg = new StringBuilder();
            successResult = new BufferedReader(new InputStreamReader(process.getInputStream()));
            errorResult = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String s;
            while ((s = successResult.readLine()) != null) {
                successMsg.append(s);
            }
            while ((s = errorResult.readLine()) != null) {
                errorMsg.append(s);
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        } finally {
            try {
                if (dos != null) {
                    dos.close();
                }
                if (process != null) {
                    process.destroy();
                }
                if (successResult != null) {
                    successResult.close();
                }
                if (errorResult != null) {
                    errorResult.close();
                }
            } catch (IOException e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }
        if (null != successMsg && null != errorMsg) {
            Log.d(TAG, "--successMsg--:" + successMsg.toString() + "--errorMsg--:" + errorMsg.toString());
        }
        if (null != successMsg && successMsg.toString().toLowerCase().contains("success")) {
            result = true;
        } else {
            result = false;
        }
        return result;
    }

    /**
     * 安装应用
     *
     * @param context         上下文
     * @param installApkFiles 安装包路径列表
     */
    public static void installApk(Context context, ArrayList<File> installApkFiles) {
        Log.d(TAG, "--installApk--:");
        for (File file1 : installApkFiles) {
            System.out.println("--解压后的文件路径--path--:" + file1.getPath());
            String packageName = ApkManager.getPackageName(context, file1);

            System.out.println("--installApk--packageName--:" + packageName);
            if (null != packageName) {
                GmsProperty.setStringProperty(context, packageName, file1.getPath());
            }
            silentInstall(context, file1.getPath());
        }
    }

    /**
     * 静默安装
     *
     * @param context 上下文
     * @param apkPath 安装包路径
     */
    private static void silentInstall(Context context, String apkPath) {
        Log.d(TAG, "--开始发送广播--:");
        Intent intent = new Intent();
        intent.setAction("android.intent.action.SILENT_INSTALL");
        Bundle bundle = new Bundle();
        bundle.putString("originPath", apkPath);
        bundle.putString("installerPackageName", InstallUtils.GMS_PROPERTY);
        intent.putExtras(bundle);
        context.sendBroadcast(intent);
        Log.d(TAG, "--发送广播结束--:");
    }
}