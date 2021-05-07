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
    private final static String LOG_CACHE_NAME = "LogCache";
    private static final LogCacheManager logCacheManager = new LogCacheManager();

    private LogCacheManager() {
        CacheDiskStaticUtils.setDefaultCacheDiskUtils(CacheDiskUtils.getInstance(LOG_CACHE_NAME));
    }

    public static LogCacheManager getInstance() {
        return logCacheManager;
    }

    /**
     * 保存日志到磁盘
     *
     * @param logCache
     * @param key
     */
    public void save(LogCache logCache, String key) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "save() called key:" + key);
        }
        CacheDiskStaticUtils.put(key, new Gson().toJson(logCache));
    }

    public synchronized boolean clear(String key) {
        boolean ret = CacheDiskStaticUtils.remove(key);
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "clear() called  key:" + key + " ret:" + ret);
        }
        return ret;
    }

    public File getLogDirFile() {
        return new File(Utils.getApp().getCacheDir().getAbsoluteFile() + File.separator + LOG_CACHE_NAME);
    }

    public boolean clear() {
        File logDirFile = getLogDirFile();
        if (logDirFile.canRead() || logDirFile.exists()) {
            return logDirFile.delete();
        } else {
            return false;
        }
    }


    public synchronized List<LogCache> getLogCaches() {
        return getLogCaches(-1);
    }

    public synchronized List<LogCache> getLogCaches(long maxSize) {
        List<LogCache> logCaches;
        logCaches = new ArrayList<>();
        File logDirFile = getLogDirFile();
        if (!logDirFile.exists() || !logDirFile.canRead()) {
            return logCaches;
        }
        File[] logs = logDirFile.listFiles();
        if (logs == null) {
            return logCaches;
        }
        int size = 0;
        for (File log : logs) {
            if (maxSize > 0 && size >= maxSize) {
                break;
            }
            LogCache logCache = getLogCache(log);
            if (logCache != null) {
                logCaches.add(logCache);
            }
            size += log.getTotalSpace();
        }
        return logCaches;
    }


    public static LogCache getLogCache(File file) {
        BufferedReader reader = null;
        try {
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
