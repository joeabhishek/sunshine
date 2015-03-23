package com.example.rahael.sunshine.app;

/**
 * Created by Mojo on 2/11/15.
 */
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.format.Time;

import com.example.rahael.sunshine.app.data.WeatherContract;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataMap;

import java.io.ByteArrayOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Utility {

    private static final int INDEX_WEATHER_ID = 0;
    private static final int INDEX_MAX_TEMP = 1;
    private static final int INDEX_MIN_TEMP = 2;
    private static final int INDEX_SHORT_DESC = 3;

    private static final String[] NOTIFY_WEATHER_PROJECTION = new String[] {
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC
    };

    private static final int WEATHER_NOTIFICATION_ID = 3004;

    public static String getPreferredLocation(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_location_key),
                context.getString(R.string.pref_location_default));
    }

    public static boolean isMetric(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_units_key),
                context.getString(R.string.pref_units_metric))
                .equals(context.getString(R.string.pref_units_metric));
    }

    public static String formatTemperature(Context context, double temperature) {
        // Data stored in Celsius by default.  If user prefers to see in Fahrenheit, convert
        // the values here.
        String suffix = "\u00B0";
        if (!isMetric(context)) {
            temperature = (temperature * 1.8) + 32;
        }

        // For presentation, assume the user doesn't care about tenths of a degree.
        return String.format(context.getString(R.string.format_temperature), temperature);
    }

    static String formatDate(long dateInMilliseconds) {
        Date date = new Date(dateInMilliseconds);
        return DateFormat.getDateInstance().format(date);
    }

    // Format used for storing dates in the database.  ALso used for converting those strings
    // back into date objects for comparison/processing.
    public static final String DATE_FORMAT = "yyyyMMdd";

    /**
     * Helper method to convert the database representation of the date into something to display
     * to users.  As classy and polished a user experience as "20140102" is, we can do better.
     *
     * @param context Context to use for resource localization
     * @param dateInMillis The date in milliseconds
     * @return a user-friendly representation of the date.
     */
    public static String getFriendlyDayString(Context context, long dateInMillis) {
        // The day string for forecast uses the following logic:
        // For today: "Today, June 8"
        // For tomorrow:  "Tomorrow"
        // For the next 5 days: "Wednesday" (just the day name)
        // For all days after that: "Mon Jun 8"

        Time time = new Time();
        time.setToNow();
        long currentTime = System.currentTimeMillis();
        int julianDay = Time.getJulianDay(dateInMillis, time.gmtoff);
        int currentJulianDay = Time.getJulianDay(currentTime, time.gmtoff);

        // If the date we're building the String for is today's date, the format
        // is "Today, June 24"
        if (julianDay == currentJulianDay) {
            String today = context.getString(R.string.today);
            int formatId = R.string.format_full_friendly_date;
            return String.format(context.getString(
                    formatId,
                    today,
                    getFormattedMonthDay(context, dateInMillis)));
        } else if ( julianDay < currentJulianDay + 7 ) {
            // If the input date is less than a week in the future, just return the day name.
            return getDayName(context, dateInMillis);
        } else {
            // Otherwise, use the form "Mon Jun 3"
            SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
            return shortenedDateFormat.format(dateInMillis);
        }
    }

    /**
     * Given a day, returns just the name to use for that day.
     * E.g "today", "tomorrow", "wednesday".
     *
     * @param context Context to use for resource localization
     * @param dateInMillis The date in milliseconds
     * @return
     */
    public static String getDayName(Context context, long dateInMillis) {
        // If the date is today, return the localized version of "Today" instead of the actual
        // day name.

        Time t = new Time();
        t.setToNow();
        int julianDay = Time.getJulianDay(dateInMillis, t.gmtoff);
        int currentJulianDay = Time.getJulianDay(System.currentTimeMillis(), t.gmtoff);
        if (julianDay == currentJulianDay) {
            return context.getString(R.string.today);
        } else if ( julianDay == currentJulianDay +1 ) {
            return context.getString(R.string.tomorrow);
        } else {
            Time time = new Time();
            time.setToNow();
            // Otherwise, the format is just the day of the week (e.g "Wednesday".
            SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE");
            return dayFormat.format(dateInMillis);
        }
    }

    /**
     * Converts db date format to the format "Month day", e.g "June 24".
     * @param context Context to use for resource localization
     * @param dateInMillis The db formatted date string, expected to be of the form specified
     *                in Utility.DATE_FORMAT
     * @return The day in the form of a string formatted "December 6"
     */
    public static String getFormattedMonthDay(Context context, long dateInMillis ) {
        Time time = new Time();
        time.setToNow();
        SimpleDateFormat dbDateFormat = new SimpleDateFormat(Utility.DATE_FORMAT);
        SimpleDateFormat monthDayFormat = new SimpleDateFormat("MMMM dd");
        String monthDayString = monthDayFormat.format(dateInMillis);
        return monthDayString;
    }

    public static String getFormattedWind(Context context, float windSpeed, float degrees) {
        int windFormat;
        if (Utility.isMetric(context)) {
            windFormat = R.string.format_wind_kmh;
        } else {
            windFormat = R.string.format_wind_mph;
            windSpeed = .621371192237334f * windSpeed;
        }

        // From wind direction in degrees, determine compass direction as a string (e.g NW)
        // You know what's fun, writing really long if/else statements with tons of possible
        // conditions.  Seriously, try it!
        String direction = "Unknown";
        if (degrees >= 337.5 || degrees < 22.5) {
            direction = "N";
        } else if (degrees >= 22.5 && degrees < 67.5) {
            direction = "NE";
        } else if (degrees >= 67.5 && degrees < 112.5) {
            direction = "E";
        } else if (degrees >= 112.5 && degrees < 157.5) {
            direction = "SE";
        } else if (degrees >= 157.5 && degrees < 202.5) {
            direction = "S";
        } else if (degrees >= 202.5 && degrees < 247.5) {
            direction = "SW";
        } else if (degrees >= 247.5 && degrees < 292.5) {
            direction = "W";
        } else if (degrees >= 292.5 || degrees < 22.5) {
            direction = "NW";
        }
        return String.format(context.getString(windFormat), windSpeed, direction);
    }

    /**
     * Helper method to provide the icon resource id according to the weather condition id returned
     * by the OpenWeatherMap call.
     * @param weatherId from OpenWeatherMap API response
     * @return resource id for the corresponding icon. -1 if no relation is found.
     */
    public static int getIconResourceForWeatherCondition(int weatherId) {
        // Based on weather code data found at:
        // http://bugs.openweathermap.org/projects/api/wiki/Weather_Condition_Codes
        if (weatherId >= 200 && weatherId <= 232) {
            return R.drawable.ic_storm;
        } else if (weatherId >= 300 && weatherId <= 321) {
            return R.drawable.ic_light_rain;
        } else if (weatherId >= 500 && weatherId <= 504) {
            return R.drawable.ic_rain;
        } else if (weatherId == 511) {
            return R.drawable.ic_snow;
        } else if (weatherId >= 520 && weatherId <= 531) {
            return R.drawable.ic_rain;
        } else if (weatherId >= 600 && weatherId <= 622) {
            return R.drawable.ic_snow;
        } else if (weatherId >= 701 && weatherId <= 761) {
            return R.drawable.ic_fog;
        } else if (weatherId == 761 || weatherId == 781) {
            return R.drawable.ic_storm;
        } else if (weatherId == 800) {
            return R.drawable.ic_clear;
        } else if (weatherId == 801) {
            return R.drawable.ic_light_clouds;
        } else if (weatherId >= 802 && weatherId <= 804) {
            return R.drawable.ic_cloudy;
        }
        return -1;
    }

    public static String getIconResourceStringForWeatherCondition(int weatherId) {
        // Based on weather code data found at:
        // http://bugs.openweathermap.org/projects/api/wiki/Weather_Condition_Codes
        if (weatherId >= 200 && weatherId <= 232) {
            return "art_storm";
        } else if (weatherId >= 300 && weatherId <= 321) {
            return "art_light_rain";
        } else if (weatherId >= 500 && weatherId <= 504) {
            return "art_rain";
        } else if (weatherId == 511) {
            return "art_snow";
        } else if (weatherId >= 520 && weatherId <= 531) {
            return "art_rain";
        } else if (weatherId >= 600 && weatherId <= 622) {
            return "art_snow";
        } else if (weatherId >= 701 && weatherId <= 761) {
            return "art_fog";
        } else if (weatherId == 761 || weatherId == 781) {
            return "art_storm";
        } else if (weatherId == 800) {
            return "art_clear";
        } else if (weatherId == 801) {
            return "art_light_clouds";
        } else if (weatherId >= 802 && weatherId <= 804) {
            return "art_clouds";
        }
        return "";
    }

    /**
     * Helper method to provide the art resource id according to the weather condition id returned
     * by the OpenWeatherMap call.
     * @param weatherId from OpenWeatherMap API response
     * @return resource id for the corresponding icon. -1 if no relation is found.
     */
    public static int getArtResourceForWeatherCondition(int weatherId) {
        // Based on weather code data found at:
        // http://bugs.openweathermap.org/projects/api/wiki/Weather_Condition_Codes
        if (weatherId >= 200 && weatherId <= 232) {
            return R.drawable.art_storm;
        } else if (weatherId >= 300 && weatherId <= 321) {
            return R.drawable.art_light_rain;
        } else if (weatherId >= 500 && weatherId <= 504) {
            return R.drawable.art_rain;
        } else if (weatherId == 511) {
            return R.drawable.art_snow;
        } else if (weatherId >= 520 && weatherId <= 531) {
            return R.drawable.art_rain;
        } else if (weatherId >= 600 && weatherId <= 622) {
            return R.drawable.art_snow;
        } else if (weatherId >= 701 && weatherId <= 761) {
            return R.drawable.art_fog;
        } else if (weatherId == 761 || weatherId == 781) {
            return R.drawable.art_storm;
        } else if (weatherId == 800) {
            return R.drawable.art_clear;
        } else if (weatherId == 801) {
            return R.drawable.art_light_clouds;
        } else if (weatherId >= 802 && weatherId <= 804) {
            return R.drawable.art_clouds;
        }
        return -1;
    }

    //Set notification preferences for Phone and Wear together
    private static NotificationCompat.Builder setNotificationSettings(Context context, SharedPreferences prefs, NotificationCompat.Builder mBuilder){
        String displayWearNotificationKey = context.getString(R.string.pref_enable_notifications_wear_key);
        String displayPhoneNotificationKey = context.getString(R.string.pref_enable_notifications_phone_key);
        String displayNotificationLight = context.getString(R.string.pref_enable_notifications_light_key);
        String displayNotificationSound = context.getString(R.string.pref_enable_notifications_sound_key);
        String displayNotificationVibrate = context.getString(R.string.pref_enable_notifications_vibrate_key);
        String displayPriority = "notification_priority";

        boolean wearNotifications = prefs.getBoolean(displayWearNotificationKey, Boolean.parseBoolean("true"));
        boolean notificationLight = prefs.getBoolean(displayNotificationLight, Boolean.parseBoolean("true"));
        boolean notificationSound = prefs.getBoolean(displayNotificationSound, Boolean.parseBoolean("true"));
        boolean notificationVibrate = prefs.getBoolean(displayNotificationVibrate, Boolean.parseBoolean("true"));
        int notification_priority = Integer.parseInt(prefs.getString("notification_priority", "0"));

        // Setting for wear notifications
        mBuilder.setLocalOnly(!wearNotifications);

        // Setting for notification Light
        if(notificationLight){
            mBuilder.setLights( -3355444, 100, 100);
        } else {
            mBuilder.setLights( -3355444, 0, 0);
        }

        // Setting for notification sound
        if(notificationSound) {
            // To get the default notification tone. This is used later to set the sound for notification
            Uri uriSound= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            mBuilder.setSound(uriSound);
        }

        // Setting for notification vibration
        if(notificationVibrate) {
            // Vibration pattern
            long[] vibrate = { 0, 100, 100, 100, 100, 100, 100, 100 };
            mBuilder.setVibrate(vibrate);
        }

        // Set the priority of the notification
        mBuilder.setPriority(notification_priority);

        return mBuilder;

    }

    //Set Notification preferences for Wear Only
    private static DataMap setNotificationSettingsForWearOnly(Context context, SharedPreferences prefs, DataMap dataMap){
        String displayNotificationVibrate = context.getString(R.string.pref_enable_notifications_vibrate_key);
        String displayPriority = "notification_priority";

        boolean notificationVibrate = prefs.getBoolean(displayNotificationVibrate, Boolean.parseBoolean("true"));
        int notification_priority = Integer.parseInt(prefs.getString("notification_priority", "0"));

        // Setting for notification vibration
        if(notificationVibrate) {
            // Vibration pattern
            long[] vibrate = { 0, 100, 100, 100, 100, 100, 100, 100 };
            dataMap.putLongArray("vibrate", vibrate);
        }

        // Set the priority of the notification
        dataMap.putInt("priority", notification_priority );

        return dataMap;
    }
    public static void notifyWeather(Context context) {

        //checking the last update and notify if it' the first of the day
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String displayNotificationsPhoneKey = context.getString(R.string.pref_enable_notifications_phone_key);
        boolean displayNotificationsPhone = prefs.getBoolean(displayNotificationsPhoneKey,
                Boolean.parseBoolean(context.getString(R.string.pref_enable_notifications_phone_default)));
        String displayNotificationsWearKey = context.getString(R.string.pref_enable_notifications_wear_key);
        boolean displayNotificationsWear = prefs.getBoolean(displayNotificationsWearKey,
                Boolean.parseBoolean(context.getString(R.string.pref_enable_notifications_wear_default)));

        String displayTimedNotificationsKey = context.getString(R.string.pref_notifications_time_preference_key);
        String time = prefs.getString(displayTimedNotificationsKey, context.getString
                (R.string.pref_notifications_time_preference_default));


        if ( displayNotificationsPhone || displayNotificationsWear ) {

            String lastNotificationKey = context.getString(R.string.pref_last_notification);
            long lastSync = prefs.getLong(lastNotificationKey, 0);

            // System.currentTimeMillis() - lastSync >= DAY_IN_MILLIS
            // passing true for testing
            if (true) {
                // Last sync was more than 1 day ago, let's send a notification with the weather.
                String locationQuery = Utility.getPreferredLocation(context);

                Uri weatherUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(locationQuery, System.currentTimeMillis());

                // we'll query our contentProvider, as always
                Cursor cursor = context.getContentResolver().query(weatherUri, NOTIFY_WEATHER_PROJECTION, null, null, null);

                if (cursor.moveToFirst()) {
                    int weatherId = cursor.getInt(INDEX_WEATHER_ID);
                    double high = cursor.getDouble(INDEX_MAX_TEMP);
                    double low = cursor.getDouble(INDEX_MIN_TEMP);
                    String desc = cursor.getString(INDEX_SHORT_DESC);

                    int iconId = Utility.getIconResourceForWeatherCondition(weatherId);
                    String iconName = Utility.getIconResourceStringForWeatherCondition(weatherId);
                    Resources resources = context.getResources();
                    Bitmap largeIcon = BitmapFactory.decodeResource(resources,
                            Utility.getArtResourceForWeatherCondition(weatherId));
                    String title = context.getString(R.string.app_name);

                    // Define the text of the forecast.
                    String contentText = String.format(context.getString(R.string.format_notification),
                            desc,
                            Utility.formatTemperature(context, high),
                            Utility.formatTemperature(context, low));

                    if (displayNotificationsPhone) {
                        // NotificationCompatBuilder is a very convenient way to build backward-compatible
                        // notifications.  Just throw in some data.
                        NotificationCompat.Builder mBuilder =
                                new NotificationCompat.Builder(context)
                                        .setColor(resources.getColor(R.color.sunshine_light_blue))
                                        .setSmallIcon(iconId)
                                        .setLargeIcon(largeIcon)
                                        .setContentTitle(title)
                                        .setContentText(contentText);

                        // Setting all the notification preferences
                        mBuilder = setNotificationSettings(context, prefs, mBuilder);

                        // Make something interesting happen when the user clicks on the notification.
                        // In this case, opening the app is sufficient.
                        Intent resultIntent = new Intent(context, MainActivity.class);

                        // The stack builder object will contain an artificial back stack for the
                        // started Activity.
                        // This ensures that navigating backward from the Activity leads out of
                        // your application to the Home screen.
                        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                        stackBuilder.addNextIntent(resultIntent);
                        PendingIntent resultPendingIntent =
                                stackBuilder.getPendingIntent(
                                        0,
                                        PendingIntent.FLAG_UPDATE_CURRENT
                                );
                        mBuilder.setContentIntent(resultPendingIntent);

                        NotificationManager mNotificationManager =
                                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                        // WEATHER_NOTIFICATION_ID allows you to update the notification later on.
                        mNotificationManager.notify(WEATHER_NOTIFICATION_ID, mBuilder.build());
                    }

                    if (displayNotificationsWear && !displayNotificationsPhone) {
                        DataMap dataMap = new DataMap();
                        dataMap.putInt("color", resources.getColor(R.color.sunshine_light_blue));
                        dataMap.putString("title", title);
                        dataMap.putString("content", contentText);
                        dataMap.putString("back", "270");
                        dataMap.putString("icon", iconName);

                        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), iconId);
                        Asset smallIconAsset = createAssetFromBitmap(bitmap);
                        dataMap.putAsset("smallIcon", smallIconAsset);
                        Asset largeIconAsset = createAssetFromBitmap(largeIcon);
                        dataMap.putAsset("largeIcon", largeIconAsset);
                        dataMap = setNotificationSettingsForWearOnly(context, prefs, dataMap);
                        WearableCommunication.increaseCounter(dataMap);
                    }
                    //refreshing last sync
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putLong(lastNotificationKey, System.currentTimeMillis());
                    editor.commit();

                }
                cursor.close();
            }
        }
    }

    private static Asset createAssetFromBitmap(Bitmap bitmap) {
        final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteStream);
        return Asset.createFromBytes(byteStream.toByteArray());
    }
}