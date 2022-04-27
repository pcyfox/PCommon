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
    private static String defDir = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "AppImages/CompanyLogo";

    public CustomSourceImageView(Context context) {
        super(context);
    }

    public CustomSourceImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomSourceImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    public void setDefLoadLogoDir(String defDir) {
        CustomSourceImageView.defDir = defDir;
    }


    public static String getDefLoadLogoDir() {
        return defDir;
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
        String path = getDefLoadLogoDir();
        File f = new File(path);
        if (f.exists() && f.isDirectory()) {
            File[] images = f.listFiles();
            if (images != null && images.length > 0) {
                for (File img : images) {
                    String name = img.getName().toLowerCase();
                    if (name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".webp")) {
                        Glide.with(this).load(img).into(this);
                        break;
                    }
                }
            }
            return;
        }
        Glide.with(this).load(path).into(this);
    }
}
