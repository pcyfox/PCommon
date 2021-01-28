package com.pcommon.lib_vidget.widget.markview;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Environment;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.WorkerThread;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class DrawMarkView extends View {

    private final List<MarkInfo> list = new CopyOnWriteArrayList<>();
    private final Paint mLinePaint;
    private final Paint textPaint;
    private final Paint bitMapPaint;
    private final Path mPath;
    private float mX, mY;
    private static final float TOUCH_TOLERANCE = 4;
    private float startX, startY, endX, endY;
    private Canvas mCanvas;
    private MarkInfo selectMarkInfo;
    private OnMarkInfoCallback callback;

    private final Runnable runnable;
    private Bitmap mBitmap;
    private int parentHight = 0;
    private int parentWidth = 0;
    private boolean isLocal = false;
    private boolean isMarked = false;
    private float moveX = 0f;
    private float moveY = 0f;

    public boolean isMarked() {
        return isMarked;
    }

    public DrawMarkView(Context context) {
        this(context, null);
    }

    public DrawMarkView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);//此处第二个参数不能为空，否则java中无法根据id获得实例的引用
    }

    public DrawMarkView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        runnable = new Runnable() {
            @Override
            public void run() {
                //执行长按点击事件的逻辑代码
                if (callback != null && selectMarkInfo != null) {
                    callback.onEditText(selectMarkInfo.getText());
                }
            }
        };
        bitMapPaint = new Paint();
        bitMapPaint.setAntiAlias(true);
        bitMapPaint.setStyle(Paint.Style.STROKE);

        //线的Paint
        mLinePaint = new Paint();
        mLinePaint.setAntiAlias(true);
        mLinePaint.setStyle(Paint.Style.STROKE);
        mLinePaint.setStrokeWidth(5);
        mLinePaint.setColor(Color.RED);


        // 需要加上这句，否则画不出东西
        mLinePaint.setStyle(Paint.Style.STROKE);
        mLinePaint.setStrokeWidth(3);
        mLinePaint.setPathEffect(new DashPathEffect(new float[]{15, 5}, 0));


        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setStyle(Paint.Style.STROKE);
        textPaint.setStrokeWidth(5);
        textPaint.setColor(Color.BLUE);
        textPaint.setTextSize(50);
        textPaint.setStyle(Paint.Style.FILL);
        //该方法即为设置基线上那个点到底是left,center,还是right  这里我设置为center
        textPaint.setTextAlign(Paint.Align.CENTER);


        //路径
        mPath = new Path();
        //mBitmap = BitmapFactory.decodeFile("/storage/emulated/0/1.webp");//得到bitmap
        //mBitmap =resizeBitmap(BitmapFactory.decodeFile(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator+"dd.jpg"),getMeasuredWidth(),getMeasuredHeight()) ;//得到bitmap


   /*     String path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "dd.jpg";
        loadImage(path);*/
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchDown(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                touchMove(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                touchUp(x, y);
                invalidate();
                break;
        }
        return true;
    }

    /**
     * 手指按下时
     *
     * @param x
     * @param y
     */
    private void touchDown(float x, float y) {
        moveX = x;
        moveY = y;
        boolean exist = false;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getRecstartX() - list.get(i).getRecWidth() < x && list.get(i).getRecstartX() + list.get(i).getRecWidth() > x && list.get(i).getRecstartY() - list.get(i).getRecHight() < y && list.get(i).getRecstartY() + list.get(i).getRecHight() > y) {
                selectMarkInfo = list.get(i);
                exist = true;
                postDelayed(runnable, 1000);
                break;
            }

        }
        if (!exist) {
            selectMarkInfo = null;
            mPath.reset();
            mPath.moveTo(x, y);
            mX = x;
            mY = y;
            startX = x;
            startY = y;
            endX = x;
            endY = y;
        }

    }

    /**
     * 手指移动时
     *
     * @param x
     * @param y
     */
    private void touchMove(float x, float y) {

        if (selectMarkInfo == null) {
            float dx = Math.abs(x - mX);
            float dy = Math.abs(y - mY);
            //两点之间的距离大于等于4时，生成贝塞尔绘制曲线
            if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                //设置贝塞尔曲线的操作点为起点和终点的一半
                mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
                mX = x;
                mY = y;
            }
        } else {

            float tendx = x - selectMarkInfo.getRecWidth() / 2 + 15;
            float tendy = y - selectMarkInfo.getRecHight() / 2 + 15;

            float teampx = x - moveX;
            float teampy = y - moveY;
            if (Math.abs(teampx) > 2 && Math.abs(teampy) > 2) {
                removeCallbacks(runnable);
            } else {

            }
            selectMarkInfo.setEndX(tendx);
            selectMarkInfo.setEndY(tendy);
        }
        moveX = x;
        moveY = y;

    }

    /**
     * 手指抬起时
     */
    private void touchUp(float endX, float endY) {

        if (selectMarkInfo == null) {
            this.endX = endX;
            this.endY = endY;
            mPath.lineTo(mX, mY);
            if (Math.abs(startX - endX) > 100 || Math.abs(startY - endY) > 100) {
                isMarked = true;
                list.add(new MarkInfo(startX, startY, endX, endY, ""));
                selectMarkInfo = list.get(list.size() - 1);
                if (callback != null && selectMarkInfo != null) {
                    callback.onEditText(selectMarkInfo.getText());
                }
            }
            mPath.reset();
        } else {
            removeCallbacks(runnable);
        }
    }


    @Override
    protected void onDraw(Canvas canvas) {
        if (mBitmap != null) {
            //canvas.drawBitmap(mBitmap, 0, 0, null);//调用Canvas类的drawBitmap()即可绘制bitmap。
            drawBitmap(canvas, mBitmap, bitMapPaint);
        }
        canvas.drawPath(mPath, mLinePaint);
        for (int i = 0; i < list.size(); i++) {
            float startx = list.get(i).getStartX();
            float startY = list.get(i).getStartY();
            float endX = list.get(i).getEndX();
            float endY = list.get(i).getEndY();
            String text = list.get(i).getText();
            canvas.drawLine(startx, startY, endX, endY, mLinePaint);
            canvas.drawCircle(startx, startY, 10.0f, mLinePaint);
            canvas.drawCircle(endX, endY, 10.0f, mLinePaint);
            addText(startx, startY, endX, endY, text, canvas, list.get(i));
        }
        if (mCanvas == null) {
            mCanvas = canvas;
        }
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        ViewGroup mViewGroup = (ViewGroup) getParent();
        if (null != mViewGroup) {
            parentWidth = mViewGroup.getMeasuredWidth();
            parentHight = mViewGroup.getMeasuredHeight();
        }
    }

    public void drawBitmap(Canvas canvas, Bitmap bitmap, Paint paint) {

        //Rect rectF = new Rect(100, 300, 300, 100);
        //canvas.drawRect(rectF, paint);
        //canvas.drawBitmap(bitmap, null, rectF, paint);

        //canvas.drawRect(src, paint);
        //src 本次绘制的原区域 dst 本次绘制的目标区域


        if (parentWidth <= 0 || parentWidth <= 0) {
            return;
        }
        if (isLocal) {
            Rect dst = new Rect(0, 0, parentWidth, parentHight);
            Rect src = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
            canvas.drawBitmap(bitmap, src, dst, paint);
        } else {
            Rect dst = new Rect(parentWidth / 2 - parentHight / 2, 0, parentWidth / 2 + parentHight / 2, parentHight);
            Rect src = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
            canvas.drawBitmap(bitmap, src, dst, paint);
        }

        //canvas.drawBitmap(bitmap, 0, bitmap.getHeight() / 2, paint);
    }


    public void addText(float startx, float startY, float endX, float endY, String text, Canvas canvas, MarkInfo info) {
        if (!TextUtils.isEmpty(text)) {
            Rect rect = new Rect();
            textPaint.getTextBounds(text, 0, text.length(), rect);
            int w = rect.width() + 30;
            int h = rect.height() + 30;
            float tempX = 0;
            float tempY = 0;
            if (Math.abs(startx - endX) > Math.abs(startY - endY)) {
                if (startx > endX) {
                    tempX = -w / 2;
                } else {
                    tempX = w / 2;
                }

            } else {
                if (startY > endY) {
                    tempY = -30;
                } else {
                    tempY = h;
                }
            }
            float centerX = endX + tempX;
            float centerY = endY + tempY;
            info.setRecT(centerX, centerY, w / 2, h / 2);
            canvas.drawText(text, centerX, centerY, textPaint);

        }
    }


    /**
     * 撤销绘图步骤，移除上一个节点
     */
    public void cancelStep() {
        if (list.size() > 0) {
            list.remove(list.size() - 1);
            postInvalidate();
        }
    }

    private Bitmap getBitmapFromView() {

        int w = getMeasuredWidth();

        int h = getMeasuredHeight();

        Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bmp);

        // layout(0, 0, w, h);

        draw(canvas);

        return bmp;
    }

    public void saveTest() {
        final String filepath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "aaa" + ".jpg";
        final Bitmap bitmap = getBitmapFromView();
        if (bitmap != null) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        saveToLocal(bitmap, filepath);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            });
            thread.start();
        } else {
        }
    }

    @WorkerThread
    private void saveToLocal(Bitmap bitmap, String filepath) throws IOException {
        //File file = new File("/sdcard/DCIM/Camera/" + bitName + ".jpg");
        File file = new File(filepath);
        if (file.exists()) {
            file.delete();
        }
        FileOutputStream out;
        try {
            out = new FileOutputStream(file);
            if (bitmap.compress(Bitmap.CompressFormat.PNG, 90, out)) {
                out.flush();
                out.close();
                //保存图片后发送广播通知更新数据库
                // Uri uri = Uri.fromFile(file);
                // sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
             /*   Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                Uri uri = Uri.fromFile(file);
                intent.setData(uri);
                this.sendBroadcast(intent);
                showToast("保存成功");*/
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void saveImage(final String path) {
        final Bitmap bitmap = getBitmapFromView();
        if (bitmap != null) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        saveToLocal(bitmap, path);
                        if (callback != null) {
                            callback.onSaveFinish(path);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            });
            thread.start();
        } else {
        }

    }


    public void updateMarkView(String text) {
        if (selectMarkInfo != null) {
            selectMarkInfo.setText(text);
            postInvalidate();
        }
    }

    public void deleteMarkView() {
       /* if (selectMarkInfo != null) {
            list.remove(selectMarkInfo);
            selectMarkInfo = null;
            postInvalidate();
        } else {

        }*/
        cancelStep();

    }

    public void restartMarkView() {
        if (list != null) {
            if (list.size() > 0) {
                list.clear();
            }
            isMarked = false;
            postInvalidate();
        }
    }


    public Bitmap resizeBitmap(Bitmap bitmap, int w, int h) {
        if (bitmap != null) {
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            int newWidth = w;
            int newHeight = h;
            float scaleWight = ((float) newWidth) / width;
            float scaleHeight = ((float) newHeight) / height;
            Matrix matrix = new Matrix();
            matrix.postScale(scaleWight, scaleHeight);
            Bitmap res = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
            return res;

        } else {
            return null;
        }
    }


    public interface OnMarkInfoCallback {
        void onEditText(String preText);

        void onSaveFinish(String path);
    }

    public void setOnMarkInfoCallback(OnMarkInfoCallback callback) {
        this.callback = callback;
    }


    public void loadImage(String url, boolean isLocal) {
        this.isLocal = isLocal;
     /*   BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 2;*/
        Bitmap bm = BitmapFactory.decodeFile(url);
        mBitmap = bm;
        invalidate();

    /*    Glide.with(getContext())
                .asBitmap()
                .load(url)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                        //Drawable drawable = new BitmapDrawable(resource);
                        mBitmap = resource;
                        ViewGroup mViewGroup = (ViewGroup) getParent();
                        if (null != mViewGroup) {
                            parentWidth = mViewGroup.getWidth();
                            parentHight = mViewGroup.getHeight();

                      *//*      ViewGroup.LayoutParams layoutParams = getLayoutParams();
                            if (layoutParams != null) {
                                layoutParams.height = mParentHeight;
                                layoutParams.width = mParentHeight;
                                setLayoutParams(layoutParams);
                            }
*//*
                        }
                        // setBackground(drawable);
                        invalidate();
                    }

                });*/
    }

}




