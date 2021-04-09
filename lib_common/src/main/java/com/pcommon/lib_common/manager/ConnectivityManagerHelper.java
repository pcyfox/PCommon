package com.pcommon.lib_common.manager;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.elvishew.xlog.XLog;
import com.jeremyliao.liveeventbus.LiveEventBus;
import com.pcommon.lib_common.receiver.NetWorkChangEvent;
import com.pcommon.lib_common.receiver.NetWorkChangReceiver;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class ConnectivityManagerHelper {
    private static final String TAG = "ConnectivityManagerHelp";

    private ConnectivityManagerHelper() {

    }

    public static final ConnectivityManagerHelper INSTANCE = new ConnectivityManagerHelper();

    private ConnectivityManager connectivityManager = null;
    private NetWorkChangEvent.Type lastType;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void init(@NonNull Context context) {
        if (connectivityManager != null) {
            return;
        }
        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        connectivityManager.requestNetwork(new NetworkRequest.Builder().build(),
                new ConnectivityManager.NetworkCallback() {
                    @Override
                    public void onAvailable(Network network) {
                        XLog.i(TAG + ";onAvailable() called with: network = [" + network + "]");
                        super.onAvailable(network);
                        NetWorkChangEvent event = new NetWorkChangEvent(true, NetWorkChangReceiver.getType(connectivityManager.getNetworkInfo(network)));
                        lastType = event.getType();
                        sendEvent(event);
                    }

                    @Override
                    public void onUnavailable() {
                        XLog.e(TAG + "+onUnavailable() called");
                        NetWorkChangEvent event = new NetWorkChangEvent(false, lastType);
                        sendEvent(event);
                        super.onUnavailable();
                    }

                    @Override
                    public void onCapabilitiesChanged(Network network, NetworkCapabilities networkCapabilities) {
                        Log.i(TAG, "onCapabilitiesChanged() called with: network = [" + network + "], networkCapabilities = [" + networkCapabilities + "]");
                        super.onCapabilitiesChanged(network, networkCapabilities);
                    }

                    @Override
                    public void onLinkPropertiesChanged(Network network, LinkProperties linkProperties) {
                        Log.i(TAG, "onLinkPropertiesChanged() called with: network = [" + network + "], linkProperties = [" + linkProperties + "]");
                        super.onLinkPropertiesChanged(network, linkProperties);
                    }

                    @Override
                    public void onLosing(Network network, int maxMsToLive) {
                        XLog.i(TAG + ";onLosing() called with: network = [" + network + "], maxMsToLive = [" + maxMsToLive + "]");
                        super.onLosing(network, maxMsToLive);
                    }

                    @Override
                    public void onLost(Network network) {
                        NetWorkChangEvent event = new NetWorkChangEvent(false, NetWorkChangReceiver.getType(connectivityManager.getNetworkInfo(network)));
                        sendEvent(event);
                        XLog.e(TAG + ";onLost() called with: network = [" + network + "]");
                        super.onLost(network);
                    }
                }

        );
    }


    private void sendEvent(final NetWorkChangEvent event) {
        LiveEventBus.get().with(event.getClass().getSimpleName()).post(event);
    }
}