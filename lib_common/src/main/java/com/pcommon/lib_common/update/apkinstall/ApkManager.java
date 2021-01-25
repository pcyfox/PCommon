package com.pcommon.lib_common.update.apkinstall;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import androidx.core.content.FileProvider;

import java.io.File;
import java.util.ArrayList;

public class ApkManager {


    private static final String TAG = "ApkManager";

    /**
     * 获取某个APK文件的包名
     *
     * @param context 上下文
     * @param file    APK文件
     * @return 返回APK文件的包名
     */
    public static String getPackageName(Context context, File file) {
        String packageName = InstallUtils.DEFAULT_STRING;
        PackageManager pm = context.getPackageManager();
        PackageInfo packageInfo = pm.getPackageArchiveInfo(file.getPath(), PackageManager.GET_ACTIVITIES);
        ApplicationInfo applicationInfo = null;
        if (null != packageInfo) {
            applicationInfo = packageInfo.applicationInfo;
            packageName = applicationInfo.packageName;
        }
        return packageName;
    }

    /**
     * 安装APK
     *
     * @param context         上下文
     * @param installApkFiles 将要安装的APK文件
     */
    public static void installApk(Context context, ArrayList<File> installApkFiles) {
        Uri contentUri;
        for (File file1 : installApkFiles) {
            System.out.println("--解压后的文件路径--path--:" + file1.getPath());
            String packageName = ApkManager.getPackageName(context, file1);

            System.out.println("--installApk--packageName--:" + packageName);
            if (null != packageName) {
                GmsProperty.setStringProperty(context, packageName, file1.getPath());
            }
            Intent intent = new Intent(Intent.ACTION_VIEW);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                Log.d(TAG, "--file1--安装包路径--:" + file1.getPath());
                contentUri = FileProvider.getUriForFile(context, "com.taike.student.fileprovider", file1);
            } else {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                contentUri = Uri.parse("file://" + file1.getPath());
            }
            intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
            context.startActivity(intent);
        }
    }

    /**
     * 跳转到开启无障碍服务的界面
     *
     * @param context
     */
    public static void setSmartInatall(Context context) {
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        context.startActivity(intent);
    }

}
