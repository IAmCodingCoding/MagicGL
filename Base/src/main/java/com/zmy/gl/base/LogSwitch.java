package com.zmy.gl.base;

import android.util.Log;

public class LogSwitch {
    private static boolean LOG_OPENED = true;

    public static boolean isLogOpened() {
        return LOG_OPENED;
    }

    public static void setLogOpened(boolean logOpend) {
        LOG_OPENED = logOpend;
    }


    public static void v(String tag, String msg) {
        if (LOG_OPENED) Log.v(tag, msg);
    }

    public static void v(String tag, String msg, Throwable tr) {
        if (LOG_OPENED) Log.v(tag, msg, tr);
    }

    public static void d(String tag, String msg) {
        if (LOG_OPENED) Log.d(tag, msg);
    }

    public static void d(String tag, String msg, Throwable tr) {
        if (LOG_OPENED) Log.d(tag, msg, tr);
    }

    public static void i(String tag, String msg) {
        if (LOG_OPENED) Log.i(tag, msg);
    }

    public static void i(String tag, String msg, Throwable tr) {
        if (LOG_OPENED) Log.i(tag, msg, tr);
    }

    public static void w(String tag, String msg) {
        if (LOG_OPENED) Log.w(tag, msg);
    }

    public static void w(String tag, String msg, Throwable tr) {
        if (LOG_OPENED) Log.w(tag, msg, tr);
    }

    public static void w(String tag, Throwable tr) {
        if (LOG_OPENED) Log.w(tag, tr);
    }

    public static void e(String tag, String msg) {
        if (LOG_OPENED) Log.e(tag, msg);
    }

    public static void e(String tag, String msg, Throwable tr) {
        if (LOG_OPENED) Log.e(tag, msg, tr);
    }

}
