package com.example.rahael.sunshine.app;

import android.support.v4.app.NotificationCompat;

import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

/**
 * Created by Mojo on 3/7/15.
 */
public class WearableCommunication {

    private static int count = 0;
    private static final String NOTIFICATION = "com.example.notification";
    private static final String COUNT_KEY = "com.example.key.count";

    public static void increaseCounter(DataMap dataMap) {
        PutDataMapRequest putDataMapReq = PutDataMapRequest.create("/count");
        putDataMapReq.getDataMap().putDataMap(NOTIFICATION, dataMap);
        putDataMapReq.getDataMap().putInt(COUNT_KEY, ++count);
        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
        PendingResult<DataApi.DataItemResult> pendingResult =
                Wearable.DataApi.putDataItem(MainActivity.mApiClient, putDataReq);
    }
}