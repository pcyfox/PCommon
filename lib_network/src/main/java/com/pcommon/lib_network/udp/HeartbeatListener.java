package com.pcommon.lib_network.udp;

public interface HeartbeatListener {
    void onTimeout(long duration);
}
