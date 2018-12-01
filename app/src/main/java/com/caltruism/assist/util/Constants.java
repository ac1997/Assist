package com.caltruism.assist.util;

public final class Constants {
    public static final String USER_DATA_SHARED_PREFERENCE = "userData";

    public static final double MILES_TO_KM = 1.609344;
    public static final double METER_TO_MILE = 0.000621371;

    public static final int OTHER_TYPE = 0;
    public static final int GROCERY_TYPE = 1;
    public static final int LAUNDRY_TYPE = 2;
    public static final int WALKING_TYPE = 3;

    public static final int REQUEST_STATUS_WAITING = 0;
    public static final int REQUEST_STATUS_ACCEPTED = 1;
    public static final int REQUEST_STATUS_COMPLETED = 2;
    public static final int REQUEST_STATUS_NO_SHOW = 3;
    public static final int REQUEST_STATUS_CANCELLED = 4;
    public static final int REQUEST_STATUS_EXPIRED = 5;

    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 1000;
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    public static final float DEFAULT_ZOOM = 15;
    public static final float MIN_ZOOM = 10;
    public static final float MAX_ZOOM = 20;

}
