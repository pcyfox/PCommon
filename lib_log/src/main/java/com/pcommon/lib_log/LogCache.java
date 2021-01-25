package com.pcommon.lib_log;

import java.util.Map;

public class LogCache {
    private static final String TAG = "LogCache";
    private Map<String, String> header;
    private String logContent;

    private  String cacheKey;

    public LogCache(Map<String, String> header, String logContent, String cacheKey) {
        this.header = header;
        this.logContent = logContent;
        this.cacheKey = cacheKey;
    }

    public Map<String, String> getHeader() {
        return header;
    }

    public void setHeader(Map<String, String> header) {
        this.header = header;
    }

    public String getLogContent() {
        return logContent;
    }

    public void setLogContent(String logContent) {
        this.logContent = logContent;
    }

    public String getCacheKey() {
        return cacheKey;
    }

    public void setCacheKey(String cacheKey) {
        this.cacheKey = cacheKey;
    }

    @Override
    public String toString() {
        return "LogCache{" +
                "header=" + header +
                ", logContent='" + logContent + '\'' +
                '}';
    }
}
