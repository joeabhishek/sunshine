package com.example.rahael.sunshine.app;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.example.rahael.sunshine.app.sync.SunshineSyncAdapter;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.List;

public class MainActivity extends ActionBarActivity implements
        ForecastFragment.Callback,
        DataApi.DataListener,
        GoogleApiClient.ConnectionCallbacks{

    private final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final String DETAILFRAGMENT_TAG = "DFTAG";

    private boolean mTwoPane;
    private String mLocation;

    private static final String START_ACTIVITY = "/start_activity";
    private static final String WEAR_MESSAGE_PATH = "/message";
    public static GoogleApiClient mApiClient;

    private static int count = 0;
    private static final String COUNT_KEY = "com.example.key.count";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Intent intnet = new Intent("com.hmkcode.android.USER_ACTION");
        sendBroadcast(intnet);
        super.onCreate(savedInstanceState);
        mLocation = Utility.getPreferredLocation(this);

        setContentView(R.layout.activity_main);
        if (findViewById(R.id.weather_detail_container) != null) {
            // The detail container view will be present only in the large-screen layouts
            // (res/layout-sw600dp). If this view is present, then the activity should be
            // in two-pane mode.
            mTwoPane = true;
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.weather_detail_container, new DetailFragment(), DETAILFRAGMENT_TAG)
                        .commit();
            }
        } else {
            mTwoPane = false;
            getSupportActionBar().setElevation(0f);
        }

        ForecastFragment forecastFragment =  ((ForecastFragment)getSupportFragmentManager()
                .findFragmentById(R.id.fragment_forecast));
        forecastFragment.setUseTodayLayout(!mTwoPane);

        SunshineSyncAdapter.initializeSyncAdapter(this);
        initGoogleApiClient();


        // This is Google glass Bluetooth Connection
        if (!myGlassRunning()){

            try {
                JoeMessageUtil.sendInfoRequest();
            } catch (Exception e1) {

                e1.printStackTrace();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)  {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        if(id == R.id.action_poke_glass) {
            try {
                JoeMessageUtil.sendText("Message Test", "Yo Glass!!");
            } catch (Exception e) {
                Log.d("Sending to Glass Failed", "Yest its true");
                e.printStackTrace();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        myGlassRunning();
        String location = Utility.getPreferredLocation( this );
        // update the location in our second pane using the fragment manager
        if (location != null && !location.equals(mLocation)) {
            ForecastFragment ff = (ForecastFragment)getSupportFragmentManager().findFragmentById(R.id.fragment_forecast);
            if ( null != ff ) {
                ff.onLocationChanged();
            }
            DetailFragment df = (DetailFragment)getSupportFragmentManager().findFragmentByTag(DETAILFRAGMENT_TAG);
            if ( null != df ) {
                df.onLocationChanged(location);
            }
            mLocation = location;
        }
        mApiClient.connect();
    }

    @Override
    public void onItemSelected(Uri contentUri) {
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle args = new Bundle();
            args.putParcelable(DetailFragment.DETAIL_URI, contentUri);

            DetailFragment fragment = new DetailFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.weather_detail_container, fragment, DETAILFRAGMENT_TAG)
                    .commit();
        } else {
            Intent intent = new Intent(this, DetailActivity.class)
                    .setData(contentUri);
            startActivity(intent);
        }
    }

    private void initGoogleApiClient() {
        mApiClient = new GoogleApiClient.Builder( this )
                .addApi( Wearable.API )
                .addConnectionCallbacks(this)
                .build();

        mApiClient.connect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Wearable.DataApi.removeListener(mApiClient, this);
        mApiClient.disconnect();
    }


    @Override
    public void onConnected(Bundle bundle) {
        Wearable.DataApi.addListener(mApiClient, this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Wearable.DataApi.removeListener(mApiClient, this);
        mApiClient.disconnect();
    }

    public void onDataChanged(DataEventBuffer dataEvents) {
        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                // DataItem changed
                DataItem item = event.getDataItem();
                if (item.getUri().getPath().compareTo("/count") == 0) {
                    NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes( mApiClient ).await();
                    for(Node node : nodes.getNodes()) {
                        MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(
                                mApiClient, node.getId(), WEAR_MESSAGE_PATH, "Hello".getBytes() ).await();
                    }
                }
            } else if (event.getType() == DataEvent.TYPE_DELETED) {
                // DataItem deleted
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    boolean myglassrunning = false;
    public boolean myGlassRunning(){

        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        // am.killBackgroundProcesses("com.google.glass.companion");
        List<ActivityManager.RunningServiceInfo> pids = am.getRunningServices(9999);
        for (int i = 0; i < pids.size(); i++) {
            ActivityManager.RunningServiceInfo info = pids.get(i);
            //Log.d("JOE",info.service.getClassName());
            if (info.service.getClassName().equals("com.google.glass.companion.service.CompanionService")){
                Log.d("JOE", "MyGlass is running");
                myglassrunning = true;
                /*tv = (TextView) findViewById(R.id.textView2);
                tv.setText("MyGlass is running. It blocks the Bluetooth channel needed to talk to glass.  In order to use this app please Force Stop or uninstall it.");*/
                return true;
            }
        }

        return false;

    }
}