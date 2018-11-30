package com.caltruism.assist.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;

import com.caltruism.assist.R;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

public class BitMapDescriptorFromVector {
    public static BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    public static BitmapDescriptor regularMarker(Context context) {
        return bitmapDescriptorFromVector(context, R.drawable.ic_marker_regular);
    }

    public static BitmapDescriptor groceryMarker(Context context) {
        return bitmapDescriptorFromVector(context, R.drawable.ic_marker_grocery);
    }

    public static BitmapDescriptor laundryMarker(Context context) {
        return bitmapDescriptorFromVector(context, R.drawable.ic_marker_laundry);
    }

    public static BitmapDescriptor walkingMarker(Context context) {
        return bitmapDescriptorFromVector(context, R.drawable.ic_marker_walking);
    }

    public static BitmapDescriptor otherMarker(Context context) {
        return bitmapDescriptorFromVector(context, R.drawable.ic_marker_other);
    }

    public static BitmapDescriptor otherUserCap(Context context) {
        return bitmapDescriptorFromVector(context, R.drawable.ic_cap_other_user);
    }

    public static BitmapDescriptor requestTypeMarker(Context context, int requestType) {
        switch (requestType) {
            case Constants.GROCERY_TYPE:
                return groceryMarker(context);
            case Constants.LAUNDRY_TYPE:
                return laundryMarker(context);
            case Constants.WALKING_TYPE:
                return walkingMarker(context);
            case Constants.OTHER_TYPE:
                return otherMarker(context);
            default:
                return regularMarker(context);
        }
    }
}
