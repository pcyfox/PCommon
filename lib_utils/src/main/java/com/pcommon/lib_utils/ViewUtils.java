package com.pcommon.lib_utils;

import android.graphics.Bitmap;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.WorkerThread;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ViewUtils {
    private ViewUtils() {
    }

    public static Bitmap createBitmap(View view) {
        if (view == null) return null;
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();  //启用DrawingCache并创建位图
        Bitmap cache = view.getDrawingCache();
        if (cache == null || cache.isRecycled()) {
            return null;
        }
        Bitmap bitmap = Bitmap.createBitmap(cache); //创建一个DrawingCache的拷贝，因为DrawingCache得到的位图在禁用后会被回收
        view.setDrawingCacheEnabled(false);  //禁用DrawingCahce否则会影响性能
        return bitmap;
    }

    public static void saveImage(final String path, final Bitmap bitmap, final OnSaveListener listener) {
        if (bitmap != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        saveToLocal(bitmap, path);
                        if (listener != null) {
                            listener.onSaveFinish();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    @WorkerThread
    public static void saveToLocal(Bitmap bitmap, String filepath) throws IOException {
        File file = new File(filepath);
        if (file.exists()) {
            file.delete();
        }
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            if (bitmap.compress(Bitmap.CompressFormat.PNG, 90, out)) {
                out.flush();
                out.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    public interface OnSaveListener {
        void onSaveFinish();
    }

    public static void addBottomLine(TextView textView) {
        if (textView != null) {
            String text = textView.getText().toString();
            SpannableString spannableString = new SpannableString(text);
            UnderlineSpan underlineSpan = new UnderlineSpan();
            spannableString.setSpan(underlineSpan, 0, spannableString.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            textView.setText(spannableString);
        }
    }

    public static void clearBottomLine(TextView textView) {
        if (textView != null) {
            String text = textView.getText().toString();
            textView.setText(text );
        }
    }


}
