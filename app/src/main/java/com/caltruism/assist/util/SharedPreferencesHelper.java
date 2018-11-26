package com.caltruism.assist.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.firestore.DocumentSnapshot;

public class SharedPreferencesHelper {
    public static void setPreferences(Context context, DocumentSnapshot ds) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.USER_DATA_SHARED_PREFERENCE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        Object memberFirstNameObject = ds.get("firstName");
        Object memberLastNameObject = ds.get("lastName");
        Object memberPictureURLObject = ds.get("pictureURL");
        Object memberRatingsObject = ds.get("ratings");
        Object memberTypeObject = ds.get("memberType");

        if (memberFirstNameObject != null)
            editor.putString("firstName", memberFirstNameObject.toString());

        if (memberLastNameObject != null)
            editor.putString("lastName", memberLastNameObject.toString());

        if (memberFirstNameObject != null && memberLastNameObject != null)
            editor.putString("name", String.format("%s %s", memberFirstNameObject.toString(), memberLastNameObject.toString()));

        if (memberPictureURLObject != null)
            editor.putString("pictureURL", memberPictureURLObject.toString());

        if (memberRatingsObject != null)
            editor.putFloat("ratings", Float.parseFloat(memberRatingsObject.toString()));

        if (memberTypeObject != null)
            editor.putString("memberType", memberTypeObject.toString());

        editor.apply();
    }

    public static void setPreferencesMemberType(Context context, String memberType) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.USER_DATA_SHARED_PREFERENCE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("memberType", memberType);
        editor.apply();
    }

    public static void clearPreferences(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.USER_DATA_SHARED_PREFERENCE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }
}
