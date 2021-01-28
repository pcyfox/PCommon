package com.pcommon.lib_network.log;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class HeaderInterceptor implements Interceptor {
    private Map<String, String> mHeadMap;
    private Filter filter;
    private static final String TAG = "HeaderInterceptor";

    public HeaderInterceptor() {
        mHeadMap = new HashMap<>();
        mHeadMap.put("Content-Type", "application/json;charset=UTF-8");
    }

    @NotNull
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request.Builder builder = chain.request().newBuilder();
        String encodedPath = chain.request().url().toString();
        if (filter != null && filter.filter(encodedPath)) {
            // Log.d(TAG, "intercept() called with: filter = [" + true + "]");
            return chain.proceed(builder.build());
        }
        //  Log.d(TAG, "intercept() called with: mHeadMap = [" + mHeadMap + "]");
        for (String key : mHeadMap.keySet()) {
            String value = mHeadMap.get(key);
            if (value != null) {
                builder.addHeader(key, value);
            }
        }

        return chain.proceed(builder.build());
    }

    public void setDeviceId(String clientId) {
        mHeadMap.put("device-id", clientId);
    }


    public void setPkgName(String pkgName) {
        mHeadMap.put("pkg-name", pkgName);
    }

    public void setAppVersionCode(String code) {
        mHeadMap.put("app-version-code", code);
    }

    public void setAppVersionName(String name) {
        mHeadMap.put("app-version-name", name);
    }

    public void setUid(String uid) {
        mHeadMap.put("uid", uid);
    }

    public void setAuthorization(String authorization) {
        mHeadMap.put("authorization", authorization);
    }

    public void addHeader(String key, String value) {
        mHeadMap.put(key, value);
    }

    public void setFilter(Filter filter) {
        this.filter = filter;
    }

    public Map<String, String> getHeadMap() {
        return mHeadMap;
    }
}
