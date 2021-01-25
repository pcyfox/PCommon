package com.pcommon.lib_common.update;

import androidx.annotation.Keep;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@Keep
public class DownloadUtils {

    static OkHttpClient okHttpClient;

    public static void downloadFile(final String url, final String savePath, final DownloadCallback callback) {
        final long startTime = System.currentTimeMillis();
        okHttpClient = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Connection", "close")
                .build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                if (callback != null) {
                    callback.downloaddFail(e.getMessage());
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                InputStream is = null;
                byte[] buf = new byte[2048];
                int len = 0;
                FileOutputStream fos = null;
                if (isFolderExists(savePath)) {
                    makeDirectory(savePath);
                }
                if (callback != null) {
                    callback.downloadStart();
                }
                try {
                    is = response.body().byteStream();
                    long total = response.body().contentLength();
                    String saveFile=url.substring(url.lastIndexOf("/") + 1);
                    if(saveFile.contains("?"))
                    {
                        saveFile=saveFile.replace("?","！");
                        saveFile=saveFile.split("！")[0];
                    }

                    File file = new File(savePath, saveFile);
                    fos = new FileOutputStream(file);
                    long sum = 0;
                    while ((len = is.read(buf)) != -1) {
                        fos.write(buf, 0, len);
                        sum += len;
                        int progress = (int) (sum * 1.0f / total * 100);
                        if (callback != null) {
                            callback.downloadProgress(progress);
                        }
                    }
                    fos.flush();
                    if (callback != null) {
                        callback.downloadFinish(file.getAbsolutePath());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (callback != null) {
                        callback.downloaddFail(e.getMessage());
                    }
                } finally {
                    try {
                        if (is != null)
                            is.close();
                    } catch (IOException e) {
                    }
                    try {
                        if (fos != null)
                            fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }


    public interface DownloadCallback {
        void downloadStart();

        void downloadProgress(int progress);

        void downloadFinish(String fileAbsPath);

        void downloaddFail(String mag);
    }


    public static void clearTask() {
        if (okHttpClient != null) {
            try {
                okHttpClient.dispatcher().cancelAll();//移除所有任务

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }


    public static boolean isFolderExists(String path) {
        if (path == null) {
            return false;
        }
        File file = new File(path);
        if (file.exists()) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean makeDirectory(String path) {
        if (path == null) {
            return false;
        }
        File file = new File(path);
        if (file.exists()) {
            return true;
        } else {
            return file.mkdir();
        }
    }

}
