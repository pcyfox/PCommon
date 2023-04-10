package com.pcommon.lib_network;

import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class RetryInterceptor implements Interceptor {
    private static final String TAG = "RetryInterceptor";
    private final int maxRetry;//最大重试次数

    public RetryInterceptor(int maxRetry) {
        Log.d(TAG, "RetryInterceptor() called with: maxRetry = [" + maxRetry + "]");
        this.maxRetry = maxRetry;
    }

    @NonNull
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Response response = doRequest(chain, request);
        if (maxRetry <= 1) return response;
        int tryCount = 1;
        while (response == null && tryCount <= maxRetry) {
            tryCount++;
            Log.w(TAG, "http重试次数:" + tryCount + ",url:" + chain.request().url());
            try {
                Thread.sleep(200L * tryCount);
            } catch (InterruptedException ignored) {
            }
            response = doRequest(chain, request);
        }
        if (response == null) {
            throw new IOException();
        }
        return response;
    }


    private Response doRequest(Chain chain, Request request) {
        Response response = null;
        try {
            response = chain.proceed(request);
        } catch (Exception e) {
            Log.e(TAG, "doRequest() called fail,exception:" + e.getMessage());
        }
        return response;
    }
}