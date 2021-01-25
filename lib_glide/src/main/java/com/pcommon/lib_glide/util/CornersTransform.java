package com.pcommon.lib_glide.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;

import androidx.annotation.NonNull;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.bumptech.glide.util.Util;

import java.nio.ByteBuffer;
import java.security.MessageDigest;

public class CornersTransform extends BitmapTransformation {
    private final String ID = getClass().getName();
    private final float borderWidth;
    private final int borderColor;

    public CornersTransform(float borderWidth, int borderColor) {
        this.borderWidth = borderWidth;
        this.borderColor = borderColor;
    }

    @Override
    protected Bitmap transform(@NonNull BitmapPool pool, @NonNull Bitmap source, int outWidth, int outHeight) {
        // 目标直径
        int destMinEdge = Math.min(outWidth, outHeight);
        // 目标半径 & 中心点坐标
        float radius = destMinEdge / 2f;
        // 修正源宽高
        int srcWidth = source.getWidth();
        int srcHeight = source.getHeight();
        float scaleX = (destMinEdge - borderWidth * 2) / (float) srcWidth;
        float scaleY = (destMinEdge - borderWidth * 2) / (float) srcHeight;
        float maxScale = Math.max(scaleX, scaleY);
        float scaledWidth = maxScale * srcWidth;
        float scaledHeight = maxScale * srcHeight;
        // 源绘制起始坐标
        float left = (destMinEdge - scaledWidth) / 2f;
        float top = (destMinEdge - scaledHeight) / 2f;
        // 新建画布
        Bitmap outBitmap = pool.get(destMinEdge, destMinEdge, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(outBitmap);
        // 绘制内圆
        Paint srcPaint = new Paint(Paint.DITHER_FLAG | Paint.FILTER_BITMAP_FLAG | Paint.ANTI_ALIAS_FLAG);
        srcPaint.setColor(borderColor);
        RectF destRect = new RectF(left, top, left + scaledWidth, top + scaledHeight);
        canvas.drawCircle(radius, radius, radius - borderWidth, srcPaint);
        srcPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(source, null, destRect, srcPaint);
        // 绘制外圆
        Paint borderPaint = new Paint(Paint.DITHER_FLAG | Paint.ANTI_ALIAS_FLAG);
        borderPaint.setColor(borderColor);
        borderPaint.setStyle(Paint.Style.FILL);
        borderPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OVER));
        canvas.drawCircle(radius, radius, radius, borderPaint);
        return outBitmap;
    }

    @Override
    public void updateDiskCacheKey(MessageDigest messageDigest) {
        messageDigest.update(ID.getBytes(CHARSET));
        byte[] radiusData = ByteBuffer.allocate(Float.BYTES * 2).putFloat(borderWidth).putFloat(borderColor).array();
        messageDigest.update(radiusData);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof CornersTransform) {
            return borderColor == ((CornersTransform) o).borderColor && borderWidth == ((CornersTransform) o).borderWidth;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hashcode = Util.hashCode(borderWidth);
        hashcode = Util.hashCode(borderColor, hashcode);
        hashcode = Util.hashCode(ID.hashCode(), hashcode);
        return hashcode;
    }
}
