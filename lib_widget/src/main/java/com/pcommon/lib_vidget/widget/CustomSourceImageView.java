package com.pcommon.lib_vidget.widget;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.text.TextUtils;
import android.util.AttributeSet;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;

import java.io.File;

public class CustomSourceImageView extends androidx.appcompat.widget.AppCompatImageView {
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

    public static synchronized String getAppName(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            int labelRes = packageInfo.applicationInfo.labelRes;
            if (labelRes <= 0) {
                String[] names = packageInfo.packageName.split("\\.");
                return names[names.length - 1];
            }
            return context.getResources().getString(labelRes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void setDefDir(String defDir) {
        CustomSourceImageView.defDir = defDir;
    }


    public static String getDefLoadImgDir(Context context) {
        if (!TextUtils.isEmpty(defDir)) {
            return defDir;
        }
        return Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + getAppName(context) + File.separator + "CompanyLogo";
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
        }, 10);
    }


    private void loadImg() {
        File dir = new File(getDefLoadImgDir(getContext()));
        if (dir.exists()) {
            File[] images = dir.listFiles();
            if (images != null && images.length > 0) {
                for (File img : images) {
                    String name = img.getName().toLowerCase();
                    if (name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".webp")) {
                        Glide.with(this).load(img).into(this);
                        break;
                    }
                }
            }
        }
    }
}
