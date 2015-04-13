package cn.vimer.kpush;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import cn.vimer.ferry.Ferry;
import cn.vimer.kpush_demo.MainActivity;
import cn.vimer.kpush_demo.R;
import cn.vimer.netkit.Box;
import cn.vimer.netkit.IBox;
import org.json.JSONObject;

/**
 * Created by dantezhu on 15-4-13.
 */
public class PushService extends Service {

    private Handler handler;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(Constants.LOG_TAG, "onCreate");

        handler = new Handler();

        regEventCallback();

        Ferry.getInstance().init("115.28.224.64", 29000);
        Ferry.getInstance().start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 每次发intent都会进来，可以重复进入
        Log.d(Constants.LOG_TAG, "onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(Constants.LOG_TAG, "onDestory");
    }

    private void regEventCallback() {
        Ferry.getInstance().addEventCallback(new Ferry.CallbackListener() {
            @Override
            public void onOpen() {
                Log.d(Constants.LOG_TAG, "onOpen");

                userRegister();
            }

            @Override
            public void onRecv(IBox ibox) {
                Log.d(Constants.LOG_TAG, String.format("onRecv, box: %s", ibox));
                Box box = (Box) ibox;
                if (box.cmd == Proto.EVT_NOTIFICATION) {
                    registerNtf();
                }
            }

            @Override
            public void onClose() {
                Log.d(Constants.LOG_TAG, "onClose");
                Ferry.getInstance().connect();
            }

            @Override
            public void onError(int code, IBox ibox) {
                Log.d(Constants.LOG_TAG, String.format("onError, code: %s, box: %s", code, ibox));
            }

        }, this, "main");
    }

    private void userRegister() {

        Log.d(Constants.LOG_TAG, "userRegister");

        Box box = new Box();
        box.cmd = Proto.CMD_REGISTER;
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("os", Constants.OS);
            jsonObject.put("sdk_version", Constants.SDK_VERSION);
            jsonObject.put("appkey", KPush.getAppkey());
            jsonObject.put("channel", KPush.getChannel());
            jsonObject.put("device_id", DeviceUtil.getDeviceId());
            jsonObject.put("os_version", DeviceUtil.getOsVersion());
            jsonObject.put("app_version", DeviceUtil.getAppVersion());
            jsonObject.put("device_name", DeviceUtil.getDeviceName());
            jsonObject.put("package_name", DeviceUtil.getPackageName());
        } catch (Exception e) {
        }

        Log.d(Constants.LOG_TAG, "" + KPush.getAppkey());
        Log.d(Constants.LOG_TAG, jsonObject.toString());

        byte[] body = Utils.packData(jsonObject);
        if (body == null) {
            return;
        }

        box.body = body;

        Ferry.getInstance().send(box, new Ferry.CallbackListener() {
            @Override
            public void onSend(IBox ibox) {
                Log.d(Constants.LOG_TAG, String.format("onSend, box: %s", ibox));
            }

            @Override
            public void onRecv(IBox ibox) {
                Log.d(Constants.LOG_TAG, String.format("onRecv, box: %s", ibox));
                Box box = (Box) ibox;
                // Log.d(Constants.LOG_TAG, "data: " + Utils.unpackData(box.body));

                if (box.ret != 0) {
                    // 几秒后再重试
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            userRegister();
                        }
                    }, Constants.ERROR_RETRY_INTERVAL * 1000);
                }
            }

            @Override
            public void onError(int code, IBox ibox) {
                Log.d(Constants.LOG_TAG, String.format("onError, code: %s, box: %s", code, ibox));
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        userRegister();
                    }
                }, Constants.ERROR_RETRY_INTERVAL * 1000);
            }

            @Override
            public void onTimeout() {
                Log.d(Constants.LOG_TAG, "onTimeout");

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        userRegister();
                    }
                }, Constants.ERROR_RETRY_INTERVAL * 1000);
            }
        }, 5, this);
    }

    public void registerNtf() {
        //消息通知栏
        //定义NotificationManager
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);
        //定义通知栏展现的内容信息
        CharSequence tickerText = "我的通知栏标题";
        long when = System.currentTimeMillis();
        Notification notification = new Notification(DeviceUtil.getAppIconId(), tickerText, when);

        //定义下拉通知栏时要展现的内容信息
        Context context = getApplicationContext();
        CharSequence contentTitle = "我的通知栏标展开标题";
        CharSequence contentText = "我的通知栏展开详细内容";
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);
        notification.setLatestEventInfo(context, contentTitle, contentText,
                contentIntent);

        //用mNotificationManager的notify方法通知用户生成标题栏消息通知
        int nfyid = 1;
        mNotificationManager.notify(nfyid, notification);
    }

}
