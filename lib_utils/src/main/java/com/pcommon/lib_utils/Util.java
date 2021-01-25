package com.pcommon.lib_utils;

import android.content.Context;
import android.text.TextUtils;

import com.blankj.utilcode.constant.TimeConstants;
import com.blankj.utilcode.util.DeviceUtils;
import com.blankj.utilcode.util.EncryptUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Util {
    public final static int KB = 1024;
    public final static int MB = 1024 * 1024;
    public final static int GB = 1024 * 1024 * 1024;

    public final static int MinuteS = 60;
    public final static int HourS = MinuteS * 60;
    public final static int DayS = HourS * 24;
    public final static int MonthS = DayS * 30;
    public final static int YearS = DayS * 365;


    public static String formatSize(long size) {
        if (size > GB) {
            return String.format("%.2fGB", (double) size / GB);
        } else if (size > MB) {
            return String.format("%.2fMB", (double) size / MB);
        } else if (size > KB) {
            return String.format("%.2fKB", (double) size / KB);
        } else {
            return String.format("%dB", size);
        }
    }

    public static String getCurrentDateTime() {
        return formatDateTimeMs(System.currentTimeMillis());
    }

    public static String getCurrentDate() {
        return formatDateMs(System.currentTimeMillis());
    }

    public static String formatDateTimeS(long tsSeconds) {
        return formatDateTimeMs(tsSeconds * 1000);
    }

    public static String formatDateS(long tsSeconds) {
        return formatDateMs(tsSeconds * 1000);
    }

    public static String formatDateTimeMs(long tsMillis) {
        if (tsMillis <= 0) {
            return "0000-00-00 00:00:00";
        } else {
            return DateTimeFormat.format(new Date(tsMillis));
        }
    }

    public static String formatDateMs(long tsMillis) {
        if (tsMillis <= 0) {
            return "0000-00-00";
        } else {
            return DateFormat.format(new Date(tsMillis));
        }
    }

    public static long parseTimeString(String strTime) {
        try {
            Date date = DateTimeFormat.parse(strTime);
            return date.getTime();
        } catch (ParseException e) {
            return 0;
        }

    }

    public static String getExceptionContent(Throwable e) {
        StringBuffer sb = new StringBuffer();
        sb.append(e.toString());
        StackTraceElement[] stackArray = e.getStackTrace();
        for (StackTraceElement element : stackArray) {
            sb.append(element.toString()).append("\n");
        }
        return sb.toString();
    }


    public static String getLauncherActivity(Context context, String packageName) {
        return context.getApplicationContext()
                .getPackageManager()
                .getLaunchIntentForPackage(packageName)
                .getComponent()
                .getClassName();
    }

    private static SimpleDateFormat DateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static SimpleDateFormat DateFormat = new SimpleDateFormat("yyyy-MM-dd");


    public static <T> T checkNull(T object, String msg) {
        if (object == null) {
            throw new NullPointerException("" + msg);
        }
        return object;
    }

    public static <T> T checkNull(T object) {
        return checkNull(object, "object is null");
    }

    public static String genClientId() {
        String content = String.format("%s-%s-%s-%s",
                DeviceUtils.getAndroidID(),
                DeviceUtils.getMacAddress(),
                DeviceUtils.getModel(),
                DeviceUtils.getManufacturer());
        return EncryptUtils.encryptHmacMD5ToString(content, "90a41246c0c3411eaa5e12a5a710a80f").toLowerCase();
    }


    public static String getFriendlyTimeSpanByNow(final long millis) {
        long wee = getWeeOfToday();
        if (millis >= wee) {
            return "今天";
        } else if (millis >= wee - TimeConstants.DAY) {
            return "昨天";
        } else if (millis >= wee - 2 * TimeConstants.DAY) {
            return "前天";
        } else if (isCurrentYesr(millis)) {
            SimpleDateFormat format = new SimpleDateFormat("MM-dd");
            return format.format(millis);
        } else {
            return String.format("%tF", millis);
        }
    }


    static boolean isCurrentYesr(long input) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = null;
        try {
            String dateString = format.format(input);
            date = format.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar inputCalendar = Calendar.getInstance();
        inputCalendar.setTime(date);
        Calendar now = Calendar.getInstance();
        return inputCalendar.get(Calendar.YEAR) == now.get(Calendar.YEAR);
    }

    private static long getWeeOfToday() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    public static String unixMillis2String(Long m, String rex) {
        Date d = new Date(m);
        SimpleDateFormat format = new SimpleDateFormat(rex);
        return format.format(d);
    }

    public static String getNameFromUrl(String url) {
        if (TextUtils.isEmpty(url)) {
            return "";
        }
        return url.substring(url.lastIndexOf("/"));
    }


    public static String formatTimeForCountdown(long seconds) {
        return String.format(" %02d:%02d:%02d", seconds / 60 / 60 % 60, seconds / 60 % 60, seconds % 60);
    }

    public static String formatTimeForCountdownSimple(long seconds) {
        return String.format("%02d:%02d", seconds / 60, seconds % 60);
    }
}


