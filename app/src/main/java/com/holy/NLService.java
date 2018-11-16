package com.holy;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import android.widget.Toast;


import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class NLService extends NotificationListenerService {
    public String TAG = "NLService";

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
//        super.onNotificationPosted(sbn);
        //这里只是获取了包名和通知提示信息，其他数据可根据需求取，注意空指针就行
        String pkg = sbn.getPackageName();
        long when = sbn.getNotification().when;
        Date date = new Date(when);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        sbn.getNotification().extras.getString("android.text");
        Toast.makeText(getBaseContext(),sbn.getNotification().extras.getString("android.text"),Toast.LENGTH_SHORT).show();
        Map<String,String> map = new HashMap<>();
        map.put("title","");
        map.put("content","");
        JSONObject jsonObject = new JSONObject(map);
        hTTPRequest(getApplicationContext(),jsonObject.toString());
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        Toast.makeText(getBaseContext(),sbn.getNotification().tickerText,Toast.LENGTH_SHORT).show();
    }

    @SuppressLint("WrongConstant")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        flags = START_STICKY;

        //启用前台服务，主要是startForeground()
        Notification notification = new Notification(R.drawable.ic_launcher, "监听通知"
                , System.currentTimeMillis());

        //设置通知默认效果
        notification.flags = Notification.FLAG_SHOW_LIGHTS;
        startForeground(1, notification);

        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        //读者可以修改此处的Minutes从而改变提醒间隔时间
        //此处是设置每隔55分钟启动一次
        //这是55分钟的毫秒数
        int Minutes = 55 * 60 * 1000;
        //SystemClock.elapsedRealtime()表示1970年1月1日0点至今所经历的时间
        long triggerAtTime = SystemClock.elapsedRealtime() + Minutes;
        //此处设置开启AlarmReceiver这个Service
        Intent i = new Intent(this, NLService.class);
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, 0);
        //ELAPSED_REALTIME_WAKEUP表示让定时任务的出发时间从系统开机算起，并且会唤醒CPU。
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
        return super.onStartCommand(intent, flags, startId);
    }

    public static void hTTPRequest(Context context, String content) {
        HttpURLConnection con = null;
        InputStream is = null;
        InputStreamReader reader = null;
        OutputStream os = null;
        OutputStreamWriter writer = null;
        SharedPreferences sp = context.getSharedPreferences("notification_sp",Context.MODE_PRIVATE);
        String urlStr = sp.getString("url_post","");
        if (TextUtils.isEmpty(urlStr)) {
            return;
        }
        try {
            URL url = new URL(urlStr);
            con = (HttpURLConnection) url.openConnection();
            con.setDoInput(true);
            con.setInstanceFollowRedirects(true);
            con.setConnectTimeout(15000);
            if (content != null) {
                con.setRequestMethod("POST");
                con.setDoOutput(true);
            }
            con.connect();

            if (content != null) {
                os = con.getOutputStream();
                writer = new OutputStreamWriter(os);
                writer.write(content);
                writer.flush();
                writer.close();
                writer = null;
            }
            int httpCode = con.getResponseCode();

            is = con.getInputStream();
            reader = new InputStreamReader(is);
            char[] buffer = new char[512];
            int len = 0;
            StringBuilder sb = new StringBuilder();
            while ((len = reader.read(buffer)) > 0) {
                sb.append(buffer, 0, len);
            }
            return ;
        } catch (Exception e) {

        } finally {

        }
        return;
    }

}
