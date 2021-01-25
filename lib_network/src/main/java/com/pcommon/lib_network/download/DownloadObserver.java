package com.pcommon.lib_network.download;

import android.text.TextUtils;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;


public class DownloadObserver implements Observer<DownloadInfo> {
    private DownLoadCallback callback;
    private Disposable disposable;//可以用于取消注册的监听者
    private DownloadInfo downloadInfo;

    public DownloadObserver(DownLoadCallback callback) {
        this.callback = callback;
    }

    public Disposable getDisposable() {
        return disposable;
    }

    @Override
    public void onSubscribe(Disposable d) {
        disposable = d;
        if (callback != null) {
            callback.onStart();
        }
    }

    @Override
    public void onNext(DownloadInfo value) {
        this.downloadInfo = value;
        downloadInfo.setDownloadStatus(DownloadInfo.DOWNLOAD);
        callback.onProgress(downloadInfo.getProgress(), downloadInfo.getTotal());
    }

    @Override
    public void onError(Throwable e) {
        if (downloadInfo != null) {
            if (DownloadManager.getInstance().isDownCallContainsUrl(downloadInfo.getCacheKey())) {
                DownloadManager.getInstance().pauseDownload(downloadInfo.getCacheKey());
                downloadInfo.setDownloadStatus(DownloadInfo.DOWNLOAD_ERROR);
                if (callback != null) {
                    callback.onError(e.toString());
                }
            } else {
                downloadInfo.setDownloadStatus(DownloadInfo.DOWNLOAD_PAUSE);
                callback.onPause();
            }
        } else {
            if (callback != null) {
                String msg = "";
                if (e != null) {
                    if (e.getMessage() != null) {
                        msg = e.getMessage();
                    } else {
                        Throwable throwable = e.getCause();
                        if (throwable != null) {
                            msg = throwable.getMessage();
                        }
                    }

                    if (TextUtils.isEmpty(msg)) {
                        msg = e.getLocalizedMessage();
                    }
                    if (TextUtils.isEmpty(msg)) {
                        msg = e.toString();
                    }
                    e.printStackTrace();
                }
                callback.onError(msg);
            }
        }
    }

    @Override
    public void onComplete() {
        if (downloadInfo != null) {
            downloadInfo.setDownloadStatus(DownloadInfo.DOWNLOAD_OVER);
            if (callback != null) {
                callback.onFinish(downloadInfo);
                callback.onFinish(downloadInfo.getDownloadFilePath());
            }
        }
    }
}
