package com.pcommon.lib_network;


import android.text.TextUtils;
import android.util.Log;

import com.elvishew.xlog.XLog;
import com.pcommon.lib_network.log.Filter;
import com.pcommon.lib_network.log.HeaderInterceptor;
import com.pcommon.lib_network.log.HttpLogger;
import com.pcommon.lib_network.log.LogFilter;
import com.pcommon.lib_network.log.MyHttpLoggingInterceptor;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class RequestManager extends AbsRequest {
    private static final String TAG = "RequestManager";
    private static final RequestManager requestManager = new RequestManager();
    private HeaderInterceptor headerInterceptor;
    private OkHttpClient.Builder okhttpBuilder;

    private RequestManager() {
        super();
    }

    @Override
    public OkHttpClient.Builder createOkHttpClientBuilder() {
        if (okhttpBuilder != null) {
            return okhttpBuilder;
        }
        return new OkHttpClient().newBuilder()
                .retryOnConnectionFailure(false)//默认重试一次，若需要重试N次，则要实现拦截器。
                .dns(new OkHttpDns(8L))
                .connectTimeout(6, TimeUnit.SECONDS)
                .readTimeout(6, TimeUnit.SECONDS)
                .writeTimeout(6, TimeUnit.SECONDS);
    }


    @Override
    public Retrofit.Builder createRetrofitBuilder() {
        return new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create());
    }

    public static RequestManager get() {
        return requestManager;
    }


    public void iniRetrofit(String clientId, String baseUrl, String appVersionCode, String appVersionName, String pkgName) {
        XLog.i(TAG + ":iniRetrofit() called with: clientId = [" + clientId + "], baseUrl = [" + baseUrl + "], appVersionCode = [" + appVersionCode + "], pkgName = [" + pkgName + "]");
        if (TextUtils.isEmpty(baseUrl)) {
            XLog.e("iniRetrofit fail, bad baseUrl+" + baseUrl);
            return;
        }
        headerInterceptor = new HeaderInterceptor();
        headerInterceptor.setDeviceId(clientId);
        headerInterceptor.setAppVersionCode(appVersionCode);
        headerInterceptor.setPkgName(pkgName);
        headerInterceptor.setAppVersionName(appVersionName);
        headerInterceptor.addHeader("Connection", "close");
        loggingInterceptor.setLevel(MyHttpLoggingInterceptor.Level.BODY);
        loggingInterceptor.setCareHeaders("uid", "token", "device-id", "token");

        this.baseUrl = baseUrl;
        retrofit = null;

        clearInterceptor();
        addInterceptor(loggingInterceptor, headerInterceptor);
        cleaConverterFactories();
        addConverterFactory(GsonConverterFactory.create());
        buildHttpClient();
    }

    public void setAuthorization(String authorization) {
        Log.d(TAG, "setAuthorization() called with: authorization = [" + authorization + "]");
        if (headerInterceptor == null) {
            return;
        }
        headerInterceptor.setAuthorization(authorization);
    }

    public void setUidToken(String uid) {
        headerInterceptor.setUid(uid);
    }


    public Map<String, String> getHeader() {
        if (headerInterceptor == null) {
            return null;
        }
        return headerInterceptor.getHeadMap();
    }

    public void addHeader(String key, String value) {
        if (headerInterceptor == null) {
            return;
        }
        headerInterceptor.getHeadMap().put(key, value);
    }

    /**
     * 设置请求头过滤器
     *
     * @param filter
     */
    public void setHeaderInterceptorFilter(Filter filter) {
        if (headerInterceptor != null) {
            headerInterceptor.setFilter(filter);
        }
    }


    public void setHttpLoggingInterceptor(LogFilter filter) {
        if (loggingInterceptor != null) {
            loggingInterceptor.setFilter(filter);
        }
    }


    public void setOkhttpBuilder(OkHttpClient.Builder okhttpBuilder) {
        this.okhttpBuilder = okhttpBuilder;
    }

    private final MyHttpLoggingInterceptor loggingInterceptor = new MyHttpLoggingInterceptor(new HttpLogger() {
        @Override
        public void log(String url, String message) {
            //将所有请求日志交给XLog处理
            if (!TextUtils.isEmpty(message)) {
                try {
                    if (checkForUpload(url)) {
                        XLog.i("XRetrofitLog: " + message);
                    } else {
                        Log.d("RetrofitLog: ", message);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
            , new LogFilter() {//日志过滤处理
        @Override
        public String filter(String url, String log) {
            return log;
        }
    });

    private boolean checkForUpload(String url) {
        String matchUrl = null;
        synchronized (lock) {
            for (String key : uploadLogRequests.keySet()) {
                if (url.contains(key)) {
                    matchUrl = key;
                    break;
                }
            }
            if (matchUrl != null) {
                synchronized (lock) {
                    Boolean isNeedUpdate = uploadLogRequests.get(matchUrl);
                    return isNeedUpdate != null && isNeedUpdate;
                }
            }
        }
        return true;
    }
}