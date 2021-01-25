package com.pcommon.lib_network.download;


import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.jakewharton.disklrucache.DiskLruCache;
import com.pcommon.lib_cache.DiskCacheManager;
import com.pcommon.lib_network.BuildConfig;
import com.pcommon.lib_network.MD5;
import com.pcommon.lib_network.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicReference;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * 下载管理
 */
public class DownloadManager {
    private static final String TAG = "DownloadManager";
    private static final AtomicReference<DownloadManager> INSTANCE = new AtomicReference<>();
    private OkHttpClient okHttpClient;
    private final WeakHashMap<DownloadInfo.Key, Call> downCalls; //用来存放各个下载的请求
    private SoftReference<Map<DownloadInfo.Key, DownloadInfo>> memoryCache;

    private boolean isSupportBreakpointDown;
    private DownLoadCallback callback;
    private final static MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private String defStoreDir = Environment.getExternalStorageDirectory().getPath() + File.separator + "DownloadManager";
    private final boolean isDebug = BuildConfig.DEBUG;

    public static DownloadManager getInstance() {
        for (; ; ) {
            DownloadManager current = INSTANCE.get();
            if (current != null) {
                return current;
            }
            current = new DownloadManager();
            if (INSTANCE.compareAndSet(null, current)) {
                return current;
            }
        }
    }

    private DownloadManager() {
        downCalls = new WeakHashMap<>();
        okHttpClient = new OkHttpClient.Builder().build();
        Map<DownloadInfo.Key, DownloadInfo> map = new HashMap<>();
        memoryCache = new SoftReference<>(map);
    }


    public OkHttpClient getOkHttpClient() {
        return okHttpClient;
    }

    public void setOkHttpClient(OkHttpClient okHttpClient) {
        this.okHttpClient = okHttpClient;
    }

    /**
     * 查看是否在下载任务中
     *
     * @param key
     * @return
     */
    public boolean isDownCallContainsUrl(DownloadInfo.Key key) {
        return downCalls.containsKey(key);
    }

    public DownloadManager setSupportBreakpointDown(boolean isSupportBreakpointDown) {
        this.isSupportBreakpointDown = isSupportBreakpointDown;
        return this;
    }


    public void download(final String url, final String cacheKey, final String jsonParam, final String fileName, final String storeDir, final Map<String, String> headers, final boolean isUseCache, final DownLoadCallback callback) {
        if (isDebug)
            Log.d(TAG, "download() called with: url = [" + url + "], cacheKey = [" + cacheKey + "], jsonParam = [" + jsonParam + "], fileName = [" + fileName + "], storeDir = [" + storeDir + "], headers = [" + headers + "], isUseCache = [" + isUseCache + "]");
        this.callback = callback;
        if (TextUtils.isEmpty(url) || callback == null) {
            return;
        }

        //初始化下载根目录
        String rootPath = defStoreDir;
        if (!TextUtils.isEmpty(storeDir)) {
            File store = new File(storeDir);
            if (store.exists()) {
                rootPath = storeDir;
            } else {
                if (store.mkdirs()) {
                    rootPath = storeDir;
                }
            }
        }
        if (isUseCache) {
            final DownloadInfo.Key key = buildKey(url, jsonParam, cacheKey);
            getCacheDownloadInfo(key, url, fileName, rootPath, new CacheDownloadInfoCallback() {
                @Override
                public void onCache(DownloadInfo info) {
                    if (info != null && info.getDownloadStatus() == DownloadInfo.DOWNLOAD_OVER && new File(info.getDownloadFilePath()).exists()) {
                        addToMemoryCache(info);
                        String localPath = info.getDownloadFilePath();
                        info.setCacheKey(key);
                        callback.onFinish(info);
                        callback.onFinish(localPath);
                    } else {
                        doDownload(url, cacheKey, jsonParam, fileName, storeDir, headers, true, callback);
                        if (BuildConfig.DEBUG) Log.d(TAG, ": not found cache");
                    }
                }
            });
        } else {
            doDownload(url, cacheKey, jsonParam, fileName, rootPath, headers, false, callback);
        }
    }

    public DownloadInfo.Key buildKey(String url, String param, String userCacheKey) {
        DownloadInfo.Key key = null;
        if (TextUtils.isEmpty(userCacheKey)) {
            key = DownloadInfo.createKey(url, param);
        } else {
            key = new DownloadInfo.Key(userCacheKey);
        }
        return key;
    }

    private void doDownload(final String url, final String cacheKey, final String jsonParam, final String fileName, final String storeDir, final Map<String, String> headers, final boolean isUseCache, DownLoadCallback callback) {
        Observable.just(url).filter(new Predicate<String>() { // 过滤 call的map中已经有了,就证明正在下载,则这次不下载
            @Override
            public boolean test(String s) {
                return !downCalls.containsKey(new DownloadInfo.Key(url));
            }
        })
                .map(new Function<String, DownloadInfo>() { // 生成 DownloadInfo
                    @Override
                    public DownloadInfo apply(String url) {
                        return createDownInfo(url, cacheKey, isUseCache, jsonParam, fileName, storeDir, headers);
                    }
                })
                .map(new Function<DownloadInfo, DownloadInfo>() { // 如果已经下载，重新命名
                    @Override
                    public DownloadInfo apply(DownloadInfo downloadInfo) {
                        return getRealFileName(downloadInfo);
                    }
                })
                .flatMap(new Function<DownloadInfo, ObservableSource<DownloadInfo>>() { // 下载
                    @Override
                    public ObservableSource<DownloadInfo> apply(DownloadInfo downloadInfo) {
                        return Observable.create(new DownloadSubscribe(downloadInfo));
                    }
                })
                .observeOn(AndroidSchedulers.mainThread()) // 事件回调的线程
                .subscribeOn(Schedulers.io()) //事件执行的线程
                .subscribe(new DownloadObserver(callback)); //  添加观察者，监听下载进度
    }


    /**
     * 开始下载
     *
     * @param url 下载请求的网址
     */
    public void download(final String url, boolean isUseCache, DownLoadCallback callback) {
        download(url, null, null, null, null, null, isUseCache, callback);
    }

    public void download(final String url, DownLoadCallback callback) {
        download(url, null, null, null, null, null, true, callback);
    }

    public void downloadWithCacheKey(final String url, String cacheKey, DownLoadCallback callback) {
        download(url, cacheKey, null, null, null, null, true, callback);
    }


    public void downloadToDir(String dUrl, String storeDir, String cacheKey, boolean isUseCache, DownLoadCallback downloadUrlCallback) {
        download(dUrl, cacheKey, null, null, storeDir, null, isUseCache, downloadUrlCallback);
    }

    public void downloadToDir(String dUrl, String storeDir, boolean isUseCache, DownLoadCallback downloadUrlCallback) {
        download(dUrl, null, null, null, storeDir, null, isUseCache, downloadUrlCallback);
    }


    /**
     * 下载取消或者暂停
     *
     * @param key
     */
    public void pauseDownload(DownloadInfo.Key key) {
        Call call = downCalls.get(key);
        if (call != null) {
            call.cancel();//取消
        }
        downCalls.remove(key);
    }

    public String getDefStoreDir() {
        return defStoreDir;
    }

    /**
     * 取消下载 删除本地文件
     *
     * @param info
     */
    public void cancelDownload(DownloadInfo info) {
        pauseDownload(info.getCacheKey());
        info.setProgress(0);
        info.setDownloadStatus(DownloadInfo.DOWNLOAD_CANCEL);
    }


    public void cancelDownload(DownloadInfo.Key key) {
        if (memoryCache.get() == null) {
            return;
        }
        DownloadInfo info = memoryCache.get().get(key);
        if (info != null) {
            cancelDownload(info);
        }
    }


    public void getCacheDownloadInfo(DownloadInfo.Key key, final String url, final String fileName, final String storeDir, final CacheDownloadInfoCallback callback) {
        if (BuildConfig.DEBUG)
            Log.d(TAG, "getCacheDownloadInfo() called with: key = [" + key + "], url = [" + url + "], fileName = [" + fileName + "], storeDir = [" + storeDir + "], callback = [" + callback + "]");

        DownloadInfo downloadInfo = null;
        //内存缓存
        if (memoryCache != null && memoryCache.get() != null) {
            downloadInfo = memoryCache.get().get(key);
            if (downloadInfo != null && new File(downloadInfo.getDownloadFilePath()).exists()) {
                if (BuildConfig.DEBUG)
                    Log.d(TAG, ":hunt file from ------------>memoryCache localPath:" + downloadInfo.getDownloadFilePath());
                callback.onCache(downloadInfo);
                return;
            }
        }

        //磁盘缓存
        final File file = DiskCacheManager.INSTANCE().getFile(key.getKey(), DownloadInfo.getType(url));
        if (file != null && file.exists()) {
            final DownloadInfo cacheDownloadInfo = new DownloadInfo(url);
            if (TextUtils.isEmpty(fileName)) {
                cacheDownloadInfo.setFileName(file.getName());
            } else {
                cacheDownloadInfo.setFileName(fileName);
            }
            cacheDownloadInfo.setStoreDir(storeDir);
            cacheDownloadInfo.setDownloadStatus(DownloadInfo.DOWNLOAD_OVER);
            if (defStoreDir.equals(storeDir)) {//用户未指定存储区域,直接返回缓存文件
                cacheDownloadInfo.setDownloadFilePath(file.getAbsolutePath());
                if (BuildConfig.DEBUG)
                    Log.d(TAG, ":hunt file from direct------------>diskCache localPath:" + cacheDownloadInfo.getDownloadFilePath());
                callback.onCache(cacheDownloadInfo);
            } else {
                //将缓存文件复制到用户指定目录
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            File dest = new File(cacheDownloadInfo.getStoreDir(), cacheDownloadInfo.getFileName());
                            if (dest.exists()) {
                                dest.delete();
                            }
                            Utils.copyNio(file.getAbsolutePath(), dest.getAbsolutePath());
                            cacheDownloadInfo.setDownloadFilePath(dest.getAbsolutePath());
                            callback.onCache(cacheDownloadInfo);
                            if (BuildConfig.DEBUG)
                                Log.d(TAG, ":hunt file from ,and do copy------------>diskCache localPath:" + cacheDownloadInfo.getDownloadFilePath());
                        } catch (Exception e) {
                            callback.onCache(null);
                        }
                    }
                }).start();
            }

        } else {
            callback.onCache(null);
        }
    }


    private void addToMemoryCache(DownloadInfo downloadInfo) {
        if (BuildConfig.DEBUG)
            Log.d(TAG, "addToMemoryCache() called with: downloadInfo = [" + downloadInfo + "]");
        if (memoryCache != null && memoryCache.get() != null) {
            memoryCache.get().put(downloadInfo.getCacheKey(), downloadInfo);
        }
    }


    public void clearCache() {
        if (memoryCache.get() != null) {
            for (DownloadInfo info : memoryCache.get().values()) {
                if (info != null) {
                    cancelDownload(info);
                }
            }
            memoryCache.clear();
        }

        // TODO 清除磁盘中的缓存
      /*

        if (diskCacheManager != null) {
            diskCacheManager.clear();
        }

      * */
    }


    public void clearTaskDeep(DownloadInfo info) {
        DownloadInfo.Key key = info.getCacheKey();
        if (key == null || TextUtils.isEmpty(key.getKey())) {
            return;
        }
        if (BuildConfig.DEBUG) Log.d(TAG, "clearTaskDeep() called with: key= [" + key + "]");
        if (downCalls != null) {
            Map.Entry<DownloadInfo.Key, Call> deleteEntry = null;
            for (Map.Entry<DownloadInfo.Key, Call> entry : downCalls.entrySet()) {
                if (entry.getKey().equals(key)) {
                    entry.getValue().cancel();
                    deleteEntry = entry;
                    break;
                }
            }
            if (deleteEntry != null) {
                if (BuildConfig.DEBUG) Log.d(TAG, "clearTaskDeep() clear call success ");
                deleteEntry.getValue().cancel();
                downCalls.entrySet().remove(deleteEntry);
            }
        }

        //清除内存缓存并删除已下载的文件（如果有）
        Map<DownloadInfo.Key, DownloadInfo> map = memoryCache.get();
        if (map != null && !map.isEmpty()) {
            Iterator<DownloadInfo.Key> iterator = map.keySet().iterator();
            while (iterator.hasNext()) {
                DownloadInfo.Key next = iterator.next();
                if (key.equals(next)) {
                    String filePath = map.get(next).getDownloadFilePath();
                    File file = new File(filePath);
                    if (file.exists() && file.isFile()) {
                        if (BuildConfig.DEBUG)
                            Log.d(TAG, "clearTaskDeep() : key= [" + key + "] 删除已下载文件：" + filePath);
                        file.delete();
                    }
                    iterator.remove();
                    if (BuildConfig.DEBUG)
                        Log.d(TAG, "clearTaskDeep() : key = [" + key + " 清除内存缓存");
                    break;
                }
            }
        }

        //清除磁盘缓存
        final File file = DiskCacheManager.INSTANCE().getFile(key.getKey(), null);
        if (file != null && file.isFile() && file.exists()) {
            if (BuildConfig.DEBUG)
                Log.d(TAG, "clearTaskDeep() : key= [" + key + " 清除磁盘缓存 :" + file.getAbsolutePath());
            file.delete();
        }
        if (!TextUtils.isEmpty(info.getDownloadFilePath())) {
            File storeFile = new File(info.getDownloadFilePath());
            if (storeFile.exists()) {
                storeFile.delete();
                info.setDownloadFilePath("");
                if (BuildConfig.DEBUG)
                    Log.d(TAG, "clearTaskDeep() : key= [" + key + " 清理存储文件 :" + storeFile.getAbsolutePath());
            }
        }

    }

    public void clearTask() {
        if (downCalls != null) {
            for (Call call : downCalls.values()) {
                if (call != null) {
                    call.cancel();
                }
            }
            downCalls.clear();
        }
    }

    public void clear() {
        clearCache();
        clearTask();
        callback = null;
        File cacheFile = new File(defStoreDir);
        if (cacheFile.exists()) {
            cacheFile.delete();
        }
    }


    public void clear(String url) {
        if (downCalls != null) {
            DownloadInfo.Key foundKey = null;
            for (DownloadInfo.Key key : downCalls.keySet()) {
                if (key.equals(new DownloadInfo.Key(url))) {
                    foundKey = key;
                    break;
                }
            }
            if (foundKey != null) {
                Call call = downCalls.get(foundKey);
                if (call != null) {
                    call.cancel();
                }
            }
            downCalls.remove(foundKey);
        }
    }


    private DownloadInfo createDownInfo(String url, String cacheKey, boolean isUseCache, String jsonParam, String name, String storePath, Map<String, String> headers) {
        String key = TextUtils.isEmpty(cacheKey) ? url + jsonParam : cacheKey;
        if (TextUtils.isEmpty(storePath)) {
            storePath = defStoreDir;
        }
        DownloadInfo downloadInfo = new DownloadInfo(url);
        downloadInfo.setStoreDir(storePath);
        downloadInfo.setHeaders(headers);
        downloadInfo.setUseCache(isUseCache);
        DownloadInfo.Key realKey = buildKey(url, jsonParam, cacheKey);
        downloadInfo.setCacheKey(realKey);
        downloadInfo.setJsonParam(jsonParam);
        long contentLength = getContentLength(url);//获得文件大小
        downloadInfo.setTotal(contentLength);
        String nameFormUrl = getFileNameFormUrl(url);
        String fileName = TextUtils.isEmpty(name) ? nameFormUrl : name;
        downloadInfo.setFileName(fileName);

        if (memoryCache.get() == null) {
            Map<DownloadInfo.Key, DownloadInfo> map = new HashMap<>();
            memoryCache = new SoftReference<>(map);
        }
        return downloadInfo;
    }

    private String getFileNameFormUrl(String url) {
        if (TextUtils.isEmpty(url)) {
            return "null";
        }
        int index = url.indexOf("?");
        String sub = url;
        if (index > 0) {
            sub = url.substring(0, index);
            int splitIndex = sub.lastIndexOf("/");
            if (splitIndex >= 0) {
                return sub.substring(splitIndex + 1);
            } else {
                return sub;
            }
        }
        return sub.substring(sub.lastIndexOf("/") + 1);
    }

    /**
     * 如果文件已下载重新命名新文件名
     *
     * @param downloadInfo
     * @return
     */
    private DownloadInfo getRealFileName(DownloadInfo downloadInfo) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "getRealFileName() called with: downloadInfo = [" + downloadInfo + "]");
        }
        String fileName = downloadInfo.getFileName();
        long downloadLength = 0;
        long contentLength = downloadInfo.getTotal();
        File path = new File(downloadInfo.getStoreDir());
        if (!path.exists()) {
            path.mkdir();
        }

        File file = new File(downloadInfo.getStoreDir(), fileName);
        if (isSupportBreakpointDown) {
            if (file.exists()) {
                //找到了文件,代表已经下载过（但不见得下载全）,则获取其长度
                downloadLength = file.length();
            }
            //之前下载过,需要重新来一个文件
            int i = 1;
            while (downloadLength >= contentLength) {
                int dotIndex = fileName.lastIndexOf(".");
                String fileNameOther;
                if (dotIndex == -1) {
                    fileNameOther = fileName + "(" + i + ")";
                } else {
                    fileNameOther = fileName.substring(0, dotIndex)
                            + "(" + i + ")" + fileName.substring(dotIndex);
                }
                File newFile = new File(downloadInfo.getStoreDir(), fileNameOther);
                file = newFile;
                downloadLength = newFile.length();
                i++;
            }
        } else {
            if (file.exists()) {
                file.delete();
            }
        }
        //设置改变过的文件名/大小
        downloadInfo.setProgress(downloadLength);
        downloadInfo.setFileName(file.getName());
        return downloadInfo;
    }

    private class DownloadSubscribe implements ObservableOnSubscribe<DownloadInfo> {
        private final DownloadInfo downloadInfo;

        public DownloadSubscribe(DownloadInfo downloadInfo) {
            this.downloadInfo = downloadInfo;
        }

        @Override
        public void subscribe(ObservableEmitter<DownloadInfo> emitter) throws Exception {
            executeDownload(emitter);
        }

        private void executeDownload(ObservableEmitter<DownloadInfo> emitter) throws IOException {
            String url = downloadInfo.getUrl();
            long downloadLength = downloadInfo.getProgress();//已经下载好的长度
            long contentLength = downloadInfo.getTotal();//文件的总长度

            //初始进度信息
            emitter.onNext(downloadInfo);
            Request.Builder builder = new Request.Builder()
                    //确定下载的范围,添加此头,则服务器就可以跳过已经下载好的部分
                    .addHeader("RANGE", "bytes=" + downloadLength + "-" + contentLength)
                    .url(url);
            Map<String, String> headers = downloadInfo.getHeaders();
            if (headers != null) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    builder.addHeader(entry.getKey(), entry.getValue());
                }
            }
            //以post-JSON方法下载
            if (!TextUtils.isEmpty(downloadInfo.getJsonParam())) {
                RequestBody requestBody = RequestBody.create(JSON, downloadInfo.getJsonParam());
                builder.post(requestBody);
            }

            Request request = builder.build();
            Call call = okHttpClient.newCall(request);
            downCalls.put(downloadInfo.getCacheKey(), call);//把这个添加到call里,方便取消
            //直接请求（未使用线程池）
            Response response = call.execute();
            String storePath = downloadInfo.getStoreDir();
            if (isDebug)
                Log.d(TAG, "executeDownload()  downloadInfo = [" + downloadInfo + "]");

            FileOutputStream fileOutputStream = null;
            OutputStream outputStream = null;
            ResponseBody body = response.body();
            if (body == null) {
                return;
            }

            DiskLruCache.Editor editor = null;
            InputStream is = body.byteStream();
            try {
                File file = new File(storePath, downloadInfo.getFileName());
                if (file.exists()) {
                    file.delete();
                } else {
                    if (!file.getParentFile().exists()) {
                        file.getParentFile().mkdirs();// 能创建多级目录
                    }
                }
                //判断是否需要磁盘缓存
                if (downloadInfo.isUseCache() && DiskCacheManager.INSTANCE().isInitOk()) {
                    editor = DiskCacheManager.INSTANCE().getEditor(downloadInfo.getCacheKey().getKey());
                    editor.getEntry().setSuffix(downloadInfo.getType());
                    outputStream = editor.newOutputStream(0);
                } else {
                    fileOutputStream = new FileOutputStream(file, true);
                }

                byte[] buffer = new byte[4086 * 2];//缓冲数组4kB
                int len;

                while ((len = is.read(buffer)) != -1) {
                    if (call.isCanceled()) {
                        DiskLruCache.Entry entry = editor.getEntry();
                        if (entry != null) {
                            File cacheFile = editor.getEntry().getCleanFile(0);
                            if (cacheFile != null && cacheFile.exists()) {
                                cacheFile.delete();
                            }
                        }
                        if (file.isFile()) {
                            file.delete();
                        }
                        break;
                    }
                    if (outputStream != null) {
                        outputStream.write(buffer, 0, len);
                    } else {
                        if (fileOutputStream == null) {
                            return;
                        }
                        fileOutputStream.write(buffer, 0, len);
                    }
                    downloadLength += len;
                    downloadInfo.setProgress(downloadLength);
                    if (!emitter.isDisposed()) {
                        if (call.isCanceled()) {
                            emitter.onError(new Throwable("call is canceled"));
                        } else {
                            emitter.onNext(downloadInfo);
                        }
                    }
                }

                if (outputStream != null) {
                    outputStream.flush();
                }
                if (editor != null) {//说明已开启缓存功能
                    editor.commit();
                    File cacheFile = editor.getEntry().getCleanFile(0);
                    if (TextUtils.isEmpty(downloadInfo.getStoreDir())) {
                        downloadInfo.setStoreDir(cacheFile.getParent());
                        downloadInfo.setDownloadFilePath(cacheFile.getAbsolutePath());
                    } else {//用户指定了特定存放位置
                        File dest = new File(downloadInfo.getStoreDir(), downloadInfo.getFileName());
                        if (dest.exists()) {//下载的文件已经存在
                            String destFileMD5 = MD5.getFileMD5(dest);
                            String cacheFileMD5 = MD5.getFileMD5(dest);
                            if (Objects.equals(destFileMD5, cacheFileMD5)) {//已存在的文件与磁盘缓存中文件的MD5一致
                                downloadInfo.setDownloadFilePath(dest.getAbsolutePath());
                            }
                            return;
                        }
                        //拷贝磁盘缓存文件到用户的地方
                        Utils.copyNio(cacheFile.getAbsolutePath(), dest.getAbsolutePath());
                        downloadInfo.setDownloadFilePath(dest.getAbsolutePath());
                    }
                } else {
                    downloadInfo.setDownloadFilePath(file.getAbsolutePath());
                }

                if (fileOutputStream != null) {
                    fileOutputStream.flush();
                }
                downCalls.remove(downloadInfo.getCacheKey());
                if (downloadInfo.isUseCache()) {
                    addToMemoryCache(downloadInfo);
                }
                if (isDebug)
                    Log.d(TAG, "executeDownload() called 下载完成: downloadInfo = [" + downloadInfo + "]");
            } catch (Exception e) {
                if (!emitter.isDisposed()) {
                    emitter.onError(e);
                    if (editor != null) {
                        editor.abort();
                    }
                }
                e.printStackTrace();
            } finally {
                //关闭IO流
                CloseUtils.close(is, fileOutputStream);
            }

            if (!emitter.isDisposed()) {
                emitter.onComplete();//完成
            }
        }
    }

    public void setDefStoreDir(String defStoreDir) {
        this.defStoreDir = defStoreDir;
    }

    /**
     * 获取下载长度
     *
     * @param downloadUrl
     * @return
     */
    private long getContentLength(String downloadUrl) {
        Request request = new Request.Builder()
                .url(downloadUrl)
                .build();
        try {
            if (okHttpClient == null) {
                throw new IllegalArgumentException("okHttpClient is null");
            }
            Response response = okHttpClient.newCall(request).execute();
            ResponseBody body = response.body();
            if (body != null && response.isSuccessful()) {
                long contentLength = body.contentLength();
                response.close();
                return contentLength == 0 ? DownloadInfo.TOTAL_ERROR : contentLength;
            }
        } catch (IOException e) {
            if (callback != null) {
                callback.onError(e.toString());
            }
            e.printStackTrace();
        }
        return DownloadInfo.TOTAL_ERROR;
    }

}
