package com.caltruism.assist.data;

import android.os.Parcel;
import android.os.Parcelable;

public class User implements Parcelable {
    public String uid;
    public String name;

    public User(String uid, String name) {
        this.uid = uid;
        this.name = name;
    }

    protected User(Parcel in) {
        uid = in.readString();
        name = in.readString();
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(uid);
        dest.writeString(name);
    }
}