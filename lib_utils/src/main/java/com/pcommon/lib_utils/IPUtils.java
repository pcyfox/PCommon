package com.pcommon.lib_utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.AppUtils;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;

final public class IPUtils {
    public static boolean isPrintLog = BuildConfig.DEBUG;

    private IPUtils() {
    }


    public static String getIpAddress(Context context) {
        if (context == null) return null;
        ConnectivityManager conMann = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (conMann == null) return null;

        NetworkInfo mobileNetworkInfo = conMann.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET);
        if (mobileNetworkInfo != null && mobileNetworkInfo.isConnected()) {
            return getIp();
        }

        NetworkInfo wifiNetworkInfo = conMann.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifiNetworkInfo != null && wifiNetworkInfo.isConnected()) {
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            int ipAddress = wifiInfo.getIpAddress();
            String ip = intToIp(ipAddress);
            if (isPrintLog)
                System.out.println("----------- net element:" + wifiInfo.getSSID() + ",Ip:" + ip + "-----------");
            return ip;
        }
        return "";
    }


    public static String intToIp(int ipInt) {
        return (ipInt & 0xFF) + "." + ((ipInt >> 8) & 0xFF) + "." + ((ipInt >> 16) & 0xFF) + "." + ((ipInt >> 24) & 0xFF);
    }

    private static String getIp() {
        String ip = null;
        try {
            List<String> ips = new ArrayList<>();
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface into = en.nextElement();
                for (Enumeration<InetAddress> addr = into.getInetAddresses(); addr.hasMoreElements(); ) {
                    InetAddress inetAddress = addr.nextElement();
                    // 排除IPv6地址和回环地址
                    if (!inetAddress.isLoopbackAddress() && !inetAddress.isLinkLocalAddress() && inetAddress instanceof Inet4Address) {
                        ip = inetAddress.getHostAddress();
                        if (isPrintLog)
                            System.out.println("----------- net element:" + into.getName() + ",Ip:" + ip + "-----------");
                        ips.add(ip);
                    }
                }
            }
            if (ips.size() == 0) return "";
            if (ips.size() == 1) return ips.get(0);
            return sort(ips).get(0);
        } catch (SocketException ex) {
            Log.e("getIp()", ex.toString());
        }
        return ip;
    }

    private static List<String> sort(final List<String> ips) {
        Collections.sort(ips, (o1, o2) -> {
            if (o1.equals(o2)) return 0;
            if (o1.contains(".") && o2.contains(".")) {
                return o2.substring(0, o2.indexOf(".")).compareTo(o1.substring(0, o1.indexOf(".")));
            }
            return 0;
        });
        return ips;
    }

}
