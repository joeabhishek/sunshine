package com.example.rahael.sunshine.app;

/**
 * Created by Mojo on 4/1/15.
 */
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class ErrorActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_error);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.error, menu);
        return true;
    }

}