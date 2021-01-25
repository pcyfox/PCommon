package com.pcommon.lib_glide;

import android.content.Context;
import android.os.Looper;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.module.AppGlideModule;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.pcommon.lib_network.OKHttpUtils;
import com.pcommon.lib_network.RequestManager;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

@GlideModule
public class HttpGlideModule extends AppGlideModule {
    private static final String TAG = "HttpGlideModule";
    private static final String FILTER_FLAG = "/oss/";
    private static final WeakHashMap<String, URLRecord> recordCache = new WeakHashMap<>();
    private final static int OSS_LIVE_TIME = 30 * 60 * 1000;//OSS服务设定有效时长为30分钟
    private final static int AUTO_START_CLEAN_COUNT = 200;

    @Override
    public void registerComponents(@NonNull Context context, @NonNull Glide glide, @NonNull Registry registry) {
        OkHttpUrlLoader.Factory factory = new OkHttpUrlLoader.Factory(OKHttpUtils.getOkHttpClient(new Interceptor() {
            @Override
            public Response intercept(final Chain chain) throws IOException {

                final HttpUrl httpUrl = chain.request().url();
                final String url = httpUrl.url().toString();

                if (url.contains(FILTER_FLAG)) {
                    URLRecord record = recordCache.get(url);
                    if (record != null && !TextUtils.isEmpty(record.realUrl)) {
                        return createResponse(record.realUrl, chain);
                    }

                    URLRecord urlRecord = new URLRecord();
                    urlRecord.startRequestTime = SystemClock.uptimeMillis();
                    recordCache.put(url, urlRecord);
                    RequestManager.get().asyncGet(url, new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            setRecordStateERROR(url);
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            try {
                                ResponseBody responseBody = response.body();
                                if (responseBody == null) {
                                    return;
                                }
                                String bodyString = responseBody.string();
                                Type type = new TypeToken<BaseRespEntity<String>>() {
                                }.getType();
                                BaseRespEntity<String> resp = new Gson().fromJson(bodyString, type);
                                if (resp.isOK()) {
                                    URLRecord urlRecord = recordCache.get(url);
                                    if (urlRecord != null) {
                                        urlRecord.realUrl = resp.getData();
                                        urlRecord.responseRealUrlTime = SystemClock.uptimeMillis();
                                        urlRecord.state = URLRecord.STATE_OK;
                                        Log.d(TAG, "onResponse() called :get url success! real url  = [" + urlRecord.realUrl + "]");
                                    }
                                } else {
                                    setRecordStateERROR(url);
                                    if (BuildConfig.DEBUG) {
                                        Log.e(TAG, "onResponse() called with: resp = [${resp.message}]");
                                    }
                                }
                            } catch (Exception e) {
                                setRecordStateERROR(url);
                                e.printStackTrace();
                            }
                        }
                    });

                    //自旋等待结果
                    while (recordCache.get(url) != null) {
                        if (Looper.myLooper() != Looper.getMainLooper()) {
                            try {
                                URLRecord cache = recordCache.get(url);
                                if (cache != null) {//执行过过请求，但未成功，失败次数愈多等待时间越短，避免影响后面的任务
                                    Thread.sleep(130 - 2 * cache.requestCount);
                                } else {
                                    Thread.sleep(130);
                                }

                                //请求失败超过30次或者请求超过15秒就放弃该次请求
                                if (cache == null || cache.requestCount > 30 || SystemClock.uptimeMillis() - cache.startRequestTime > 15 * 1000) {
                                    break;
                                }
                                cache.requestCount++;
                                if (URLRecord.STATE_OK == urlRecord.state) {
                                    String realUrl = cache.realUrl;
                                    if (TextUtils.isEmpty(realUrl)) {
                                        return chain.proceed(chain.request());
                                    }
                                    autoCleanCache();
                                    return createResponse(realUrl, chain);
                                }
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        } else {
                            Log.e(TAG, "Error Glide load url in ui Thread!");
                        }
                    }
                }

                return chain.proceed(chain.request());
            }
        }));

        registry.replace(GlideUrl.class, InputStream.class, factory);
    }

    private void autoCleanCache() {
        if (recordCache.size() > AUTO_START_CLEAN_COUNT) {//清理过期的数据
            if (BuildConfig.DEBUG)
                Log.d(TAG, "autoCleanCache() start clean size=" + recordCache.size());

            Iterator<Map.Entry<String, URLRecord>> iterator = recordCache.entrySet().iterator();
            while (iterator.hasNext()) {
                if (iterator.next().getValue().isTimeout()) {
                    iterator.remove();
                }
            }

            if (BuildConfig.DEBUG)
                Log.d(TAG, "autoCleanCache() end clean size=" + recordCache.size());

            if (recordCache.size() > AUTO_START_CLEAN_COUNT) {//清理过期的数据后任然超标（说明缓存过热）开启暴力清理
                int i = 0;
                //清理四分之一 todo:使用LRU效果更佳
                while (iterator.hasNext() && i < AUTO_START_CLEAN_COUNT / 4) {
                    iterator.remove();
                    i++;
                }
            }
        }

    }

    private void setRecordStateERROR(String url) {
        URLRecord urlRecord = recordCache.get(url);
        if (urlRecord == null) {
            return;
        }
        urlRecord.state = URLRecord.STATE_ERROR;
        urlRecord.realUrl = "";
        urlRecord.responseRealUrlTime = SystemClock.uptimeMillis();
    }


    private Response createResponse(String url, Interceptor.Chain chain) throws IOException {
        HttpUrl httpUrl = chain.request().url();
        Request.Builder builder = chain.request().newBuilder().url(httpUrl.resolve(url));
        return chain.proceed(builder.build());
    }

    @Keep
    private static class URLRecord {
        public static final int STATE_ERROR = 0;
        public static final int STATE_START = 1;
        public static final int STATE_OK = 2;

        String realUrl;
        long startRequestTime;
        long responseRealUrlTime;
        volatile int state = STATE_START;
        int requestCount;

        public boolean isTimeout() {
            return SystemClock.uptimeMillis() - responseRealUrlTime > OSS_LIVE_TIME - 0.5 * 60 * 1000;
        }
    }

}
