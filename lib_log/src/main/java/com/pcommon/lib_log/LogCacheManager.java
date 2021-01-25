package com.pcommon.lib_log;

import android.util.Log;

import com.blankj.utilcode.util.CacheDiskStaticUtils;
import com.blankj.utilcode.util.CacheDiskUtils;
import com.blankj.utilcode.util.Utils;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class LogCacheManager {
    private static final String TAG = "LogCacheManager";
    private final static String LOG_CACHE_NAME = "tk_cloud_log_cache";
    private static LogCache logCache;
    private static LogCacheManager logCacheManager = new LogCacheManager();

    private LogCacheManager() {
        CacheDiskStaticUtils.setDefaultCacheDiskUtils(CacheDiskUtils.getInstance(LOG_CACHE_NAME));
    }

    public static LogCacheManager getInstance() {
        return logCacheManager;
    }

    public void save(LogCache logCache, String key) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "save() called key:" + key);
        }
        LogCacheManager.logCache = logCache;
        CacheDiskStaticUtils.put(key, new Gson().toJson(logCache));
    }

    public synchronized boolean clear(String key) {
        logCache = null;
        boolean ret = CacheDiskStaticUtils.remove(key);
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "clear() called  key:" + key + " ret:" + ret);
        }
        return ret;
    }

    public synchronized List<LogCache> getLogCaches() {
        File logDirFile = new File(Utils.getApp().getCacheDir().getAbsoluteFile() + File.separator + LOG_CACHE_NAME);
        File[] logs = logDirFile.listFiles();
        //  Log.d(TAG, "getLogCache() called all logs;" + Arrays.toString(logs));
        List<LogCache> logCaches = new ArrayList<>();
        if (logs == null) {
            return logCaches;
        }
        for (File log : logs) {
            LogCache logCache = getLogCache(log);
            if (logCache != null) {
                logCaches.add(logCache);
            }
        }
        return logCaches;
    }


    public static LogCache getLogCache(File file) {
        BufferedReader reader = null;
        try {
            //  InputStream in = context.getAssets().open(name);
            InputStream in = new FileInputStream(file);
            reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder jsonString = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonString.append(line);
            }
            return new Gson().fromJson(jsonString.toString(), LogCache.class);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
}
