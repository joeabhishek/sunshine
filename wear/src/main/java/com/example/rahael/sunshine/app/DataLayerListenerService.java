package com.example.rahael.sunshine.app;

import android.app.Notification;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

public class DataLayerListenerService extends WearableListenerService {
    private GoogleApiClient mApiClient;
    private static final int WEATHER_NOTIFICATION_ID = 3004;
    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        DataMap dataMap;
        String NOTIFICATION = "com.example.notification";
        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                // DataItem changed
                DataItem item = event.getDataItem();
                dataMap = DataMapItem.fromDataItem(event.getDataItem()).getDataMap();
                DataMap notification_properties = dataMap.getDataMap(NOTIFICATION);
                String iconName = notification_properties.getString("icon");
                int iconId = getResources().getIdentifier(iconName, "drawable", getPackageName());
                //Asset largeIconAsset = notification_properties.getAsset("largeIcon");
                //Bitmap largeIcon = loadBitmapFromAsset(largeIconAsset);
                long[] vibrate_pattern = notification_properties.getLongArray("vibrate");
                int priority = notification_properties.getInt("priority");
                if (item.getUri().getPath().compareTo("/count") == 0) {
                    Notification notify = new NotificationCompat.Builder(this)
                            .setContentTitle(notification_properties.getString("title"))
                            .setContentText(notification_properties.getString("content"))
                            .setColor(notification_properties.getInt("color"))
                            .setSmallIcon(iconId)
                            //.setLargeIcon(largeIcon)
                            .setVibrate(vibrate_pattern)
                            .setPriority(priority)
                            .build();
                    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
                    notificationManager.notify(WEATHER_NOTIFICATION_ID, notify);
                }
            } else if (event.getType() == DataEvent.TYPE_DELETED) {
                // DataItem deleted
            }
        }


    }

    public Bitmap loadBitmapFromAsset(Asset asset) {
        if (asset == null) {
            throw new IllegalArgumentException("Asset must be non-null");
        }
        ConnectionResult result =
                mApiClient.blockingConnect(12000, TimeUnit.MILLISECONDS);
        if (!result.isSuccess()) {
            return null;
        }
        // convert asset into a file descriptor and block until it's ready
        InputStream assetInputStream = Wearable.DataApi.getFdForAsset(
                mApiClient, asset).await().getInputStream();
        mApiClient.disconnect();

        if (assetInputStream == null) {
            Log.w("NULL INPUT STREAM", "Requested an unknown Asset.");
            return null;
        }
        // decode the stream into a bitmap
        return BitmapFactory.decodeStream(assetInputStream);
    }

}