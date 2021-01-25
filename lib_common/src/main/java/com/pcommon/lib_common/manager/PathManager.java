package com.pcommon.lib_common.manager;


import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.PathUtils;


public class PathManager {
    private String mDataRootPath;
    private String mLogPath;
    private String mCachePath;
    private String mDownloadPath;
    private static PathManager mInstance = null;

    public static PathManager get() {
        if (mInstance == null) {
            synchronized (PathManager.class) {
                if (mInstance == null) {
                    mInstance = new PathManager();
                }
            }
        }
        return mInstance;
    }

    private PathManager() {
        init();
    }

    private final static String TAG = "PathManager";

    private void init() {
        String appDataPath = PathUtils.getExternalAppFilesPath() + "/";
        mDataRootPath = createDir(appDataPath, "tk");
        mLogPath = createDir(mDataRootPath, "log");
        mCachePath = createDir(mDataRootPath, "cache");
        mDownloadPath = createDir(mDataRootPath, "download");
    }

    private String createDir(String parent, String dirName) {
        String path = parent + dirName + "/";
        FileUtils.createOrExistsDir(path);
//        if (!FileUtils.isDir(path)) {
//            Log.e(TAG, path + " create fail");
//        } else {
//            Log.i(TAG, path + " exists");
//        }
        return path;
    }

    public String getDataRootPath() {
        return mDataRootPath;
    }

    public String getLogPath() {
        return mLogPath;
    }

    public String getCachePath() {
        return mCachePath;
    }

    public String getDownloadPath() {
        return mDownloadPath;
    }
}