package cn.vimer.kpush;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.util.UUID;

/**
 * Created by dantezhu on 15-4-13.
 */
public class DeviceUtil {
    private static String packageName = null;
    private static int appVersion = 0;
    private static String deviceId = null;
    private static int osVersion = 0;
    private static String deviceName = null;
    private static Drawable appIcon = null;
    private static int appIconId = 0;

    private static Context context = null;

    public static void init(Context context) {
        DeviceUtil.context = context;

        initVals();

        Log.v(Constants.LOG_TAG, String.format(
                        "packageName: %s, appVersion: %s, deviceId: %s, osVersion: %s, deviceName: %s",
                        packageName, appVersion, deviceId, osVersion, deviceName
                )
        );

    }

    private static void initVals() {
        packageName = context.getPackageName();

        try
        {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(packageName, PackageManager.GET_CONFIGURATIONS);
            appVersion = packageInfo.versionCode;
            appIcon = packageInfo.applicationInfo.loadIcon(context.getPackageManager());
            appIconId = packageInfo.applicationInfo.icon;
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

    public static Drawable getAppIcon() {
        return appIcon;
    }

    public static int getAppIconId() {
        return appIconId;
    }

}
