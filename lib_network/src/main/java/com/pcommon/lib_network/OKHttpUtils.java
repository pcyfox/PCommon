package com.pcommon.lib_network;


import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.EOFException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

import static android.content.ContentValues.TAG;

/**
 * description ： 可直接用于网络请求的工具类
 * author : LN
 * date : 2019-10-25 11:10
 */
public class OKHttpUtils {
    private static OkHttpClient okHttpClient;
    private final static MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    public static void get(@NonNull String url, @NonNull Callback callback) {
        final OkHttpClient client = new OkHttpClient();
        try {
            Cache cache = client.cache();
            if (cache != null) {
                cache.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Request request = new Request.Builder()
                .url(url)
                .build();
        client.newCall(request).enqueue(callback);
    }


    public static void get(@NonNull String url, @Nullable Map<String, String> params, @Nullable Map<String, String> header, @NonNull Callback callback) {
        get(getOkHttpClient(), url, params, header, false, callback);
    }

    /**
     * 同步请求
     *
     * @param url
     * @param params
     * @param header
     * @param callback
     */
    public static void syncGet(@NonNull String url, @Nullable Map<String, String> params, @Nullable Map<String, String> header, @NonNull Callback callback) {
        get(getOkHttpClient(), url, params, header, true, callback);
    }


    public static void syncGet(@NonNull String url, @NonNull Callback callback) {
        get(getOkHttpClient(), url, null, null, true, callback);
    }


    public static void get(@NonNull OkHttpClient client, @NonNull String url, @Nullable Map<String, String> params, @Nullable Map<String, String> header, boolean sync, @NonNull Callback callback) {
        Request.Builder reqBuild = new Request.Builder();
        HttpUrl httpUrl = HttpUrl.parse(url);
        if (httpUrl == null) {
            throw new IllegalArgumentException(" can't parse url:" + url);
        }
        HttpUrl.Builder urlBuilder = httpUrl.newBuilder();
        if (params != null) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                urlBuilder.addQueryParameter(entry.getKey(), entry.getValue());
            }
        }
        if (header != null) {
            for (Map.Entry<String, String> entry : header.entrySet()) {
                reqBuild.addHeader(entry.getKey(), entry.getValue());
            }
        }
        reqBuild.url(urlBuilder.build());
        Request request = reqBuild.build();
        if (sync) {
            try {
                Call call = client.newCall(request);
                Response response = call.execute();
                if (response.isSuccessful()) {
                    callback.onResponse(call, response);
                } else {
                    callback.onFailure(call, new IOException("call  onFinish fail"));
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            client.newCall(request).enqueue(callback);
        }

    }

    public static void get(@NonNull OkHttpClient client, @NonNull String url, @NonNull Callback callback) {
        Request request = new Request.Builder().url(url).build();
        client.newCall(request).enqueue(callback);
    }

    public static void syncGet(@NonNull OkHttpClient client, @NonNull String url, @NonNull Callback callback) {
        Request request = new Request.Builder().url(url).build();
        try {
            client.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void post(@NonNull OkHttpClient client, @NonNull String url, Map<String, String> param, Map<String, String> header, @NonNull Callback callback) {
        Request.Builder builder = new Request.Builder().url(url);
        RequestBody body = null;
        if (param != null) {
            FormBody.Builder formBodyBuilder = new FormBody.Builder();
            for (Map.Entry<String, String> entry : param.entrySet()) {
                formBodyBuilder.add(entry.getKey(), entry.getValue());
            }
            body = formBodyBuilder.build();
        }

        if (header != null) {
            for (Map.Entry<String, String> entry : header.entrySet()) {
                builder.addHeader(entry.getKey(), entry.getValue());
            }
        }
        if (body != null) {
            builder.post(body);
        }
        client.newCall(builder.build()).enqueue(callback);
    }


    public static void post(@NonNull String url, Map<String, String> param, Map<String, String> header, @NonNull Callback callback) {
        post(getOkHttpClient(), url, param, header, callback);
    }


    public static void post(@NonNull OkHttpClient client, @NonNull String url, @NonNull String jsonData, Map<String, String> header, @NonNull Callback callback) {
        if (TextUtils.isEmpty(url) || TextUtils.isEmpty(jsonData)) {
            callback.onFailure(null, new EOFException("param error"));
            return;
        }
        RequestBody requestBody = RequestBody.create(JSON, jsonData);
        Request.Builder builder = new Request.Builder().url(url).post(requestBody);
        if (header != null) {
            for (Map.Entry<String, String> entry : header.entrySet()) {
                builder.addHeader(entry.getKey(), entry.getValue());
            }
        }
        client.newCall(builder.build()).enqueue(callback);
    }

    public static Response post(String url, Map<String, String> params) throws Exception {
        FormBody.Builder builder = new FormBody.Builder();
        if (params != null && params.size() > 0) {
            for (Map.Entry<String, String> p : params.entrySet()) {
                builder.add(p.getKey(), p.getValue());
            }
        }
        RequestBody body = builder.build();
        Request request = new Request.Builder().url(url).post(body).build();
        return getOkHttpClient().newCall(request).execute();
    }


    public static void post(@NonNull String url, @NonNull String jsonData, Map<String, String> header, @NonNull Callback callback) {
        post(getOkHttpClient(), url, jsonData, header, callback);
    }


    public static String getSys(String url) {
        Request request = new Request.Builder().url(url).build();
        Call call = getOkHttpClient().newCall(request);
        try {
            Response response = call.execute();
            if (response.isSuccessful()) {
                return response.body().string();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static OkHttpClient getOkHttpClient(Interceptor... interceptors) {
        if (okHttpClient == null) {
            OkHttpClient.Builder builder = new OkHttpClient.Builder()
                    .retryOnConnectionFailure(true)//默认重试一次，若需要重试N次，则要实现拦截器。
                    .connectTimeout(20, TimeUnit.SECONDS)
                    .readTimeout(20, TimeUnit.SECONDS)
                    .writeTimeout(20, TimeUnit.SECONDS);

            if (interceptors != null) {
                for (Interceptor interceptor : interceptors) {
                    builder.addInterceptor(interceptor);
                }
            }
            okHttpClient = builder.build();
        }
        return okHttpClient;
    }


}
