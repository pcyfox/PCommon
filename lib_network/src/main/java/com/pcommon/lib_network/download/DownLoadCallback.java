package com.pcommon.lib_network.download;

import com.elvishew.xlog.XLog;

public abstract class DownLoadCallback {
    public void onStart() {
    }


    public void onPause() {
    }


    public void onProgress(float progress, long totalSize) {
    }


    public void onFinish(String file) {
    }


    public void onFinish(DownloadInfo downloadInfo) {
    }


    public void onError(String msg) {
        XLog.w("DownLoadCallback:" + msg);
    }


}
