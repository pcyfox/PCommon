package com.pcommon.lib_vidget.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.ColorInt;


import com.blankj.utilcode.util.SizeUtils;
import com.elvishew.xlog.XLog;
import com.pcommon.lib_utils.ViewUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class LinePathView extends View {
    private static final String TAG = "LinePathView";
    private ViewTouchEvenListener viewTouchEvenListener;
    /**
     * 笔画X坐标起点
     */
    private float mX;
    /**
     * 笔画Y坐标起点
     */
    private float mY;
    /**
     * 手写画笔
     */
    private final Paint mGesturePaint = new Paint();
    private final Paint eraserPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    /**
     * 路径
     */
    private final List<Path> mPathList = new ArrayList<>();
    private final List<Path> eraserPathList = new ArrayList<>();
    private Path mPath;

    /**
     * 是否已经签名
     */
    private boolean isTouched = false;
    /**
     * 画笔宽度 px；
     */
    private int mPaintWidth = 3;
    /**
     * 前景色
     */
    private int mPenColor = Color.BLACK;
    /**
     * 背景色（指最终签名结果文件的背景颜色，默认为透明色）
     */

    private boolean isHasDrawBitmap = false;

    private boolean isInEraserState = false;
    private boolean isCanTouch = true;
    private Bitmap background;
    private Paint mBitPaint;
    private int mBackColor = Color.WHITE;

    private int w;
    private int h;

    private Canvas cacheCanvas;

    public LinePathView(Context context) {
        super(context);
        init(context);
    }

    public LinePathView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public LinePathView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void init(Context context) {
        //设置抗锯齿
        mGesturePaint.setAntiAlias(true);
        //设置签名笔画样式
        mGesturePaint.setStyle(Paint.Style.STROKE);
        //设置笔画宽度
        mGesturePaint.setStrokeWidth(mPaintWidth);
        //设置签名颜色
        mGesturePaint.setColor(mPenColor);

        eraserPaint.setColor(Color.WHITE);
        eraserPaint.setStyle(Paint.Style.STROKE);
        eraserPaint.setStrokeWidth(SizeUtils.dp2px(30));

        mBitPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBitPaint.setFilterBitmap(true);
        mBitPaint.setDither(true);
    }

    public void setViewTouchEvenListener(ViewTouchEvenListener viewTouchEvenListener) {
        this.viewTouchEvenListener = viewTouchEvenListener;
    }

    public ViewTouchEvenListener getViewTouchEvenListener() {
        return viewTouchEvenListener;
    }

    public void setBackground(Bitmap background) {
        this.background = background;
    }

    public boolean isInEraserState() {
        return isInEraserState;
    }

    public void setInEraserState(boolean inEraserState) {
        isInEraserState = inEraserState;
    }

    public void setEraserPaintWidth(float width) {
        if (width < SizeUtils.dp2px(1)) {
            Log.e(TAG, "setEraserPaintWidth: width is too small width=" + width);
            return;
        }
        eraserPaint.setStrokeWidth(width);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w * h == 0) return;
        this.w = w;
        this.h = h;
        isTouched = false;
    }

    private void drawBitmap(Bitmap bitmap, int w, int h) {
        XLog.d("drawBitmap() called with: bitmap = [" + bitmap + "], w = [" + w + "], h = [" + h + "]");
        if (bitmap == null || cacheCanvas == null) {
            return;
        }
        if (!bitmap.isMutable()) {
            XLog.w("bitmap is not mutable!");
            return;
        }
        Bitmap backBm = Bitmap.createScaledBitmap(bitmap, w, h, true);
        setBackground(backBm);
        invalidate();
        isHasDrawBitmap = true;

    }


    public void drawBitmap(final Bitmap bitmap) {

        post(new Runnable() {
            @Override
            public void run() {
                if (bitmap == null || bitmap.isRecycled()) return;
                XLog.d("drawBitmap() called with: bitmap.getWidth() = [" + bitmap.getWidth() + "]" + "bitmap.getH:" + bitmap.getHeight());
                if (w * h == 0) {
                    XLog.e("drawBitmap() called with:w = [" + w + "]" + "h:" + h);
                    return;
                }
                drawBitmap(bitmap, w, h);
                invalidate();
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isCanTouch) {
            return false;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchDown(event);
                break;
            case MotionEvent.ACTION_MOVE:
                isTouched = true;
                touchMove(event);
                break;
            case MotionEvent.ACTION_UP:
                break;
        }
        // 更新绘制
        invalidate();
        if (viewTouchEvenListener != null) {
            viewTouchEvenListener.onTouchEvent(event);
        }
        return true;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

    }

    @Override
    public void draw(Canvas canvas) {
        cacheCanvas = canvas;
        super.draw(canvas);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (background != null && !background.isRecycled()) {
            canvas.drawBitmap(background, 0, 0, mBitPaint);
        }
        for (Path path : mPathList) {
            if (eraserPathList.contains(path)) {
                canvas.drawPath(path, eraserPaint);
            } else {
                canvas.drawPath(path, mGesturePaint);
            }
        }
    }


    private void touchDown(MotionEvent event) {
        mPath = new Path();
        if (isInEraserState) {
            eraserPathList.add(mPath);
        }
        mPathList.add(mPath);
        float x = event.getX();
        float y = event.getY();
        mX = x;
        mY = y;
        mPath.moveTo(x, y);
    }

    // 手指在屏幕上滑动时调用
    private void touchMove(MotionEvent event) {
        final float x = event.getX();
        final float y = event.getY();
        final float previousX = mX;
        final float previousY = mY;
        final float dx = Math.abs(x - previousX);
        final float dy = Math.abs(y - previousY);
        // 两点之间的距离大于等于3时，生成贝塞尔绘制曲线
        if (dx >= 3 || dy >= 3) {
            // 设置贝塞尔曲线的操作点为起点和终点的一半
            float cX = (x + previousX) / 2;
            float cY = (y + previousY) / 2;
            // 二次贝塞尔，实现平滑曲线；previousX, previousY为操作点，cX, cY为终点
            mPath.quadTo(previousX, previousY, cX, cY);
            // 第二次执行时，第一次结束调用的坐标值将作为第二次调用的初始坐标值
            mX = x;
            mY = y;
        }
    }

    /**
     * 清除画板
     */
    public void clear() {
        XLog.d(" LinePathView clear() called");
        isTouched = false;
        //更新画板信息
        mGesturePaint.setColor(mPenColor);
        // cacheCanvas.drawColor(mBackColor, PorterDuff.Mode.CLEAR);
        mPathList.clear();
        eraserPathList.clear();
        if (background != null) {
            background.recycle();
            background = null;
        }
        isHasDrawBitmap = false;
        invalidate();
    }

    public void rollback() {
        if (mPathList.isEmpty()) {
            if (mPath != null) {
                mPath.reset();
                invalidate();
            }
            return;
        }
        Path last = mPathList.get(mPathList.size() - 1);
        eraserPathList.remove(last);
        if (last == mPath) {
            mPath.reset();
        }
        mPathList.remove(last);
        invalidate();
    }


    /**
     * 保存画板
     *
     * @param path 保存到路径
     */
    public void save(String path) throws IOException {
        save(path, false, 0);
    }

    public boolean isHasDrawBitmap() {
        return isHasDrawBitmap;
    }

    /**
     * 保存画板
     *
     * @param fileName   保存到路径
     * @param clearBlank 是否清除边缘空白区域
     * @param blank      要保留的边缘空白距离
     */
    public void save(String fileName, boolean clearBlank, int blank) throws IOException {
        Bitmap bitmap = getCurrentBitmap();
        //BitmapUtil.createScaledBitmapByHeight(srcBitmap, 300);//  压缩图片
        if (clearBlank) {
            bitmap = clearBlank(bitmap, blank);
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 60, bos);
        byte[] buffer = bos.toByteArray();
        if (buffer != null) {
            File file = new File(fileName);
            if (file.exists()) {
                file.delete();
            }
            OutputStream outputStream;
            if (fileName.contains(File.separator)) {
                outputStream = new FileOutputStream(file);
            } else {
                outputStream = getContext().openFileOutput(fileName, Context.MODE_PRIVATE);
            }
            outputStream.write(buffer);
            outputStream.close();
        }
    }


    public Bitmap getCurrentBitmap() {
        return ViewUtils.createBitmap(this);
    }


    /**
     * 获取画板的bitmap
     *
     * @return
     */
    public Bitmap getBitMap() {
        setDrawingCacheEnabled(true);
        buildDrawingCache();
        Bitmap bitmap = getDrawingCache();
        setDrawingCacheEnabled(false);
        return bitmap;
    }

    /**
     * 逐行扫描 清楚边界空白。
     *
     * @param bp
     * @param blank 边距留多少个像素
     * @return
     */
    private Bitmap clearBlank(Bitmap bp, int blank) {
        int HEIGHT = bp.getHeight();
        int WIDTH = bp.getWidth();
        int top = 0, left = 0, right = 0, bottom = 0;
        int[] pixs = new int[WIDTH];
        boolean isStop;
        //扫描上边距不等于背景颜色的第一个点
        for (int y = 0; y < HEIGHT; y++) {
            bp.getPixels(pixs, 0, WIDTH, 0, y, WIDTH, 1);
            isStop = false;
            for (int pix : pixs) {
                if (pix != mBackColor) {
                    top = y;
                    isStop = true;
                    break;
                }
            }
            if (isStop) {
                break;
            }
        }
        //扫描下边距不等于背景颜色的第一个点
        for (int y = HEIGHT - 1; y >= 0; y--) {
            bp.getPixels(pixs, 0, WIDTH, 0, y, WIDTH, 1);
            isStop = false;
            for (int pix : pixs) {
                if (pix != mBackColor) {
                    bottom = y;
                    isStop = true;
                    break;
                }
            }
            if (isStop) {
                break;
            }
        }
        pixs = new int[HEIGHT];
        //扫描左边距不等于背景颜色的第一个点
        for (int x = 0; x < WIDTH; x++) {
            bp.getPixels(pixs, 0, 1, x, 0, 1, HEIGHT);
            isStop = false;
            for (int pix : pixs) {
                if (pix != mBackColor) {
                    left = x;
                    isStop = true;
                    break;
                }
            }
            if (isStop) {
                break;
            }
        }
        //扫描右边距不等于背景颜色的第一个点
        for (int x = WIDTH - 1; x > 0; x--) {
            bp.getPixels(pixs, 0, 1, x, 0, 1, HEIGHT);
            isStop = false;
            for (int pix : pixs) {
                if (pix != mBackColor) {
                    right = x;
                    isStop = true;
                    break;
                }
            }
            if (isStop) {
                break;
            }
        }
        if (blank < 0) {
            blank = 0;
        }
        //计算加上保留空白距离之后的图像大小
        left = Math.max(left - blank, 0);
        top = Math.max(top - blank, 0);
        right = Math.min(right + blank, WIDTH - 1);
        bottom = Math.min(bottom + blank, HEIGHT - 1);
        return Bitmap.createBitmap(bp, left, top, right - left, bottom - top);
    }

    public void setCanTouch(boolean canTouch) {
        isCanTouch = canTouch;
    }

    /**
     * 设置画笔宽度 默认宽度为10px
     *
     * @param mPaintWidth
     */
    public void setPaintWidth(int mPaintWidth) {
        mPaintWidth = mPaintWidth > 0 ? mPaintWidth : 3;
        this.mPaintWidth = mPaintWidth;
        mGesturePaint.setStrokeWidth(mPaintWidth);
    }

    public void setBackColor(@ColorInt int backColor) {
        mBackColor = backColor;
    }


    /**
     * 设置画笔颜色
     *
     * @param mPenColor
     */
    public void setPenColor(int mPenColor) {
        this.mPenColor = mPenColor;
        mGesturePaint.setColor(mPenColor);
    }

    /**
     * 是否有签名
     *
     * @return
     */
    public boolean getTouched() {
        return isTouched;
    }


    public interface ViewTouchEvenListener {
        void onTouchEvent(MotionEvent event);
    }
}
