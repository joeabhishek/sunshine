package com.example.rahael.sunshine.app;

/**
 * Created by Mojo on 2/11/15.
 */
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Utility {
    public static String getPreferredLocation(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_location_key),
                context.getString(R.string.pref_location_default));
    }
}