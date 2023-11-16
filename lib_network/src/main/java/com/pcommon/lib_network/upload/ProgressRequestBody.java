package com.pcommon.lib_network.upload;

import androidx.annotation.Nullable;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.ForwardingSink;
import okio.Okio;

public class ProgressRequestBody extends RequestBody {
    private final RequestBody requestBody;
    private final ProgressListener progressListener;
    private final String file;
    private final Object tag;

    public ProgressRequestBody(RequestBody requestBody, ProgressListener progressListener, String file, Object tag) {
        this.file = file;
        this.tag = tag;
        this.requestBody = requestBody;
        this.progressListener = progressListener;
    }

    @Nullable
    @Override
    public MediaType contentType() {
        return requestBody.contentType();
    }

    @Override
    public long contentLength() throws IOException {
        return requestBody.contentLength();
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        BufferedSink bufferedSink = Okio.buffer(new ForwardingSink(sink) {
            private long bytesWritten = 0L;
            private long contentLength = 0L;
            private long startTime = 0L;

            @Override
            public void write(Buffer source, long byteCount) throws IOException {
                super.write(source, byteCount);
                if (contentLength == 0) {
                    contentLength = contentLength();
                    startTime = System.currentTimeMillis();
                }
                bytesWritten += byteCount;
                progressListener.onProgress(bytesWritten, contentLength, System.currentTimeMillis() - startTime, file, tag);
            }
        });
        requestBody.writeTo(bufferedSink);
        bufferedSink.flush();
    }
}