package com.pcommon.lib_utils;

import android.app.Activity;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.LayoutRes;


public class MaskUtils {
    private static final String TAG = "MaskUtils";

    private static boolean idMainThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }

    private static View addView(Window window, @LayoutRes int viewId, Object tag) {
        View mask = window.getLayoutInflater().inflate(viewId, null);
        mask.setTag(tag);
        addView(window, mask);
        return mask;
    }

    private static void addView(Window window, View mask) {
        if (isViewHasParent(mask)) {
            return;
        }
        ViewGroup decorView = (ViewGroup) window.getDecorView();
        decorView.addView(mask);
    }

    private static void addView(Window window, View mask, Object tag) {
        Log.e(TAG, "addView() called with: window = [" + window + "], mask = [" + mask + "], tag = [" + tag.getClass().getSimpleName() + "]");
        if (isViewHasParent(mask)) {
            return;
        }
        mask.setTag(tag);
        ViewGroup decorView = (ViewGroup) window.getDecorView();
        decorView.addView(mask);
    }

    private static boolean isViewHasParent(View view) {
        return view.getParent() != null;
    }


    public static boolean isShow(Activity activity, Object tag) {
        View view = activity.getWindow().getDecorView().findViewWithTag(tag);
        return view != null;
    }

    ;

    private static void removeViewByTag(Window window, Object tag) {
        View view = window.getDecorView().findViewWithTag(tag);
        removeView(window, view);
    }


    public synchronized static void show(final Activity activity, final View view, final Object tag) {
        if (idMainThread()) {
            addView(activity.getWindow(), view, tag);
        } else {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    addView(activity.getWindow(), view, tag);
                }
            });

        }
    }

    private static void addViewByWM(WindowManager manager, View view, ViewGroup.LayoutParams params) {
        manager.addView(view, params);
    }


    public synchronized static void show(final Window window, @LayoutRes final int viewId, final Object tag) {
        if (idMainThread()) {
            addView(window, viewId, tag);
        } else {
            window.getDecorView().post(new Runnable() {
                @Override
                public void run() {
                    addView(window, viewId, tag);
                }
            });
        }
    }

    public synchronized static void show(final Activity activity, @LayoutRes final int viewId, final ViewGroup.LayoutParams params, final Object tag) {
        if (idMainThread()) {

            addViewByWM(activity.getWindowManager(), View.inflate(activity, viewId, null), params);
        } else {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    addViewByWM(activity.getWindowManager(), View.inflate(activity, viewId, null), params);
                }
            });
        }
    }

    public synchronized static void hide(Activity activity, final Object tag) {
        hide(activity.getWindow(), tag);
    }

    public synchronized static void hide(final Window window, final Object tag) {
        if (idMainThread()) {
            removeViewByTag(window, tag);
        } else {
            window.getDecorView().post(new Runnable() {
                @Override
                public void run() {
                    removeViewByTag(window, tag);
                }
            });
        }
    }

    private static void removeView(Window window, View view) {
        if (window != null && view != null) {
            ViewGroup decorView = (ViewGroup) window.getDecorView();
            try {
                decorView.removeView(view);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Log.e(TAG, "removeView() error!  called with: window = [" + window + "], view = [" + view + "]");
        }
    }


}
