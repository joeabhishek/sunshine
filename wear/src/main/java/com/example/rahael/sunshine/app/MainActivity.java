package com.example.rahael.sunshine.app;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.wearable.view.CardFragment;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;

public class MainActivity extends Activity implements
        GoogleApiClient.ConnectionCallbacks,
        DataApi.DataListener{

    private static final String WEAR_MESSAGE_PATH = "/message";
    private GoogleApiClient mApiClient;
    private ArrayAdapter<String> mAdapter;
    private static final int WEATHER_NOTIFICATION_ID = 3004;

    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mTextView = (TextView) stub.findViewById(R.id.text);
            }
        });

        initGoogleApiClient();
    }

    private void initGoogleApiClient() {
        mApiClient = new GoogleApiClient.Builder( this )
                .addApi( Wearable.API )
                .addConnectionCallbacks( this )
                .build();

        if( mApiClient != null && !( mApiClient.isConnected() || mApiClient.isConnecting() ) )
            mApiClient.connect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if( mApiClient != null && !( mApiClient.isConnected() || mApiClient.isConnecting() ) )
            mApiClient.connect();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mApiClient.connect();
    }

//    @Override
//    public void onMessageReceived( final MessageEvent messageEvent ) {
//        runOnUiThread( new Runnable() {
//            @Override
//            public void run() {
//                if( messageEvent.getPath().equalsIgnoreCase( WEAR_MESSAGE_PATH ) ) {
//                    mAdapter.add(new String(messageEvent.getData()));
//                    mAdapter.notifyDataSetChanged();
//                }
//            }
//        });
//        FragmentManager fragmentManager = this.getFragmentManager();
//        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//        CardFragment cardFragment = CardFragment.create(getString(R.string.hello_round),
//                getString(R.string.hello_square),
//                R.drawable.art_clear);
//        fragmentTransaction.add(R.id.frame_layout, cardFragment);
//        fragmentTransaction.commit();
//
//       Notification notify = new NotificationCompat.Builder(this)
//                .setContentTitle("Hello")
//                .setContentText("oh god")
//                .setSmallIcon(R.drawable.ic_launcher)
//                .build();
//        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
//        notificationManager.notify(WEATHER_NOTIFICATION_ID, notify);
//        TextView myAwesomeTextView = (TextView)findViewById(R.id.text);
//        myAwesomeTextView.setText("Hello Again $%^&");
//    }

    @Override
    public void onConnected(Bundle bundle) {
        Wearable.DataApi.addListener(mApiClient, this);
    }

    @Override
    protected void onStop() {
        if ( mApiClient != null ) {
            Wearable.DataApi.removeListener( mApiClient, this );
            if ( mApiClient.isConnected() ) {
                mApiClient.disconnect();
            }
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if( mApiClient != null )
            mApiClient.unregisterConnectionCallbacks( this );
        super.onDestroy();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    protected void onPause() {
        super.onPause();
        Wearable.DataApi.removeListener(mApiClient, this);
        mApiClient.disconnect();
    }

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
                Asset largeIconAsset = notification_properties.getAsset("largeIcon");
                Bitmap largeIcon = loadBitmapFromAsset(largeIconAsset);
                long[] vibrate_pattern = notification_properties.getLongArray("vibrate");
                int priority = notification_properties.getInt("priority");
                if (item.getUri().getPath().compareTo("/count") == 0) {
                           Notification notify = new NotificationCompat.Builder(this)
                                    .setContentTitle(notification_properties.getString("title"))
                                    .setContentText(notification_properties.getString("content"))
                                    .setColor(notification_properties.getInt("color"))
                                    .setSmallIcon(iconId)
                                    .setLargeIcon(largeIcon)
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
                mApiClient.blockingConnect(500000, TimeUnit.MILLISECONDS);
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