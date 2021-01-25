package com.pcommon.lib_cache;

import android.text.TextUtils;
import android.util.Log;

import com.elvishew.xlog.XLog;
import com.jakewharton.disklrucache.DiskLruCache;

import java.io.File;
import java.io.IOException;


public class DiskCacheManager {
    private static final String TAG = "DiskCacheManager";
    private DiskLruCache diskLruCache;

    private DiskCacheManager() {
    }

    private static final DiskCacheManager INSTANCE = new DiskCacheManager();

    public static DiskCacheManager INSTANCE() {
        return INSTANCE;
    }

    /*
     * directory – 缓存目录
     * appVersion - 缓存版本
     * valueCount – 每个key对应value的个数
     * maxSize – 缓存大小的上限
     */
    public void init(File directory, int appVersion, int valueCount, long maxSize) {
        XLog.i(TAG + ":init() called with: directory = [" + directory + "], appVersion = [" + appVersion + "], valueCount = [" + valueCount + "], maxSize = [" + maxSize + "]");
        try {
            diskLruCache = DiskLruCache.open(directory, appVersion, valueCount, maxSize);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void init(String directory) {
        long availableExternalMemorySize = DiskTools.getAvailableExternalMemorySize();
        long externalMemorySize = DiskTools.getExternalMemorySize();
        if (availableExternalMemorySize < 1) {
            return;
        }
        long maxSize = (long) (externalMemorySize * 0.6);
        if (maxSize > availableExternalMemorySize) {
            maxSize = (long) (availableExternalMemorySize * 0.9);
        }
        File file = new File(directory);
        if (file.isFile()) {
            return;
        }
        if (maxSize < 1024 * 1024 * 1024) {//小于1GB
            //TODO 该清理其他应用的数据
        }
        // maxSize=3*1024*1024;
        init(file, BuildConfig.VERSION_CODE, 1, maxSize);
    }

    public boolean isInitOk() {
        return diskLruCache != null;
    }

    public DiskLruCache.Editor getEditor(String key) {
        Log.d(TAG, "getEditor() called with: key = [" + key + "]");
        if (!isInitOk()) {
            return null;
        }
        try {
            return diskLruCache.edit(key);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void clear() {
        if (diskLruCache != null) {
            try {
                diskLruCache.delete();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public File getFile(String key, String suffix) {
        if (BuildConfig.DEBUG)
            Log.d(TAG, "getFile() called with: key = [" + key + "], suffix = [" + suffix + "]");
        if (!isInitOk()) {
            return null;
        }
        try {
            DiskLruCache.Entry entry = diskLruCache.getEntry(key);
            if (entry != null && !TextUtils.isEmpty(suffix)) {
                if (BuildConfig.DEBUG) Log.d(TAG, "getFile() called with: entry = [" + entry + "]");
                entry.setSuffix(suffix);
            }

            DiskLruCache.Snapshot snapshot = diskLruCache.get(key);
            if (BuildConfig.DEBUG)
                Log.d(TAG, "getFile() called with: snapshot = [" + snapshot + "]");

            if (snapshot == null) {
                return null;
            }
            DiskLruCache.Editor editor = snapshot.edit();
            if (editor == null) return null;
            editor.commit();
            return editor.getEntry().getCleanFile(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}