package com.jchou.sdk.utils;

import android.util.Log;

import com.orhanobut.logger.Logger;

/**
 * Log统一管理类
 */
public class L {

    private L() {
        /* cannot be instantiated */
        throw new UnsupportedOperationException("cannot be instantiated");
    }

    public static boolean isDebug = false;// 是否需要打印bug，可以在application的onCreate函数里面初始化
    private static final String TAG = "jc";


    // 下面四个是默认tag的函数
    public static void i(String msg) {
        if (isDebug)
//            Log.i(TAG, msg);
        Logger.i(msg);
    }

    public static void d(String msg) {
        if (isDebug)
//            Log.d(TAG, msg);
        Logger.d( msg);
    }

    public static void e(String msg) {
        if (isDebug)
//            Log.e(TAG, msg);
        Logger.e( msg);
    }
    public static void e(Throwable t, String msg) {
        if (isDebug)
//            Log.e(TAG, msg);
        Logger.e( t,msg);
    }

    public static void json(String msg) {
        if (isDebug)
//            Log.e(TAG, msg);
        Logger.json(msg);
    }

    public static void v(String msg) {
        if (isDebug)
//            Log.v(TAG, msg);
        Logger.v( msg);
    }

    public static void w(String msg) {
        if (isDebug)
//            Log.w(TAG, msg);
            Logger.w( msg);
    }

    // 下面是传入自定义tag的函数
    public static void i(String tag, String msg) {
        if (isDebug)
            Log.i(tag, msg);
    }

    public static void d(String tag, String msg) {
        if (isDebug)
            Log.d(tag, msg);
    }

    public static void e(String tag, String msg) {
        if (isDebug)
            Log.e(tag, msg);
    }

    public static void v(String tag, String msg) {
        if (isDebug)
            Log.v(tag, msg);
    }

    public static void w(String tag, String msg) {
        if (isDebug)
            Log.w(tag, msg);
    }
}