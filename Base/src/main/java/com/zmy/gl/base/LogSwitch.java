package com.zmy.gl.base;

public class LogSwitch {
    private static boolean LOG_OPENED = true;

    public static boolean isLogOpened() {
        return LOG_OPENED;
    }

    public static void setLogOpened(boolean logOpend) {
        LOG_OPENED = logOpend;
    }
}
