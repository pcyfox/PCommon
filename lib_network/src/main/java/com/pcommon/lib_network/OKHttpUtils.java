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
        } catch (IOException e) {
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
                Response temp = call.execute();
                if (temp != null) {

                    if (temp.isSuccessful()) {
                        ResponseBody body = temp.body();
                        //call string auto close body
                        callback.onResponse(call, temp);
                    } else {
                        callback.onFailure(call, new IOException("fail"));
                    }
                } else {
                    callback.onFailure(call, new IOException("fail"));
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


    public static void post(@NonNull String url, @NonNull String jsonData, Map<String, String> header, @NonNull Callback callback) {
        post(getOkHttpClient(), url, jsonData, header, callback);
    }


    public static String getSys(String url) {
        Request request = new Request.Builder().url(url).build();
        Call call = getOkHttpClient().newCall(request);
        Response response = null;
        try {
            response = call.execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (response != null) {
            if (response.isSuccessful()) {
                try {
                    return response.body().string();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
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

            if (interceptors != null && interceptors.length > 0) {
                for (Interceptor interceptor : interceptors) {
                    builder.addInterceptor(interceptor);
                }
            }
            okHttpClient = builder.build();
        }
        return okHttpClient;
    }


    /**
     * 使用okHttp进行同步post网络请求, Form表单格式请求
     *
     * @param url    请求地址
     * @param params 请求参数
     * @return
     * @throws IOException
     */
    public static Response post(String url, Map<String, String> params) throws Exception {
        FormBody.Builder builder = new FormBody.Builder();
        StringBuilder temp = new StringBuilder();
        if (params != null && params.size() > 0) {
            for (String key : params.keySet()) {
                String value = params.get(key);
                if (!TextUtils.isEmpty(value)) {
                    builder.add(key, params.get(key));
                    temp.append("\n   ").append(key).append(" --> ").append(params.get(key));
                }
            }
        }
        RequestBody body = builder.build();
        //RequestBody requestBody = RequestBody.create(JSON, String.valueOf(json));
        Request request = new Request.Builder().url(url).post(body).build();
        //AppLog.d(TAG, "Http Req --> " + url + temp);
        Response response = getOkHttpClient().newCall(request).execute();// execute(同步) enqueue(异步)

     /*   if(response)
        if (response.isSuccessful()) {
            String result = response.body().string();
            //AppLog.d(TAG, "Http Res --> " + result);
            return result;
        } else {
            throw new IOException("Unexpected code " + response);
        }*/
        return response;
    }


    /**
     * 下载文件
     *
     * @param fileUrl  文件url
     * @param filepath 文件绝对存储路径
     */
    public static void downLoadFile(String fileUrl, final String filepath, final GrabCallback callBack) {
  /*      final String fileName = MD5.encode(fileUrl);
        final File file = new File(destFileDir, fileName);
        if (file.exists()) {
            successCallBack((T) file, callBack);

            callBack.onResponse();
            return;
        }*/
        final File file = new File(filepath);
        final Request request = new Request.Builder().url(fileUrl).build();
        final Call call = getOkHttpClient().newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, e.toString());
                callBack.onFailure(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                InputStream is = null;
                byte[] buf = new byte[2048];
                int len = 0;
                FileOutputStream fos = null;
                try {
                    long total = response.body().contentLength();
                    Log.d(TAG, "total------>" + total);
                    long current = 0;
                    is = response.body().byteStream();
                    fos = new FileOutputStream(file);
                    while ((len = is.read(buf)) != -1) {
                        current += len;
                        fos.write(buf, 0, len);
                        Log.d(TAG, "current------>" + current);
                    }
                    fos.flush();
                    callBack.onSuccess(filepath);
                } catch (IOException e) {
                    Log.e(TAG, e.toString());
                    callBack.onFailure(e);
                } finally {
                    try {
                        if (is != null) {
                            is.close();
                        }
                        if (fos != null) {
                            fos.close();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, e.toString());
                    }
                }
            }
        });
    }


    public interface GrabCallback {
        void onFailure(Exception e);

        void onSuccess(String filePath);
    }
}
