package com.example.rahael.sunshine.app;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * A {@link PreferenceActivity} that presents a set of application settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends PreferenceActivity
        implements Preference.OnPreferenceChangeListener {

    SharedPreferences prefs = null;
    String notificationTimePreferenceKey = null;
    boolean displayNotificationsOnTime = false;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add 'general' preferences, defined in the XML file
        addPreferencesFromResource(R.xml.pref_general);

        // For all preferences, attach an OnPreferenceChangeListener so the UI summary can be
        // updated when the preference changes.
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_location_key)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_units_key)));

    }

    /**
     * Attaches a listener so the summary is always updated with the preference value.
     * Also fires the listener once, to initialize the summary (so it shows up before the value
     * is changed.)
     */
    private void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(this);

        // Trigger the listener immediately with the preference's
        // current value.
        onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {

        String stringValue = value.toString();

        if (preference instanceof ListPreference) {
            // For list preferences, look up the correct display value in
            // the preference's 'entries' list (since they have separate labels/values).
            ListPreference listPreference = (ListPreference) preference;
            int prefIndex = listPreference.findIndexOfValue(stringValue);
            if (prefIndex >= 0) {
                preference.setSummary(listPreference.getEntries()[prefIndex]);
            }
        } else {
            // For other preferences, set the summary to the value's simple string representation.
            preference.setSummary(stringValue);
        }

        return true;
    }

    public void setAlarmForNotification() throws ParseException {

        String notificationTimePreferenceKey = this.getString(R.string.pref_notifications_time_preference_key);
        String timeFromPrefs = prefs.getString(notificationTimePreferenceKey, "00:00");
        SimpleDateFormat fmt = new SimpleDateFormat("HH:mm");
        Date date = null;
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR); // get the current year
        int month = calendar.get(Calendar.MONTH); // month...
        int day = calendar.get(Calendar.DAY_OF_MONTH); // current day in the month
        try {
            date = fmt.parse(timeFromPrefs);
            int hours = date.getHours();
            int minutes = date.getMinutes();
            long time = date.getTime();
            //calendar.setTime(date);
            calendar.set(Calendar.HOUR, hours);
            calendar.set(Calendar.MINUTE, minutes);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Log.d("ssd", date.toString());
        Log.d("ssd", String.valueOf(calendar.getTime()));

        Intent alarmIntent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + 10000, pendingIntent);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        notificationTimePreferenceKey = this.getString(R.string.pref_enable_notifications_time_preference_key);
        displayNotificationsOnTime = prefs.getBoolean(notificationTimePreferenceKey,
                Boolean.parseBoolean(this.getString(R.string.pref_enable_notifications_time_preference_default)));


        if(displayNotificationsOnTime) {
            try {
                setAlarmForNotification();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }
}