package com.pcommon.lib_log.printer;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

import com.blankj.utilcode.util.TimeUtils;
import com.elvishew.xlog.LogLevel;
import com.elvishew.xlog.XLog;
import com.elvishew.xlog.printer.Printer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.pcommon.lib_log.BuildConfig;
import com.pcommon.lib_log.LogCache;
import com.pcommon.lib_log.LogCacheManager;
import com.pcommon.lib_network.RequestManager;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * 主要是处理日志上传
 */
public class CloudLogPrinter implements Printer {
    public boolean isDebug = false;
    private static final String TAG = "CloudLogPrinter";
    private final List<String> mLogs = new ArrayList<>();
    private BasePrintLogReq printLogReq;//必需是个JavaBean
    private String index;
    private int quantityInterval = 30;//上传数量间隔,默认是没满30条就上传
    private static String url = "";//日志上传url
    private final Map<String, String> header = new HashMap<>();
    private static final String KEY_LOG_LEVEL = "log_level";
    private final Handler logUpDateHandler;
    private boolean isAutoUpdateLog = false;
    private LogUploadInterceptor logUploadInterceptor;

    public String getIndex() {
        return index;
    }

    public void setAutoUpdateLog(boolean autoUpdateLog) {
        isAutoUpdateLog = autoUpdateLog;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public int getQuantityInterval() {
        return quantityInterval;
    }

    public void setQuantityInterval(int quantityInterval) {
        this.quantityInterval = quantityInterval;
    }

    private volatile boolean isUpdateByUser = false;

    private static volatile boolean isUpdating = false;
    private float addLogCount;
    private long lastAddTime;
    private boolean isTooFast = false;
    private int tooFastCount;//持续发生添加日志过快的次数


    private CloudLogPrinter() {
        HandlerThread handlerThread = new HandlerThread("updateLogHandlerThread");
        handlerThread.start();
        logUpDateHandler = new Handler(handlerThread.getLooper());
    }


    /**
     * @param printLogReq 自定义打印类对象
     */
    public void init(BasePrintLogReq printLogReq/*必需是一个JavaBean*/, String url, String index) {
        this.printLogReq = printLogReq;
        this.index = index;
        CloudLogPrinter.url = url;
    }

    public void setLogUploadInterceptor(LogUploadInterceptor logUploadInterceptor) {
        this.logUploadInterceptor = logUploadInterceptor;
    }

    private static final CloudLogPrinter instance = new CloudLogPrinter();

    public static CloudLogPrinter getInstance() {
        return instance;
    }

    public BasePrintLogReq getPrintLogReq() {
        return printLogReq;
    }

    public Map<String, String> getHeader() {
        return header;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        CloudLogPrinter.url = url;
    }

    private long getTime() {
        long timeDifference = 0;
        return System.currentTimeMillis() + timeDifference;
    }

    @Override
    public void println(final int logLevel, final String tag, final String msg) {
        if (TextUtils.isEmpty(msg) || TextUtils.isEmpty(url)) {
            return;
        }
        //只有在debug模式下才会打印日志级别低于或等于debug的
        if (logLevel <= LogLevel.DEBUG) {
            if (isDebug) {
                Log.d(tag, msg);
            }
            return;
        }

        Log.println(logLevel, tag, msg);
        Runnable worker = new Runnable() {
            @Override
            public void run() {
                String logMsg = msg;
                if (logUploadInterceptor != null) {
                    logMsg = logUploadInterceptor.upload(logLevel, tag, msg);
                }
                String time = "" + SystemClock.uptimeMillis();
                String levelName = "";
                switch (logLevel) {
                    case LogLevel.NONE:
                        levelName = "CRASH";
                        header.put(KEY_LOG_LEVEL, levelName);
                        upload(tag, logMsg, levelName + "-" + time, true);
                        break;
                    case LogLevel.ERROR:
                        levelName = LogLevel.getLevelName(logLevel);
                        header.put(KEY_LOG_LEVEL, levelName);
                        upload(tag, logMsg, levelName + "-" + time, true);
                        break;
                    default:
                        levelName = LogLevel.getLevelName(logLevel);
                        header.put(KEY_LOG_LEVEL, levelName);
                        upload(tag, logMsg, levelName + "-" + time, false);
                }
            }
        };
        if (logLevel >= LogLevel.ERROR) {
            logUpDateHandler.postAtFrontOfQueue(worker);
        } else {
            logUpDateHandler.post(worker);
        }
    }

    private void upload(String tag, final String msg, String cacheKey, boolean isUpdateNow) {
        if (!isAutoUpdateLog) {
            return;
        }
        synchronized (mLogs) {
            if (isUpdateNow) {
                handleUpdate(createLog(msg), cacheKey);
                return;
            }
            if (mLogs.size() < quantityInterval || isUpdating) {
                addLog(tag, msg);
                return;
            }

            if (isUpdateByUser) {
                return;
            }
            int size = mLogs.size();
            Log.d(TAG, "upload log:-----------------> 日志已满，开始打包上传  size:" + size + " quantityInterval:" + quantityInterval);
            doUpdate(cacheKey);
        }
    }

    private void doUpdate(String cacheKey) {
        int size = mLogs.size();
        try {
            List<String> temp = new ArrayList<>();
            final StringBuilder reqContent = new StringBuilder();
            for (int i = 0; i < size; i++) {
                if (i == quantityInterval) {
                    break;
                }
                String log = mLogs.get(i);
                reqContent.append(log).append("\n\n");
                temp.add(mLogs.get(i));
            }
            mLogs.removeAll(temp);
            Log.d(TAG, "upload log:-----------------> 日志已处理" + size + "条,还剩:" + mLogs.size() + "条");
            String log = new String(reqContent.toString().getBytes(), StandardCharsets.UTF_8);
            handleUpdate(log, cacheKey);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void handleUpdate(String reqContent, String cacheKey) {
        LogCache logCache = new LogCache(header, reqContent, cacheKey);
        try {
            LogCacheManager.getInstance().save(logCache, cacheKey);
        } catch (OutOfMemoryError error) {//日志过大可能搞爆内存
            XLog.e(error.getMessage());
        }
        realUpdate(header, reqContent, cacheKey);
    }


    private void addLog(String tag, String msg) {
        synchronized (mLogs) {
            long span = SystemClock.uptimeMillis() - lastAddTime;
            //  Log.d(TAG, "addLog() called with: span = [" + span + "]");
            if (span >= 1000 && span <= 1500) {
                // Log.d(TAG, "addLog() called with: addLogRate = [" + addLogCount + "]");
                if (addLogCount > 30) {//短时间内上传日志速率过高，代码可能有异常！
                    isTooFast = true;
                }
                addLogCount = 0;
            } else if (span > 1500) {
                isTooFast = false;
                addLogCount = 0;
            }

            if (isTooFast) {
                tooFastCount++;
            } else {
                tooFastCount = 0;
            }
            if (tooFastCount >= 50) {
                //TODO:代码极有可能出现问题
                String tip = " addLog() called with:添加日志过快，日志将被丢弃！";
                if (tooFastCount % 1008 == 0) {
                    XLog.e(TAG + tip);
                } else {
                    Log.e(TAG, tip);
                }
            }

            if (!isTooFast) {
                if (mLogs.size() >= 50) {//避免一直添加，撑爆内存
                    Log.e(TAG, "addLog() 添加日志达到上线，丢掉最早的一条日志");
                    mLogs.remove(0);
                }
                mLogs.add(createLog(msg));
            }

            if (addLogCount == 0) {
                lastAddTime = SystemClock.uptimeMillis();
            }
            addLogCount++;
        }
    }

    private String createLog(String msg) {
        StringBuilder reqLogItem = new StringBuilder();
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        printLogReq.msg = msg;
        printLogReq.ts = TimeUtils.millis2String(System.currentTimeMillis());
        reqLogItem.append(gson.toJson(printLogReq));
        return reqLogItem.toString();
    }

    private static void realUpdate(final Map<String, String> header, final String reqContent, final String cacheKey) {
        isUpdating = true;
        RequestManager.get().asyncPost(url, reqContent, header, false, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                isUpdating = false;
                Log.e(TAG, "upload log fail url:" + url + " header:" + header + "\n Exception:" + e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                isUpdating = false;
                if (!response.isSuccessful()) {
                    Log.e(TAG, String.format("upload log  onResponse. code:%d   req:%s", response.code(), response.body() == null ? "" : response.body().string()));
                } else {
                    synchronized (CloudLogPrinter.class) {
                        LogCacheManager.getInstance().clear(cacheKey);
                    }
                    Log.d(TAG, "upload log successfully !!");
                }
            }
        });
    }

    /**
     * 上传当前内存中的日志
     */
    public void uploadCurrentLogs() {
        XLog.d(TAG + ":uploadCurrentLogs() called----------------->");
        if (logUpDateHandler == null || mLogs.isEmpty()) {
            return;
        }
        isUpdateByUser = true;
        logUpDateHandler.post(new Runnable() {
            @Override
            public void run() {
                synchronized (mLogs) {
                    while (!mLogs.isEmpty()) {
                        String time = "" + SystemClock.uptimeMillis();
                        doUpdate(LogLevel.INFO + "-" + time);
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    isUpdateByUser = false;
                }
            }
        });
    }

    public int getCurrentLogSize() {
        synchronized (mLogs) {
            return mLogs.size();
        }
    }

    public void uploadCache() {
        logUpDateHandler.post(new Runnable() {
            @Override
            public void run() {
                List<LogCache> caches = LogCacheManager.getInstance().getLogCaches();
                if (caches.size() > 0) {
                    XLog.d(TAG + ":------------------uploadCache() called----------------------  size=" + caches.size());
                }
                for (LogCache logCache : caches) {
                    if (logCache != null && !TextUtils.isEmpty(logCache.getLogContent())) {
                        realUpdate(logCache.getHeader(), logCache.getLogContent(), logCache.getCacheKey());
                    }
                }
            }
        });
    }
}
