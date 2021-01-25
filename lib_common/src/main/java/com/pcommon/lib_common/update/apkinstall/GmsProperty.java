package com.pcommon.lib_common.update.apkinstall;

import android.content.Context;
import android.content.SharedPreferences;


public class GmsProperty {
    /**
     * 设置string型属性值
     *
     * @param context 上下文
     * @param key     属性ID
     * @param value   String 型属性值
     */
    public static void setStringProperty(Context context, String key, String value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(InstallUtils.GMS_PROPERTY, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.commit();
    }

    /**
     * 获取String型属性值
     *
     * @param context 上下文
     * @param key     属性ID
     * @return 返回String 型属性值
     */
    public static String getStringProperty(Context context, String key) {
        return context.getSharedPreferences(InstallUtils.GMS_PROPERTY, 0).getString(key, InstallUtils.DEFAULT_STRING);
    }
}
