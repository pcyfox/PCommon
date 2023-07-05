package com.pcommon.lib_network;


import androidx.annotation.NonNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import okhttp3.Callback;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import retrofit2.Converter;
import retrofit2.Retrofit;

/**
 * description ： Retrofit网络请求封装基类
 * author : LN
 * date : 2019-10-25 11:10
 */
public abstract class AbsRequest {
    static final Object lock = new Object();
    private Set<Converter.Factory> converterFactories;
    private Set<Interceptor> interceptors;
    protected String baseUrl;
    private OkHttpClient httpClient;
    protected Retrofit retrofit;
    //标记关于URL的网络请求日志能否上传
    protected volatile WeakHashMap<String, Boolean> uploadLogRequests = new WeakHashMap<>();

    public abstract OkHttpClient.Builder createOkHttpClientBuilder();

    public abstract Retrofit.Builder createRetrofitBuilder();


    AbsRequest() {
        converterFactories = new HashSet<>();
    }

    public void addUpdateLogRequests(String url, Boolean isUpdate) {
        synchronized (lock) {
            uploadLogRequests.put(url, isUpdate);
        }
    }

    void buildHttpClient() {
        OkHttpClient.Builder builder = createOkHttpClientBuilder();
        if (interceptors != null) {
            for (Interceptor interceptor : interceptors) {
                builder.addInterceptor(interceptor);
            }
        }
        httpClient = builder.build();
    }


    private Retrofit buildRetrofit() {
        Retrofit.Builder builder = createRetrofitBuilder();
        for (Converter.Factory factory : converterFactories) {
            builder.addConverterFactory(factory);
        }
        buildHttpClient();
        builder.client(httpClient);
        return builder.build();
    }

    public <T> T create(String baseUrl, final Class<T> service) {
        if (retrofit == null || !this.baseUrl.equals(baseUrl)) {
            retrofit = buildRetrofit();
        }
        return retrofit.create(service);
    }

    public <T> T create(final Class<T> service) {
        if (retrofit == null) {
            retrofit = buildRetrofit();
        }
        return retrofit.create(service);
    }

    public OkHttpClient getHttpClient() {
        if (httpClient == null) {
            buildHttpClient();
        }
        return httpClient;
    }

    public Retrofit getRetrofit() {
        return retrofit;
    }

    public Set<Interceptor> getInterceptors() {
        return interceptors;
    }


    public void cleaConverterFactories() {
        converterFactories = new HashSet<>();
    }

    public void addConverterFactory(Converter.Factory... factories) {
        converterFactories.addAll(Arrays.asList(factories));
    }

    public void clearInterceptor() {
        if (interceptors != null) {
            interceptors.clear();
        } else {
            interceptors = new HashSet<>();
        }
    }

    public void addInterceptor(Interceptor... interceptors) {
        this.interceptors.addAll(Arrays.asList(interceptors));
    }

    public void asyncGet(@NonNull String url, Callback callback, boolean isRecordLog) {
        uploadLogRequests.put(url, isRecordLog);
        OKHttpUtils.get(getHttpClient(), url, callback);
    }

    public void asyncGet(@NonNull String url, Callback callback) {
        asyncGet(url, callback, true);
    }

    public void asyncGet(@NonNull String url, Map<String, String> params, Map<String, String> header, boolean isRecordLog, Callback callback) {
        uploadLogRequests.put(url, isRecordLog);
        OKHttpUtils.get(getHttpClient(), url, params, header, true, callback);
    }

    public void asyncGet(@NonNull String url, Map<String, String> params, Map<String, String> header, Callback callback) {
        asyncGet(url, params, header, true, callback);
    }


    public void asyncPost(@NonNull String url, Map<String, String> params, Map<String, String> header, boolean isRecordLog, Callback callback) {
        uploadLogRequests.put(url, isRecordLog);
        OKHttpUtils.post(getHttpClient(), url, params, header, callback);
    }

    public void asyncPost(@NonNull String url, Map<String, String> params, Map<String, String> header, Callback callback) {
        asyncPost(url, params, header, true, callback);
    }


    public void asyncPost(@NonNull String url, String jsonContent, Map<String, String> header, boolean isRecordLog, Callback callback) {
        uploadLogRequests.put(url, isRecordLog);
        OKHttpUtils.post(getHttpClient(), url, jsonContent, header, callback);
    }

    public void asyncPost(@NonNull String url, String jsonContent, Map<String, String> header, Callback callback) {
        asyncPost(url, jsonContent, header, true, callback);
    }

}
