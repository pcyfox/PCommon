package com.pcommon.lib_network.udp;

import android.os.SystemClock;
import android.util.Log;

public abstract class CheckSelfListener {

    public static final String CHECK_BY_SELF = "CHECK_BY_SELF";
    private static final String TAG = "CheckSelfListener";
    private long sendMsgCount = 0;
    private long receiveMsgCount = 0;

    private long timeOut = 200;
    private long startTime;
    private long endTime;

    public boolean increaseSendMsgCount() {
        if (startTime != 0 && sendMsgCount < receiveMsgCount) {
            Log.e(TAG, "increaseSendMsgCount() error,some task is running");
            return false;
        }
        sendMsgCount++;
        startTime = SystemClock.uptimeMillis();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(timeOut);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (isTimeout()) {
                    onCheckResult(false, "time out");
                }

            }
        }).start();
        return true;
    }

    public void increaseReceiveMsgTime() {
        receiveMsgCount++;
        endTime = SystemClock.uptimeMillis();
        if (isTimeout()) {
            onCheckResult(false, "time out!");
        } else {
            onCheckResult(true, "socket is ok!");
        }
    }

    private boolean isTimeout() {
        return endTime - startTime > timeOut;
    }


    public void setTimeOut(long timeOut) {
        this.timeOut = timeOut;
    }

    public long getTimeOut() {
        return timeOut;
    }

    protected void onCheckResult(boolean isOK, String msg) {

    }

    @Override
    public String toString() {
        return "CheckSelfListener{" +
                "sendMsgCount=" + sendMsgCount +
                ", receiveMsgCount=" + receiveMsgCount +
                ", timeOut=" + timeOut +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                '}';
    }
}
