package com.pcommon.lib_common.config;

import android.text.TextUtils;

import com.blankj.utilcode.util.SPStaticUtils;
import com.pcommon.lib_common.BuildConfig;
import com.pcommon.lib_common.manager.PathManager;

public final class AppConfig {
    private AppConfig() {
    }

    public static final String REPORT_LOCATION_BY_URL = "http://whois.pconline.com.cn/ipJson.jsp";
    private static String baseUrl;
    public static final String NET_CAMERA_ACCOUNT = "admin";
    public static final String NET_CAMERA_PSD = "123456";
    public static final int NET_CAMERA_PORT = 8091;
    public static final String KEY_BASE_URL = "KEY_BASE_URL";
    public static final String ENCODE_KEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCPUd11gUSkXB1omwKcQFLRhIQzlDnMpwmLzncpfQigg476B0vTeHUKRXiSoepcfyj4V+r0he3FApu1G0QxvYuP38UUf7820gVgf+9GcJICM6DFOIhOKULsC05+wNMVgGkv4zFaKlgtYraJdVfcoboQPT+tDDRCC76ups4ifixphQIDAQAB";

    public static String getBaseUrl() {
        if (!TextUtils.isEmpty(baseUrl)) {
            return baseUrl;
        }
        String cacheUrl = SPStaticUtils.getString(KEY_BASE_URL);
        if (!TextUtils.isEmpty(cacheUrl)) {
            return cacheUrl;
        }
        if (BuildConfig.DEBUG) return BuildConfig.DEBUG_URL;
        return BuildConfig.BASE_URL;
    }

    public static void setBaseUrl(String baseUrl) {
        SPStaticUtils.put(KEY_BASE_URL, baseUrl);
        AppConfig.baseUrl = baseUrl;
    }

    public static String getDownloadBaseUrl() {
        return getBaseUrl() + "video-service/oss/downFile";
    }

    public static String getDownloadUrl(String path) {
        if (TextUtils.isEmpty(path)) {
            return "";
        }
        return getDownloadBaseUrl() + "?url=" + path;
    }

    public static String getNetCameraRTSPSubUrl(String host) {
        if (TextUtils.isEmpty(host)) {
            return "";
        }
        return "rtsp://" + NET_CAMERA_ACCOUNT + ":" + NET_CAMERA_PSD + "@" + host + "/mpeg4cif";
    }

    public static String getNetCameraRTSPMainUrl(String host) {
        if (TextUtils.isEmpty(host)) {
            return "";
        }
        return "rtsp://" + NET_CAMERA_ACCOUNT + ":" + NET_CAMERA_PSD + "@" + host + "/mpeg4";
    }

    /**
     * 下载文件统一存放地
     *
     * @return
     */
    public static String getDownloadStorePath() {
        return PathManager.get().getDownloadPath();
    }

    public static String getCachePath() {
        return PathManager.get().getCachePath();
    }

    public static String getDataRootPath() {
        return PathManager.get().getDataRootPath();
    }

    public static String getLogPath() {
        return PathManager.get().getLogPath();
    }

}
