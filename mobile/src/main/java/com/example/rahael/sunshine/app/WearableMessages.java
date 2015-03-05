package com.example.rahael.sunshine.app;

import android.content.Context;
import android.os.Bundle;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;

/**
 * Created by Mojo on 3/3/15.
 */
/*
public class WearableMessages implements GoogleApiClient.ConnectionCallbacks {
    private static GoogleApiClient mApiClient;
    private static final String START_ACTIVITY = "/start_activity";

    public GoogleApiClient getGoogleApiClient(Context context) {
        return new GoogleApiClient.Builder(context)
                .addApi(Wearable.API)
                .build();
    }




    @Override
    protected void onDestroy() {
        super.onDestroy();
        mApiClient.disconnect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        sendMessage(START_ACTIVITY, "");
    }

    @Override
    public void onConnectionSuspended(int i) {

    }
    public static void sendMessage( final String path, final String text ) {
        new Thread( new Runnable() {
            @Override
            public void run() {
                NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes( mApiClient ).await();
                for(Node node : nodes.getNodes()) {
                    MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(
                            mApiClient, node.getId(), path, text.getBytes() ).await();
                }
            }
        }).start();
    }
}
*/
