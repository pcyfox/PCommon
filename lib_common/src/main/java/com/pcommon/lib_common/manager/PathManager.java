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
        mDataRootPath = createDir(appDataPath, "df");
        mLogPath = createDir(mDataRootPath, "log");
        mCachePath = createDir(mDataRootPath, "cache");
        mDownloadPath = createDir(mDataRootPath, "download");
    }

    private String createDir(String parent, String dirName) {
        String path = parent + dirName + "/";
        FileUtils.createOrExistsDir(path);
        return path;
    }

    public String getDataRootPath() {
        return mDataRootPath;
    }

    public void setDataRootPath(String mDataRootPath) {
        this.mDataRootPath = mDataRootPath;
    }

    public String getLogPath() {
        return mLogPath;
    }

    public void setLogPath(String mLogPath) {
        this.mLogPath = mLogPath;
    }

    public String getCachePath() {
        return mCachePath;
    }

    public void setCachePath(String mCachePath) {
        this.mCachePath = mCachePath;
    }

    public String getDownloadPath() {
        return mDownloadPath;
    }

    public void setDownloadPath(String mDownloadPath) {
        this.mDownloadPath = mDownloadPath;
    }

    public static PathManager getInstance() {
        return mInstance;
    }

    public static void setInstance(PathManager mInstance) {
        PathManager.mInstance = mInstance;
    }
}