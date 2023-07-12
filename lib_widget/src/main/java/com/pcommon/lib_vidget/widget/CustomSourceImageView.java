package com.pcommon.lib_vidget.widget;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;

import androidx.annotation.Nullable;

import com.blankj.utilcode.util.PermissionUtils;
import com.bumptech.glide.Glide;

import java.io.File;

public class CustomSourceImageView extends androidx.appcompat.widget.AppCompatImageView {
    private static final String TAG = "CustomSourceImageView";
    private static String defDir = "";

    public CustomSourceImageView(Context context) {
        super(context);
    }

    public CustomSourceImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomSourceImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public static void setDefLoadLogoDir(String defDir) {
        CustomSourceImageView.defDir = defDir;
    }

    public static String getDefLoadLogoDir() {
        return defDir;
    }


    {
        if (TextUtils.isEmpty(defDir)) {
            defDir = getContext().getExternalCacheDir().getAbsolutePath() + File.separator + "CompanyLogo";
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        postDelayed(() -> {
            try {
                loadImg();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 50);
    }


    public void loadImg(String path) {
        Log.d(TAG, "loadImg() called with: path = [" + path + "]");
        defDir = path;
        if (path.isEmpty()) return;
        if (path.startsWith("http")) {
            Glide.with(this).load(path).into(this);
            return;
        }

        File f = new File(path);
        if (!f.exists()) {
            f.mkdirs();
            return;
        }

        if (f.isDirectory()) {
            File[] images = f.listFiles();
            if (images == null || images.length == 0) {
                return;
            }
            for (File img : images) {
                String name = img.getName().toLowerCase();
                if (name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".webp")) {
                    Glide.with(this).load(img).into(this);
                    break;
                }
            }
            return;
        }
        Glide.with(this).load(path).into(this);
    }

    public void loadImg() {
        loadImg(defDir);
    }
}
