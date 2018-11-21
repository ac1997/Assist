package com.caltruism.assist.data;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.format.DateUtils;

import com.caltruism.assist.util.BitMapDescriptorFromVector;
import com.caltruism.assist.util.CustomDateTimeUtil;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.GeoPoint;

import java.io.Serializable;
import java.util.HashMap;

public class AssistRequest implements Serializable {
    private int status;
    private int type;
    private String title;
    private String description;
    private String locationName;
    private String locationAddress;
    private LatLng locationLatLng;
    private boolean isNow;
    private long dateTime;
    private int startTime;
    private int endTime;
    private int duration;
    private String postedByName;
    private String postedByUID;

    private String dateTimeString;
    private Marker marker;

    public AssistRequest() {
    }

    public AssistRequest(DocumentSnapshot ds) {
        setDataFromDocumentSnapshot(ds);
    }

    public void modifiedData(DocumentSnapshot ds) {
        boolean updateMarker = false;

        // TODO EDIT SNIPPER
        if (!this.title.equals(ds.getString("title")))
            updateMarker = true;

        setDataFromDocumentSnapshot(ds);

        if (marker != null && updateMarker) {
            marker.setTitle(this.title);
            marker.setSnippet(CustomDateTimeUtil.getDateWithTime(this.dateTime));
        }
    }

    public void setDataFromDocumentSnapshot(DocumentSnapshot ds) {
        this.status = ds.getLong("status").intValue();
        this.type = ds.getLong("type").intValue();
        this.title = ds.getString("title");
        this.description = ds.getString("description");
        this.isNow = ds.getBoolean("isNow");
        this.dateTime = ds.getLong("date") * DateUtils.MINUTE_IN_MILLIS;
        this.startTime = ds.getLong("startTime").intValue();
        this.endTime = ds.getLong("endTime").intValue();
        this.duration = ds.getLong("duration").intValue();


        HashMap<String, Object> location = (HashMap<String, Object>) ds.get("location");
        HashMap<String, Object> postedBy = (HashMap<String, Object>) ds.get("postedBy");

        this.locationName = (String) location.get("name");
        this.locationAddress = (String) location.get("address");
        GeoPoint gp = (GeoPoint) location.get("latLng");
        this.locationLatLng = new LatLng(gp.getLatitude(), gp.getLongitude());

        this.postedByName = (String) postedBy.get("name");
        this.postedByUID = (String) postedBy.get("uid");

        this.dateTimeString = CustomDateTimeUtil.getDateWithTime(this.dateTime);
    }

    public void setNewMarker(Context context, GoogleMap map) {
        this.marker = map.addMarker(new MarkerOptions().position(this.locationLatLng)
                .title(this.title).snippet(this.dateTimeString)
                .icon(BitMapDescriptorFromVector.requestTypeMarker(context, this.type)));
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public String getLocationAddress() {
        return locationAddress;
    }

    public void setLocationAddress(String locationAddress) {
        this.locationAddress = locationAddress;
    }

    public LatLng getLocationLatLng() {
        return locationLatLng;
    }

    public void setLocationLatLng(LatLng locationLatLng) {
        this.locationLatLng = locationLatLng;
    }

    public boolean isNow() {
        return isNow;
    }

    public void setNow(boolean now) {
        isNow = now;
    }

    public int getStartTime() {
        return startTime;
    }

    public void setStartTime(int startTime) {
        this.startTime = startTime;
    }

    public int getEndTime() {
        return endTime;
    }

    public void setEndTime(int endTime) {
        this.endTime = endTime;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getPostedByName() {
        return postedByName;
    }

    public void setPostedByName(String postedByName) {
        this.postedByName = postedByName;
    }

    public String getPostedByUID() {
        return postedByUID;
    }

    public void setPostedByUID(String postedByUID) {
        this.postedByUID = postedByUID;
    }

    public long getDateTime() {
        return dateTime;
    }

    public void setDateTime(long dateTime) {
        this.dateTime = dateTime;
    }

    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }
}
