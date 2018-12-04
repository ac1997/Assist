package com.caltruism.assist.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.firestore.DocumentSnapshot;

import java.util.HashMap;

public class SharedPreferencesHelper {
    public static void setPreferences(Context context, DocumentSnapshot ds) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.USER_DATA_SHARED_PREFERENCE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        Object memberFirstNameObject = ds.get("firstName");
        Object memberLastNameObject = ds.get("lastName");
        Object memberPhoneNumber = ds.get("phoneNumber");
        Object memberPictureURLObject = ds.get("pictureUrl");
        Object memberRatingsObject = ds.get("ratings");
        Object memberTypeObject = ds.get("memberType");

        if (memberFirstNameObject != null && !sharedPreferences.contains("firstName"))
            editor.putString("firstName", memberFirstNameObject.toString());

        if (memberLastNameObject != null && !sharedPreferences.contains("lastName"))
            editor.putString("lastName", memberLastNameObject.toString());

        if (memberFirstNameObject != null && memberLastNameObject != null && !sharedPreferences.contains("name"))
            editor.putString("name", String.format("%s %s", memberFirstNameObject.toString(), memberLastNameObject.toString()));

        if (memberPhoneNumber != null && !sharedPreferences.contains("phoneNumber"))
            editor.putString("phoneNumber", memberPhoneNumber.toString());

        if (memberPictureURLObject != null && !sharedPreferences.contains("pictureUrl"))
            editor.putString("pictureUrl", memberPictureURLObject.toString());

        if (memberRatingsObject != null && !sharedPreferences.contains("ratings"))
            editor.putFloat("ratings", Float.parseFloat(memberRatingsObject.toString()));

        if (memberTypeObject != null && !sharedPreferences.contains("memberType"))
            editor.putString("memberType", memberTypeObject.toString());

        editor.apply();
    }

    public static void setPreferences(Context context, HashMap<String, Object> userData) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.USER_DATA_SHARED_PREFERENCE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        Object memberFirstNameObject = userData.get("firstName");
        Object memberLastNameObject = userData.get("lastName");
        Object memberPhoneNumber = userData.get("phoneNumber");
        Object memberPictureURLObject = userData.get("pictureUrl");
        Object memberRatingsObject = userData.get("ratings");
        Object memberTypeObject = userData.get("memberType");

        if (memberFirstNameObject != null && !sharedPreferences.contains("firstName"))
            editor.putString("firstName", memberFirstNameObject.toString());

        if (memberLastNameObject != null && !sharedPreferences.contains("lastName"))
            editor.putString("lastName", memberLastNameObject.toString());

        if (memberFirstNameObject != null && memberLastNameObject != null && !sharedPreferences.contains("name"))
            editor.putString("name", String.format("%s %s", memberFirstNameObject.toString(), memberLastNameObject.toString()));

        if (memberPhoneNumber != null && !sharedPreferences.contains("phoneNumber"))
            editor.putString("phoneNumber", memberPhoneNumber.toString());

        if (memberPictureURLObject != null && !sharedPreferences.contains("pictureUrl"))
            editor.putString("pictureUrl", memberPictureURLObject.toString());

        if (memberRatingsObject != null && !sharedPreferences.contains("ratings"))
            editor.putFloat("ratings", Float.parseFloat(memberRatingsObject.toString()));

        if (memberTypeObject != null && !sharedPreferences.contains("memberType"))
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
