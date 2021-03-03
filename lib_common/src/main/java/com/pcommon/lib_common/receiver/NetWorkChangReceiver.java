package com.pcommon.lib_common.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import androidx.annotation.Keep;

import com.jeremyliao.liveeventbus.LiveEventBus;

@Keep
public class NetWorkChangReceiver extends BroadcastReceiver {
    private static final String TAG = "NetWorkChangReceiver";
    private NetWorkChangEvent event;
    public boolean isNetWorkConnect;

    @Override
    public void onReceive(final Context context, Intent intent) {
        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            System.out.println("-----------网络状态发生变化----------");
            NetworkInfo info = connectivityManager.getActiveNetworkInfo();
            if (info != null) {
                Log.d(TAG, "onReceive() called with: info =" + info.toString());
            }
            if (info != null && info.isAvailable()) {
                isNetWorkConnect = true;
                event = new NetWorkChangEvent(true, info.getType());
                sendEvent(event);
                Helper.getInstance().setConnectionType(info.getType());
                System.out.println(TAG + "NetworkInfo State: 网络已连接,type:" + Helper.getConnectionType(info.getType()));
            } else {
                isNetWorkConnect = false;
                event = new NetWorkChangEvent(false, -1);
                sendEvent(event);
                Helper.getInstance().setConnectionType(-1);
                System.out.println(TAG + "NetworkInfo State: 网络已断开");
            }

        }

    }

    private void sendEvent(final NetWorkChangEvent event) {
        LiveEventBus.get().with(event.getClass().getSimpleName()).post(event);
    }


    private NetWorkChangEvent getEvent() {
        return event;
    }

    public static class Helper {
        private Helper() {
        }

        private static Helper helper;

        private boolean isWifiConnect;

        public static Helper getInstance() {
            if (helper == null) {
                helper = new Helper();
            }
            return helper;

        }

        private NetWorkChangReceiver netWorkChangReceiver;
        private boolean isRegistered = false;

        /**
         * 获取连接类型
         *
         * @param type
         * @return
         */
        public static String getConnectionType(int type) {

            Log.d(TAG, "getConnectionType() called with: type = [" + type + "]");
            String connType = "其它";
            switch (type) {
                case ConnectivityManager.TYPE_MOBILE:
                    connType = "移动数据网络";
                    break;
                case ConnectivityManager.TYPE_WIFI:
                    connType = "WIFI网络";
                case ConnectivityManager.TYPE_ETHERNET:
                    break;
            }
            return connType;
        }

        public void setConnectionType(int type) {
            if (type == ConnectivityManager.TYPE_MOBILE) {
                isWifiConnect = false;
            } else if (type == ConnectivityManager.TYPE_WIFI) {
                isWifiConnect = true;
            } else {
                isWifiConnect = false;
            }
        }

        public boolean isWifiConnect() {
            return isWifiConnect;
        }

        public boolean isConnected() {
            return netWorkChangReceiver != null && netWorkChangReceiver.getEvent().isAvailable();
        }

        public void register(Context context) {
            if (isRegistered || context == null) return;
            context = context.getApplicationContext();
            if (netWorkChangReceiver == null) {
                netWorkChangReceiver = new NetWorkChangReceiver();
            }
            IntentFilter filter = new IntentFilter();
            filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
            filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
            filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            context.registerReceiver(netWorkChangReceiver, filter);
            isRegistered = true;
        }

        public void unregister(Context context) {
            if (!isRegistered || context == null) return;
            context.unregisterReceiver(netWorkChangReceiver);
            isRegistered = false;
        }
    }


    public class NetWorkChangEvent {
        private boolean isAvailable;
        private int type;

        public NetWorkChangEvent() {
        }

        public NetWorkChangEvent(boolean isAvailable, int type) {
            this.isAvailable = isAvailable;
            this.type = type;
        }

        public boolean isAvailable() {
            return isAvailable;
        }

        public void setAvailable(boolean available) {
            isAvailable = available;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        @Override
        public String toString() {
            return "NetWorkChangEvent{" +
                    "isAvailable=" + isAvailable +
                    ", type=" + type +
                    '}';
        }
    }
}
