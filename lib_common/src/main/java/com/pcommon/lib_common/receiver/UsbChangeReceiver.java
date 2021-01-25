package com.pcommon.lib_common.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.jeremyliao.liveeventbus.LiveEventBus;

import java.util.List;

import static com.pcommon.lib_utils.USBDeviceUtils.getDeviceList;


@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class UsbChangeReceiver extends BroadcastReceiver {
    private static final String TAG = "UsbChangeReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        sendEvent(new UsbChangeEvent(getDeviceList(context)));
    }

    private void sendEvent(final UsbChangeEvent event) {
        LiveEventBus.get().with(UsbChangeReceiver.UsbChangeEvent.KEY, UsbChangeReceiver.UsbChangeEvent.class).postDelay(event, 500);

    }

    public static class Helper {
        private final static String ACTION = "android.hardware.usb.action.USB_STATE";
        private static final String ACTION_USB_DEVICE_ATTACHED = "android.hardware.usb.action.USB_DEVICE_ATTACHED";
        private static final String ACTION_USB_DEVICE_DETACHED = "android.hardware.usb.action.USB_DEVICE_DETACHED";

        static Helper instance;
        private UsbChangeReceiver receiver;

        public static Helper getInstance() {
            if (instance == null) {
                instance = new Helper();
            }
            return instance;
        }

        public void register(Context context) {
            if (context == null) return;
            context = context.getApplicationContext();
            if (receiver == null) {
                receiver = new UsbChangeReceiver();
            }
            IntentFilter filter = new IntentFilter();
            filter.addAction(ACTION);
            filter.addAction(ACTION_USB_DEVICE_ATTACHED);
            filter.addAction(ACTION_USB_DEVICE_DETACHED);
            context.registerReceiver(receiver, filter);
        }

        public void unregister(Context context) {
            if (receiver == null || context == null) return;
            context.unregisterReceiver(receiver);

        }
    }



    public static class UsbChangeEvent {
        public static final String KEY = "UsbChangeEvent";
        List<String> list;

        public UsbChangeEvent(List<String> list) {
            this.list = list;
        }

        public List<String> getList() {
            return list;
        }
    }


}
