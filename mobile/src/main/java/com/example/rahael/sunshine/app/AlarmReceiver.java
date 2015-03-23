package com.example.rahael.sunshine.app;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Created by Mojo on 3/13/15.
 */
public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Calendar now = GregorianCalendar.getInstance();
        int dayOfWeek = now.get(Calendar.DATE);
        if(dayOfWeek != 1 && dayOfWeek != 7) {
            Log.d("asd", "asd");
           /* NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(context)
                            .setSmallIcon(R.drawable.ic_launcher)
                            .setContentTitle(context.getResources().getString(R.string.message_box_title))
                            .setContentText(context.getResources().getString(R.string.message_timesheet_not_up_to_date));
            Intent resultIntent = new Intent(context, MainActivity.class);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            stackBuilder.addParentStack(MainActivity.class);
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            mBuilder.setContentIntent(resultPendingIntent);
            NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(1, mBuilder.build());*/
        }
    }
}