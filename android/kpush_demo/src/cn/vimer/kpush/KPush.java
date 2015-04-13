package cn.vimer.kpush;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.util.UUID;

/**
 * Created by dantezhu on 15-4-13.
 */
public class KPush {

    private static Context context = null;
    // 默认从清单文件里获取
    private static String channel = null;
    private static String appkey = null;
    private static String packageName = null;
    private static int appVersion = 0;
    private static String deviceId = null;
    private static int osVersion = 0;
    private static String deviceName = null;

    public static void init(Context context) {
        KPush.context = context;

        initVals();

        Log.v(Constants.LOG_TAG, String.format(
                "channel:%s, appkey: %s, packageName: %s, appVersion: %s, deviceId: %s, osVersion: %s, deviceName: %s",
                channel, appkey, packageName, appVersion, deviceId, osVersion, deviceName
                )
        );

        startService();
    }

    private static void initVals() {
        channel = Utils.getMetaValue(context, "KPUSH_CHANNEL");
        appkey = Utils.getMetaValue(context, "KPUSH_APPKEY");

        packageName = context.getPackageName();

        try
        {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(packageName, PackageManager.GET_CONFIGURATIONS);
            appVersion = packageInfo.versionCode;
        }
        catch (Exception e) {
            Log.e(Constants.LOG_TAG, "get versionCode fail");
        }

        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.PREFS_NAME, 0);

        deviceId = sharedPreferences.getString("device_id", null);

        if (deviceId == null || deviceId.isEmpty()) {
            deviceId = ((TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();

            if (deviceId == null || deviceId.isEmpty()) {
                deviceId = UUID.randomUUID().toString();
            }
        }

        // apply
        sharedPreferences.edit().putString("device_id", deviceId).apply();

        deviceName = Build.MODEL;
        osVersion = Build.VERSION.SDK_INT;
    }


    public static Context getContext() {
        return context;
    }

    public static String getChannel() {
        return channel;
    }

    public static String getAppkey() {
        return appkey;
    }

    public static String getPackageName() {
        return packageName;
    }

    public static int getAppVersion() {
        return appVersion;
    }

    public static String getDeviceId() {
        return deviceId;
    }

    public static int getOsVersion() {
        return osVersion;
    }

    public static String getDeviceName() {
        return deviceName;
    }

    private static void startService() {
        // 启动service
        Intent intent = new Intent(context, PushService.class);
        context.startService(intent);
    }
}
