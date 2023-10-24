package com.pcommon.lib_network.udp;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by melo on 2017/9/21.
 */

public class HeartbeatTimer {

    private final Timer timer;
    private TimerTask task;
    private OnScheduleListener mListener;

    private boolean isStart = false;


    public HeartbeatTimer() {
        timer = new Timer();
    }

    public void startTimer(long delay, long period) {
        isStart = true;
        task = new TimerTask() {
            @Override
            public void run() {
                if (mListener != null) {
                    mListener.onSchedule();
                }
            }
        };

        timer.schedule(task, delay, period);

    }

    public void exit() {
        timer.cancel();
        if (task != null) {
            task.cancel();
        }
        isStart = false;
    }

    public boolean isStart() {
        return isStart;
    }

    public void setOnScheduleListener(OnScheduleListener listener) {
        this.mListener = listener;
    }

}
