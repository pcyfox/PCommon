package com.pcommon.lib_network.download;


import android.text.TextUtils;
import android.util.Log;

import com.pcommon.lib_network.BuildConfig;
import com.pcommon.lib_network.MD5;

import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DownloadInfo {

    private static final String TAG = "DownloadInfo";
    /**
     * 下载状态
     */
    public static final int DOWNLOAD = 0;    // 下载中
    public static final int DOWNLOAD_PAUSE = 1; // 下载暂停
    public static final int DOWNLOAD_WAIT = 2;  // 等待下载
    public static final int DOWNLOAD_CANCEL = 3; // 下载取消
    public static final int DOWNLOAD_OVER = 4;    // 下载结束
    public static final int DOWNLOAD_ERROR = -10;  // 下载出错
    public static final long TOTAL_ERROR = -1;//获取进度失败
    private Map<String, String> headers; //请求头
    private String url;
    private String fileName;
    //File file = new File(storePath, downloadInfo.getFileName());
    private String downloadFilePath;//下载后文件的保存路径。完整的
    private int downloadStatus;
    private long total;
    private long progress;
    //通过post方式请下载，参数是json时使用
    private String jsonParam = "";
    //下载文件所在的根目录
    private String storeDir = "";
    private Key cacheKey;
    private boolean isUseCache;
    private String type;

    public DownloadInfo(String url) {
        this.url = url;
    }

    public DownloadInfo(String url, int downloadStatus) {
        this.url = url;
        this.downloadStatus = downloadStatus;
    }


    public boolean isUseCache() {
        return isUseCache;
    }

    public void setUseCache(boolean useCache) {
        isUseCache = useCache;
    }

    public Key getCacheKey() {
        return cacheKey;
    }

    public void setCacheKey(Key cacheKey) {
        this.cacheKey = cacheKey;
    }

    public String getJsonParam() {
        return jsonParam;
    }

    public void setJsonParam(String jsonParam) {
        this.jsonParam = jsonParam;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public String getUrl() {
        return url;
    }

    public String getFileName() {
        return fileName;
    }

    public String getType() {
        if (!TextUtils.isEmpty(type)) {
            return "";
        }
        return getType(url);
    }

    public static String getType(String url) {
        if (!TextUtils.isEmpty(url)) {
            int index = url.lastIndexOf(".");
            if (index < 0) {
                return "";
            }
            String pattern = "(\\.(\\w+)\\?)|(\\.(\\w+)$)";
            // 创建 Pattern 对象
            Pattern p = Pattern.compile(pattern);
            Matcher m = p.matcher(url);
            if (m.find()) {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "getType() called with: url = [" + url + "] type=" + m.group());
                }
                String ret = m.group();
                if (ret.contains("?")) {
                    ret = ret.replace("?", "");
                }
                return ret;
            }
        }
        return "";
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getProgress() {
        return progress;
    }

    public void setProgress(long progress) {
        this.progress = progress;
    }

    public int getDownloadStatus() {
        return downloadStatus;
    }

    public void setDownloadStatus(int downloadStatus) {
        this.downloadStatus = downloadStatus;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setStoreDir(String storePath) {
        this.storeDir = storePath;
    }

    public String getStoreDir() {
        return storeDir;
    }

    public String getDownloadFilePath() {
        if (DownloadInfo.DOWNLOAD_OVER != downloadStatus) {
            throw new IllegalStateException("DOWNLOAD_OVER ?");
        }
        return downloadFilePath;
    }

    public void setDownloadFilePath(String downloadFilePath) {
        this.downloadFilePath = downloadFilePath;
    }

    public static Key createKey(String url, String requestParam) {
        String param = TextUtils.isEmpty(requestParam) ? "" : requestParam;
        String keySeed = url + param;
        return new Key(keySeed);
    }

    @Override
    public String toString() {
        return "DownloadInfo{" +
                "headers=" + headers +
                ", url='" + url + '\'' +
                ", fileName='" + fileName + '\'' +
                ", downloadFilePath='" + downloadFilePath + '\'' +
                ", downloadStatus=" + downloadStatus +
                ", total=" + total +
                ", progress=" + progress +
                ", jsonParam='" + jsonParam + '\'' +
                ", storePath='" + storeDir + '\'' +
                ", cacheKey=" + cacheKey +
                '}';
    }

    public static class Key {
        private String key;

        public Key(String key) {
            this.key = "" + MD5.getMD5(key);
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = "" + MD5.getMD5(key);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Key key1 = (Key) o;
            return Objects.equals(key, key1.key);
        }

        @Override
        public int hashCode() {
            return Objects.hash(key);
        }

        @Override
        public String toString() {
            return "Key{" +
                    "key='" + key + '\'' +
                    '}';
        }
    }


}
