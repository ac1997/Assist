package com.caltruism.assist.utils;

import android.icu.util.Calendar;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class CustomDateTimeUtil {
    private static final String TAG = "CustomDateTimeUtil";

    public static String getDateWithDay(String dateTimeMili) {
        long timeStamp;

        try {
            timeStamp = Long.parseLong(dateTimeMili);
        } catch (Exception e) {
            Log.e(TAG, "Failed to parse date time: getDateWithDay", e);
            return "";
        }

        Calendar requestDateAndTime = Calendar.getInstance();
        requestDateAndTime.setTimeInMillis(timeStamp);
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMMMM dd", Locale.US);
        return sdf.format(requestDateAndTime.getTime());
    }

    public static String getTime(String dateTimeMili) {
        long timeStamp;

        try {
            timeStamp = Long.parseLong(dateTimeMili);
        } catch (Exception e) {
            Log.e(TAG, "Failed to parse date time: getTime", e);
            return "";
        }

        Calendar requestDateAndTime = Calendar.getInstance();
        requestDateAndTime.setTimeInMillis(timeStamp);
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.US);
        return sdf.format(requestDateAndTime.getTime());
    }

    public static List<String> getDateWithDayandTime(String dateTimeMili) {
        long timeStamp;

        try {
            timeStamp = Long.parseLong(dateTimeMili);
        } catch (Exception e) {
            Log.e(TAG, "Failed to parse date time: getDateWithDay", e);
            return null;
        }

        Calendar requestDateAndTime = Calendar.getInstance();
        requestDateAndTime.setTimeInMillis(timeStamp);
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMMM dd", Locale.US);
        SimpleDateFormat sdf1 = new SimpleDateFormat("h:mm a", Locale.US);

        return Arrays.asList(sdf.format(requestDateAndTime.getTime()), sdf1.format(requestDateAndTime.getTime()));
    }

    public static String getFormattedDuration(String durationInMinutes) {
        int totalMinutes;

        try {
            totalMinutes = Integer.parseInt(durationInMinutes);
        } catch (Exception e) {
            Log.e(TAG, "Failed to parse duration: getFormattedDuration", e);
            return "";
        }

        int hour = totalMinutes / 60;
        int minute = totalMinutes % 60;

        if (hour > 0)
            return String.format(Locale.getDefault(), "%d hr %d mins", hour, minute);
        else
            return String.format(Locale.getDefault(), "%d minutes", minute);
    }
}
