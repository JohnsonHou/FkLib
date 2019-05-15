package com.jchou.sdk;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.jchou.sdk.models.ContactsInfo;
import com.jchou.sdk.ui.ContactListActivity;
import com.jchou.sdk.ui.FaceAuthActivity;
import com.jchou.sdk.ui.FaceCompareActivity;
import com.mylhyl.acp.Acp;
import com.mylhyl.acp.AcpListener;
import com.mylhyl.acp.AcpOptions;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.FormatStrategy;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.PrettyFormatStrategy;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;

public class SdkManager {
    private static SdkManager INSTANCE;

    public Context getmContext() {
        return mContext;
    }

    // 程序的Context对象
    private Context mContext;


    private final static String[] MULTI_PERMISSIONS = new String[]{
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION};

    private LocationManager locationManager;

    private final String TAG = "SdkManager";

    private SdkManager() {
    }

    /**
     * 获取CrashHandler实例 ,单例模式
     */
    public static SdkManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new SdkManager();
        }
        return INSTANCE;
    }

    /**
     * 初始化,注册Context对象,
     * 获取系统默认的UncaughtException处理器,
     * 设置该CrashHandler为程序的默认处理器
     *
     * @param ctx ctx
     */
    public void init(Context ctx) {
        mContext = ctx;
        setLogger();
        //获取定位服务
        locationManager = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);
    }

    public interface LocationListener {
        void locationEnd(double longitude, double latitude);

        void locationError();
    }

    public void getLocation(final Activity ctx, final LocationListener locationListener) {
        Acp.getInstance(ctx).request(new AcpOptions.Builder()
                        .setPermissions(MULTI_PERMISSIONS)
                        .build(),
                new AcpListener() {
                    @Override
                    public void onGranted() {
                        String providerName = locationManager.getBestProvider(createFineCriteria(), true);
                        @SuppressLint("MissingPermission")
                        Location location = locationManager.getLastKnownLocation(providerName); // 通过GPS获取位置
//        Location location = locationManager.getLastKnownLocation(locateType); // 通过GPS获取位置
                        if (location != null) {
                            double longitude = location.getLongitude();
                            double latitude = location.getLatitude();
                            if (locationListener != null) {
                                locationListener.locationEnd(longitude, latitude);
                            }
                        } else {
                            if (locationListener != null) {
                                locationListener.locationError();
                            }
                        }
                    }

                    @Override
                    public void onDenied(List<String> permissions) {
                        @SuppressLint("MissingPermission")
                        Location location = locationManager.getLastKnownLocation(locationManager.NETWORK_PROVIDER); // 通过NETWORK获取位置
                        if (location != null) {
                            double longitude = location.getLongitude();
                            double latitude = location.getLatitude();
                            if (locationListener != null) {
                                locationListener.locationEnd(longitude, latitude);
                            }
                        } else {
                            if (locationListener != null) {
                                locationListener.locationError();
                            }
                        }
                    }
                });

    }

    private Criteria createFineCriteria() {
        Criteria c = new Criteria();
        c.setAccuracy(Criteria.ACCURACY_COARSE);//低精度，如果设置为高精度，依然获取不了location。
        c.setAltitudeRequired(false);//不包含高度信息
        c.setBearingRequired(false);//不包含方位信息
        c.setSpeedRequired(false);//不包含速度信息
        c.setCostAllowed(true);//允许付费
        c.setPowerRequirement(Criteria.POWER_MEDIUM);//高耗电
        return c;
    }


    public String getIPAddress(Context context) {
        NetworkInfo info = ((ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if (info != null && info.isConnected()) {
            if (info.getType() == ConnectivityManager.TYPE_MOBILE) {//当前使用2G/3G/4G网络
                try {
//Enumeration<NetworkInterface> en=NetworkInterface.getNetworkInterfaces();
                    for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                        NetworkInterface intf = en.nextElement();
                        for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                            InetAddress inetAddress = enumIpAddr.nextElement();
                            if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                                return inetAddress.getHostAddress();
                            }
                        }
                    }
                } catch (SocketException e) {
                    e.printStackTrace();
                }


            } else if (info.getType() == ConnectivityManager.TYPE_WIFI) {//当前使用无线网络
                WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                String ipAddress = intIP2StringIP(wifiInfo.getIpAddress());//得到IPV4地址
                return ipAddress;
            }
        } else {
            //当前无网络连接,请在设置中打开网络
            Toast.makeText(mContext, "当前无网络连接,请在设置中打开网络", Toast.LENGTH_LONG).show();
        }
        return null;
    }


    /**
     *      * 将得到的int类型的IP转换为String类型
     *      *
     *      * @param ip
     *      * @return
     *      
     */
    private String intIP2StringIP(int ip) {
        return (ip & 0xFF) + "." +
                ((ip >> 8) & 0xFF) + "." +
                ((ip >> 16) & 0xFF) + "." +
                (ip >> 24 & 0xFF);
    }


    /**
     * 获取MAC地址
     *
     * @param context
     * @return
     */
    public String getMacAddress(Context context) {
        String mac = "02:00:00:00:00:00";
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            mac = getMacDefault(context);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            mac = getMacFromFile();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mac = getMacFromHardware();
        }
        return mac;
    }

    /**
     * Android  6.0 之前（不包括6.0）
     * 必须的权限  <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
     *
     * @param context
     * @return
     */
    private String getMacDefault(Context context) {
        String mac = "02:00:00:00:00:00";
        if (context == null) {
            return mac;
        }

        WifiManager wifi = (WifiManager) context.getApplicationContext()
                .getSystemService(Context.WIFI_SERVICE);
        if (wifi == null) {
            return mac;
        }
        WifiInfo info = null;
        try {
            info = wifi.getConnectionInfo();
        } catch (Exception e) {
        }
        if (info == null) {
            return null;
        }
        mac = info.getMacAddress();
        if (!TextUtils.isEmpty(mac)) {
            mac = mac.toUpperCase(Locale.ENGLISH);
        }
        return mac;
    }

    /**
     * Android 6.0（包括） - Android 7.0（不包括）
     *
     * @return
     */
    private String getMacFromFile() {
        String WifiAddress = "02:00:00:00:00:00";
        try {
            WifiAddress = new BufferedReader(new FileReader(new File("/sys/class/net/wlan0/address"))).readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return WifiAddress;
    }


    /**
     * 遍历循环所有的网络接口，找到接口是 wlan0
     * 必须的权限 <uses-permission android:name="android.permission.INTERNET" />
     *
     * @return
     */
    private String getMacFromHardware() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "";
                }

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(String.format("%02X:", b));
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "02:00:00:00:00:00";
    }


    private static final int REQUEST_CONTACT = 1000;


    private static final int REQUEST_COMPARE = 1001;


    public void liveAuthen(final Activity ctx) {
        Acp.getInstance(ctx.getApplicationContext()).request(new AcpOptions.Builder()
                        .setPermissions(Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .build(),
                new AcpListener() {
                    @Override
                    public void onGranted() {
                        ctx.startActivityForResult(new Intent(ctx, FaceCompareActivity.class),REQUEST_AUTH);
                    }

                    @Override
                    public void onDenied(List<String> permissions) {
                        Toast.makeText(ctx.getApplicationContext(), "权限拒绝", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public static final int REQUEST_AUTH = 10807;


    private void setLogger() {
        FormatStrategy formatStrategy = PrettyFormatStrategy.newBuilder()
                .showThreadInfo(true)  // 可选）是否显示线程信息。 默认值为true
//                .methodCount(2)         // （可选）要显示的方法行数。 默认2
                .methodOffset(7)        // （可选）隐藏内部方法调用到偏移量。 默认5
//                .logStrategy(customLog) // （可选）更改要打印的日志策略。 默认LogCat
                .tag("jc")   // （可选）每个日志的全局标记。 默认PRETTY_LOGGER
                .build();
        Logger.addLogAdapter(new AndroidLogAdapter(formatStrategy) {
            //项目上线前，可以实现以下方法，以保证上线后不输出日志。
            @Override
            public boolean isLoggable(int priority, String tag) {
                return super.isLoggable(priority, tag);
            }
        });

        /**
         * 此方法用于将日志保存在文件中
         * Logger.addLogAdapter(new DiskLogAdapter());
         *
         *
         * 如果你想保存指定的TAG，增加以下实现：
         *
         * FormatStrategy formatStrategy = CsvFormatStrategy.newBuilder()
         .tag("custom")
         .build();

         Logger.addLogAdapter(new DiskLogAdapter(formatStrategy));
         */
    }
}
