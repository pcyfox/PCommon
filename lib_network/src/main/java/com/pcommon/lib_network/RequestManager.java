package com.pcommon.lib_network;


import android.text.TextUtils;
import android.util.Log;

import com.elvishew.xlog.XLog;
import com.pcommon.lib_network.log.Filter;
import com.pcommon.lib_network.log.HeaderInterceptor;
import com.pcommon.lib_network.log.LogFilter;
import com.pcommon.lib_network.log.MyHttpLoggingInterceptor;

import java.util.Arrays;
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
    private int[] timeOuts = {5, 5, 5, 5};

    private RequestManager() {
        super();
    }

    @Override
    public OkHttpClient.Builder createOkHttpClientBuilder() {
        if (timeOuts == null || timeOuts.length < 4) {
            throw new IllegalArgumentException("time out params error");
        }
        return new OkHttpClient().newBuilder().retryOnConnectionFailure(false)//默认重试一次，若需要重试N次，则要实现拦截器。
                .dns(new OkHttpDns(timeOuts[0])).connectTimeout(timeOuts[1], TimeUnit.SECONDS).readTimeout(timeOuts[2], TimeUnit.SECONDS).writeTimeout(timeOuts[3], TimeUnit.SECONDS);
    }

    @Override
    public Retrofit.Builder createRetrofitBuilder() {
        return new Retrofit.Builder().baseUrl(baseUrl).addCallAdapterFactory(RxJava2CallAdapterFactory.create());
    }

    public static RequestManager get() {
        return requestManager;
    }


    public void iniRetrofit(String clientId, String baseUrl, String appVersionCode, String appVersionName, String pkgName, int retryTime, int[] timeOuts) {
        Log.d(TAG, "iniRetrofit() called with: clientId = [" + clientId + "], baseUrl = [" + baseUrl + "], appVersionCode = [" + appVersionCode + "], appVersionName = [" + appVersionName + "], pkgName = [" + pkgName + "], retryTime = [" + retryTime + "], timeOuts = [" + Arrays.toString(timeOuts) + "]");
        iniRetrofit(clientId, baseUrl, appVersionCode, appVersionName, pkgName, retryTime);
        this.timeOuts = timeOuts;
    }


    public void iniRetrofit(String clientId, String baseUrl, String appVersionCode, String appVersionName, String pkgName, int retryTime) {
        Log.d(TAG, "iniRetrofit() called with: clientId = [" + clientId + "], baseUrl = [" + baseUrl + "], appVersionCode = [" + appVersionCode + "], appVersionName = [" + appVersionName + "], pkgName = [" + pkgName + "], retryTime = [" + retryTime + "]");
        this.baseUrl = baseUrl;
        retrofit = null;
        headerInterceptor = new HeaderInterceptor();
        headerInterceptor.setDeviceId(clientId);
        headerInterceptor.setAppVersionCode(appVersionCode);
        headerInterceptor.setPkgName(pkgName);
        headerInterceptor.setAppVersionName(appVersionName);
        headerInterceptor.addHeader("Connection", "close");
        loggingInterceptor.setLevel(MyHttpLoggingInterceptor.Level.BODY);
        loggingInterceptor.setCareHeaders("uid", "token", "device-id", "token", "authorization");
        iniRetrofit(baseUrl, retryTime, timeOuts, headerInterceptor, loggingInterceptor);
    }

    public void iniRetrofit(String baseUrl, int retryTime) {
        iniRetrofit(baseUrl, retryTime, null);
    }

    public void iniRetrofit(String baseUrl, int retryTime, int[] timeOuts) {
        iniRetrofit(baseUrl, retryTime, timeOuts, null, null);
    }

    public void iniRetrofit(String baseUrl, int retryTime, int[] timeOuts, HeaderInterceptor headerInterceptor, MyHttpLoggingInterceptor loggingInterceptor) {
        Log.d(TAG, "iniRetrofit() called with: baseUrl = [" + baseUrl + "], retryTime = [" + retryTime + "], timeOuts = [" + timeOuts + "], headerInterceptor = [" + headerInterceptor + "], loggingInterceptor = [" + loggingInterceptor + "]");
        if (TextUtils.isEmpty(baseUrl)) {
            Log.e(TAG, "iniRetrofit() called fail!,with: baseUrl = [" + baseUrl + "]");
            return;
        }
        this.baseUrl = baseUrl;
        if (null != timeOuts) {
            this.timeOuts = timeOuts;
        }
        retrofit = null;
        clearInterceptor();
        if (retryTime > 0) addInterceptor(new RetryInterceptor(retryTime));

        if (null != headerInterceptor) {
            this.headerInterceptor = headerInterceptor;
            addInterceptor(headerInterceptor);
        }
        if (null != loggingInterceptor) addInterceptor(loggingInterceptor);

        cleaConverterFactories();
        addConverterFactory(GsonConverterFactory.create());
        buildHttpClient();
    }


    public boolean isInitOver() {
        return !TextUtils.isEmpty(baseUrl) && null != retrofit;
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
        loggingInterceptor.setFilter(filter);
    }

    //日志过滤处理
    private final MyHttpLoggingInterceptor loggingInterceptor = new MyHttpLoggingInterceptor((url, message) -> {
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
    }, (url, log) -> log);

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