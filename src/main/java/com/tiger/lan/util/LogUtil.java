package com.tiger.lan.util;

import android.util.Log;

/**
 * Created by tiger on 2019/2/12.
 */

public class LogUtil {
    public static boolean isDebug = true ;
    public static void v(String tag, String msg) {
        if(isDebug) {
            Log.v(tag, msg);
        }
    }
    public static void d(String tag, String msg) {
        if(isDebug) {
            Log.d(tag, msg);
        }
    }
    public static void w(String tag, String msg) {
        if(isDebug) {
            Log.w(tag, msg);
        }
    }
    public static void i(String tag, String msg) {
        if(isDebug) {
            Log.i(tag, msg);
        }
    }
    public static void e(String tag, String msg) {
        if(isDebug) {
            Log.e(tag, msg);
        }
    }
}
