package com.pcommon.lib_cache;

import android.os.Environment;
import android.os.StatFs;

import java.io.File;

public class DiskTools {
    /**
     * 判断sd卡是否可用
     */
    public static boolean isExternalStorageAvailable() {
        return Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);
    }

    /**
     * 获取手机内部存储空间
     *
     * @return 以M, G为单位的容量
     */
    public static long getInternalMemorySize() {
        if (!isExternalStorageAvailable()) {
            return -1;
        }
        File file = Environment.getDataDirectory();
        StatFs statFs = new StatFs(file.getPath());
        long blockSizeLong = statFs.getBlockSizeLong();
        long blockCountLong = statFs.getBlockCountLong();
        return blockCountLong * blockSizeLong;
        // return Formatter.formatFileSize(context, size);
    }

    /**
     * 获取手机内部可用存储空间
     *
     * @return 以M, G为单位的容量
     */
    public static long getAvailableInternalMemorySize() {
        if (!isExternalStorageAvailable()) {
            return -1;
        }
        File file = Environment.getDataDirectory();
        StatFs statFs = new StatFs(file.getPath());
        long availableBlocksLong = statFs.getAvailableBlocksLong();
        long blockSizeLong = statFs.getBlockSizeLong();
        return availableBlocksLong * blockSizeLong;
//        return Formatter.formatFileSize(context, availableBlocksLong
//                * blockSizeLong);
    }

    /**
     * 获取手机外部存储空间
     *
     * @return 以M, G为单位的容量
     */
    public static long getExternalMemorySize() {
        if (!isExternalStorageAvailable()) {
            return -1;
        }
        File file = Environment.getExternalStorageDirectory();
        StatFs statFs = new StatFs(file.getPath());
        long blockSizeLong = statFs.getBlockSizeLong();
        long blockCountLong = statFs.getBlockCountLong();
        return blockCountLong * blockSizeLong;
        // return Formatter.formatFileSize(context, blockCountLong * blockSizeLong);
    }


    /**
     * 获取手机外部可用存储空间
     *
     * @return 以M, G为单位的容量
     */
    public static long getAvailableExternalMemorySize() {
        if (!isExternalStorageAvailable()) {
            return -1;
        }
        File file = Environment.getExternalStorageDirectory();
        StatFs statFs = new StatFs(file.getPath());
        long availableBlocksLong = statFs.getAvailableBlocksLong();
        long blockSizeLong = statFs.getBlockSizeLong();
        return availableBlocksLong
                * blockSizeLong;
/*
        return Formatter.formatFileSize(context, availableBlocksLong
                * blockSizeLong);
*/
    }

}
