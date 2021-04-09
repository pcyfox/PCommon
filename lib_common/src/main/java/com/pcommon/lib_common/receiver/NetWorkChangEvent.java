package com.pcommon.lib_common.receiver;


import org.jetbrains.annotations.NotNull;

public class NetWorkChangEvent {
    private boolean isAvailable;
    private Type type = Type.UNKNOWN;

    public NetWorkChangEvent() {
    }

    public NetWorkChangEvent(boolean isAvailable, Type type) {
        this.isAvailable = isAvailable;
        this.type = type;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    @NotNull
    @Override
    public String toString() {
        return "NetWorkChangEvent{" +
                "isAvailable=" + isAvailable +
                ", type=" + type +
                '}';
    }

    public enum Type {
        VPN, ETHERNET, WIFI, UNKNOWN
    }
}
