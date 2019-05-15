package com.jchou.sdk;

import android.app.Application;
import android.content.Context;

public class App extends Application {
    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext=getApplicationContext();
        SdkManager.getInstance().init(this);
    }

    public static Context getContext() {
        return mContext;
    }
}
