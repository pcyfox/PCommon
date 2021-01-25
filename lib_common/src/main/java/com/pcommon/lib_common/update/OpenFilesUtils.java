package com.pcommon.lib_common.update;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;

import com.elvishew.xlog.XLog;

import java.io.File;


public class OpenFilesUtils {

    public static boolean installingState = false;

    /**
     * 打开安装包
     *
     * @param mContext
     * @param fileUri
     */
    public static void openAPKFile(Context mContext, String fileUri) {
        // 核心是下面几句代码
        if (null != fileUri) {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                File apkFile = new File(fileUri);
                //兼容7.0
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                    boolean hasInstallPermission = mContext.getPackageManager().canRequestPackageInstalls();
                    if (!hasInstallPermission) {
                        XLog.d("openAPKFile   2" + hasInstallPermission);
                        Toast.makeText(mContext, "Android8.0及以上版本安装应用需要打开未知来源权限，请去设置中开启权限!", Toast.LENGTH_LONG).show();
                        startInstallPermissionSettingActivity(mContext);
                        installingState = true;
                        return;
                    }
                    installApk(mContext, apkFile);
                    return;
                } else {
                    intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                }
                if (mContext.getPackageManager().queryIntentActivities(intent, 0).size() > 0) {
                    mContext.startActivity(intent);
                }
            } catch (Throwable e) {
                e.printStackTrace();
                Toast.makeText(mContext, "权限开启失败!", Toast.LENGTH_LONG).show();

            }
        }

    }

    /**
     * 跳转到设置-允许安装未知来源-页面
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void startInstallPermissionSettingActivity(Context context) {
        //注意这个是8.0新API
        Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }


    /**
     * 安装外置存储器的apk
     *
     * @param context
     * @param file
     */
    public static void installApk(Context context, File file) {
        Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //TODO Android 8.0需要增加此标志 FLAG_GRANT_READ_URI_PERMISSION，对目标应用临时授权该Uri所代表的文件
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setDataAndType(getUriForFile(context, file), "application/vnd.android.package-archive");
        context.startActivity(intent);

    }


    /**
     * 获取文件的Uri地址
     *
     * @param context
     * @param file
     * @return
     */
    public static Uri getUriForFile(Context context, File file) {
        Uri uri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            /**
             * TODO 此处需要注意Android 7.0：authority要与AndroidManifest.xml中的android.support.v4.content.FileProvider定义的authorities一致！
             */
            uri = FileProvider.getUriForFile(context.getApplicationContext(), context.getPackageName() + ".fileprovider", file);
        } else {
            uri = Uri.fromFile(file);
        }
        return uri;
    }


}
