package com.caltruism.assist.util;

import android.icu.util.Calendar;
import android.text.format.DateUtils;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class CustomDateTimeUtil {
    private static final String TAG = "CustomDateTimeUtil";

    public static String getDate(long dateTimeMili) {
        if (DateUtils.isToday(dateTimeMili)) {
            return "Today";
        } else if (DateUtils.isToday(dateTimeMili - DateUtils.DAY_IN_MILLIS)) {
            return "Tomorrow";
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy", Locale.US);
            return sdf.format(dateTimeMili);
        }
    }

    public static String getDateWithTime (long dateTimeMili) {
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.US);

        if (DateUtils.isToday(dateTimeMili)) {
            return "Today, " + sdf.format(dateTimeMili);
        } else if (DateUtils.isToday(dateTimeMili - DateUtils.DAY_IN_MILLIS)) {
            return "Tomorrow, " + sdf.format(dateTimeMili);
        } else {
            sdf = new SimpleDateFormat("MM/dd/yy, h:mm a", Locale.US);
            return sdf.format(dateTimeMili);
        }
    }

    public static String getDateWithDay(long timeStamp) {
        if (DateUtils.isToday(timeStamp)) {
            return "Today";
        } else if (DateUtils.isToday(timeStamp - DateUtils.DAY_IN_MILLIS)) {
            return "Tomorrow";
        } else {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(timeStamp);
            SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMMM d", Locale.US);
            return sdf.format(calendar.getTime());
        }
    }

    public static String getTime(long timeStamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeStamp);
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.US);
        return sdf.format(calendar.getTime());
    }

    public static String[] getDateWithDayAndTime(long timeStamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeStamp);

        String date;
        if (DateUtils.isToday(timeStamp)) {
            date = "Today";
        } else if (DateUtils.isToday(timeStamp - DateUtils.DAY_IN_MILLIS)) {
            date = "Tomorrow";
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMMM dd", Locale.US);
            date = sdf.format(calendar.getTime());
        }

        SimpleDateFormat sdf1 = new SimpleDateFormat("h:mm a", Locale.US);

        return new String[]{date, sdf1.format(calendar.getTime())};
    }

    public static String getFormattedDuration(int totalMinutes) {
        int hour = totalMinutes / 60;
        int minute = totalMinutes % 60;

        if (hour > 0 && minute > 0)
            return String.format(Locale.getDefault(), "%d hr %d mins", hour, minute);
        else if (hour == 1)
            return String.format(Locale.getDefault(), "%d hour", hour);
        else if (hour > 1)
            return String.format(Locale.getDefault(), "%d hours", hour);
        else
            return String.format(Locale.getDefault(), "%d minutes", minute);
    }

    public static int getStartTime(long timeStamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeStamp);

        return calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE);
    }

    public static boolean isNow(long dateTimeMili) {
        dateTimeMili += 3 * DateUtils.MINUTE_IN_MILLIS;
        return dateTimeMili - 10 * DateUtils.MINUTE_IN_MILLIS <= (System.currentTimeMillis() - 1000);
    }

    public static boolean isExpired(long dateTimeMili, int duration) {
        return (System.currentTimeMillis() - 1000) > (dateTimeMili + (duration + 5) * DateUtils.MINUTE_IN_MILLIS);
    }

    public static boolean isCurrent(long dateTimeMili, int rangeInMin) {
        return (System.currentTimeMillis() - 1000) >= (dateTimeMili - rangeInMin * DateUtils.MINUTE_IN_MILLIS);
    }
}
