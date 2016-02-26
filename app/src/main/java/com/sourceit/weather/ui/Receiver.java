package com.sourceit.weather.ui;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.NotificationCompat;

import com.sourceit.weather.R;
import com.sourceit.weather.utils.L;

/**
 * Created by User on 26.02.2016.
 */
public class Receiver extends BroadcastReceiver {
    Context context;
    private final int ID = 0;

    @Override
    public void onReceive(Context context, Intent intent) {
        L.d("onReceive");
        Intent notificationIntent = new Intent(context, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context,
                ID, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setContentIntent(contentIntent)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(context.getString(R.string.notification_title_text))
                .setContentText(String.format("%s %s", MainActivity.sp.getString(MainActivity.CITY, MainActivity.KHARKOV), MainActivity.currentTemp));
        Notification n = builder.build();
        nm.notify(ID, n);
    }
}
