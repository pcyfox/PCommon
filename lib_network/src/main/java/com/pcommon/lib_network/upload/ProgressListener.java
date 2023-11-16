package com.pcommon.lib_network.upload;

public interface ProgressListener {
    void onProgress(long currentBytes, long totalBytes, long constTime, String file, Object tag);
}