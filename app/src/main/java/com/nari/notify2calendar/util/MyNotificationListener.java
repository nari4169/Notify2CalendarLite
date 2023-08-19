package com.nari.notify2calendar.util;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MyNotificationListener extends NotificationListenerService {

    public final static String TAG = "MyNotificationListener";
    private static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private Context context;

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        super.onNotificationRemoved(sbn);

        Log.d(TAG, "onNotificationRemoved ~ " +
                " packageName: " + sbn.getPackageName() +
                " id: " + sbn.getId());
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        super.onNotificationPosted(sbn);

        context = getApplicationContext() ;

        Notification notification = sbn.getNotification();
        Bundle extras = sbn.getNotification().extras;
        String title = extras.getString(Notification.EXTRA_TITLE);
        CharSequence text = extras.getCharSequence(Notification.EXTRA_TEXT);
        CharSequence subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT);
        Icon smallIcon = notification.getSmallIcon();
        Icon largeIcon = notification.getLargeIcon();

        long ttl = sbn.getPostTime(); // 1562764174458
        Log.i(TAG, "ttl=" + ttl);
        Date today = new Date(ttl);
        DateFormat format = DateFormat.getDateInstance(DateFormat.FULL, Locale.KOREA);
        String formatted = format.format(today);

        com.nari.notify2calendar.util.StringSplitUtil stringSplitUtil = new com.nari.notify2calendar.util.StringSplitUtil(context) ;

        int iPos = 0 ;

        if (title != null && !"".equals(title) && title.indexOf("유선 충전") < 0) {

            Log.d(TAG, "onNotificationPosted ~ " +
                    "\n packageName: " + sbn.getPackageName() +
                    "\n id: " + sbn.getId() +
                    "\n postTime: " + sbn.getPostTime() +
                    "\n title: " + title +
                    "\n text : " + String.join("", text) +
                    "\n formatted : " + formatted +
                    "\n subText: " + subText);

            stringSplitUtil.doStringSpilt(title, String.join("", text));
        }

        /*if (iPos > 0) {
           sendToActivity(context, title, String.join("", text), today);
        }*/
    }

    @Override
    public IBinder onBind(Intent intent) {
        return super.onBind(intent);
    }

    /*// 액티비티로 메세지의 내용을 전달해줌
    private void sendToActivity(Context context, String sender, String contents, Date receivedDate){
        Intent intent = new Intent(context, MainActivity.class);

        // Flag 설정
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_SINGLE_TOP| Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // 메세지의 내용을 Extra에 넣어줌
        intent.putExtra("sender", sender);
        intent.putExtra("contents", contents);
        intent.putExtra("receivedDate", format.format(receivedDate));

        context.startActivity(intent);
    }*/
}
