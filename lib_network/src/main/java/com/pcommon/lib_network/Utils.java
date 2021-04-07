package com.pcommon.lib_network;


import android.util.Log;

import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.nio.channels.FileChannel;

final public class Utils {
    public static final Inet4Address LOCALHOST4;
    static {
        byte[] LOCALHOST4_BYTES = new byte[]{127, 0, 0, 1};
        Inet4Address localhost4 = null;
        try {
            localhost4 = (Inet4Address) InetAddress.getByAddress("localhost", LOCALHOST4_BYTES);
        } catch (Exception var20) {
            var20.printStackTrace();
        }

        LOCALHOST4 = localhost4;
    }

    private static final String TAG = "Utils";

    public static <T> T checkNotNull(@Nullable T object, String message) {
        if (object == null) {
            throw new NullPointerException(message);
        }
        return object;
    }

    public static void copyNio(String source, String dest) {
        if (BuildConfig.DEBUG)
            Log.d(TAG, "copyNio() called with: source = [" + source + "], dest = [" + dest + "]");
        FileChannel input = null;
        FileChannel output = null;

        try {
            input = new FileInputStream(new File(source)).getChannel();
            output = new FileOutputStream(new File(dest)).getChannel();
            output.transferFrom(input, 0, input.size());
        } catch (Exception e) {
            Log.w(TAG + "copyNio", "error occur while copy", e);
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
                if (output != null) {
                    output.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}
