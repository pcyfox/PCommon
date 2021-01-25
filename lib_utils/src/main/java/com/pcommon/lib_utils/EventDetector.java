package com.pcommon.lib_utils;

import android.view.View;

import java.lang.ref.WeakReference;
import java.util.LinkedList;
//处理双击退出
public class EventDetector {

    public EventDetector(int times, int millisLimit) {
        init(times, millisLimit);
    }

    public EventDetector(int times, int millisLimit, EventDetectorCallback callback) {
        init(times, millisLimit).setCallback(callback);
    }

    public EventDetector init(int times, int millisLimit) {
        mTimes = times;
        mMillisLimit = millisLimit;
        return this;
    }

    public EventDetector setCallback(EventDetectorCallback callback) {
        if (callback != null) {
            mCallback = new WeakReference<>(callback);
        }
        return this;
    }

    public boolean addEvent() {
        long now = System.currentTimeMillis();
        if (mEventTimeList == null) {
            mEventTimeList = new LinkedList<>();
        }
        mEventTimeList.add(now);

        while (!mEventTimeList.isEmpty() && now - mEventTimeList.get(0) > mMillisLimit) {
            mEventTimeList.removeFirst();
        }
        if (mEventTimeList.size() >= mTimes) {
            if (mCallback != null && mCallback.get() != null) {
                mCallback.get().onEventDetected();
            }
            mEventTimeList.clear();
            return true;
        } else {
            return false;
        }
    }

    public void attachView(View view) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addEvent();
            }
        });
    }

    public int getTimesLack() {
        return mTimes - mEventTimeList.size();
    }


    private int mTimes;
    private int mMillisLimit;
    private LinkedList<Long> mEventTimeList;
    private WeakReference<EventDetectorCallback> mCallback;

    public interface EventDetectorCallback {
        void onEventDetected();
    }
}
