package com.caltruism.assist.data;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.text.format.DateUtils;
import android.util.Log;

import com.caltruism.assist.util.BitMapDescriptorFromVector;
import com.caltruism.assist.util.Constants;
import com.caltruism.assist.util.CustomDateTimeUtil;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;

public class AssistRequest implements Comparable<AssistRequest>, Parcelable {

    private static String TAG = "AssistRequest";

    private String id;
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
    private User postedBy;
    private ArrayList<User> acceptedBy;

    private String dateTimeString;
    private String dateString;
    private String timeString;
    private String durationString;

    private float distance;
    private Marker marker;

    protected AssistRequest(Parcel in) {
        id = in.readString();
        status = in.readInt();
        type = in.readInt();
        title = in.readString();
        description = in.readString();
        locationName = in.readString();
        locationAddress = in.readString();
        locationLatLng = in.readParcelable(LatLng.class.getClassLoader());
        isNow = in.readByte() != 0;
        dateTime = in.readLong();
        startTime = in.readInt();
        endTime = in.readInt();
        duration = in.readInt();
        postedBy = in.readParcelable(User.class.getClassLoader());
        acceptedBy = in.createTypedArrayList(User.CREATOR);
        dateTimeString = in.readString();
        dateString = in.readString();
        timeString = in.readString();
        durationString = in.readString();
        distance = in.readFloat();
    }

    public static final Creator<AssistRequest> CREATOR = new Creator<AssistRequest>() {
        @Override
        public AssistRequest createFromParcel(Parcel in) {
            return new AssistRequest(in);
        }

        @Override
        public AssistRequest[] newArray(int size) {
            return new AssistRequest[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeInt(status);
        dest.writeInt(type);
        dest.writeString(title);
        dest.writeString(description);
        dest.writeString(locationName);
        dest.writeString(locationAddress);
        dest.writeParcelable(locationLatLng, flags);
        dest.writeByte((byte) (isNow ? 1 : 0));
        dest.writeLong(dateTime);
        dest.writeInt(startTime);
        dest.writeInt(endTime);
        dest.writeInt(duration);
        dest.writeParcelable(postedBy, flags);
        dest.writeTypedList(acceptedBy);
        dest.writeString(dateTimeString);
        dest.writeString(dateString);
        dest.writeString(timeString);
        dest.writeString(durationString);
        dest.writeFloat(distance);
    }

    public AssistRequest(String id) {
        this.id = id;
    }

    public AssistRequest(DocumentSnapshot ds) {
        if (ds.exists())
            setDataFromDocumentSnapshot(ds);
        else
            Log.e(TAG, "AssistRequest error: DocumentSnapshot does not exist");
    }

    public AssistRequest(DocumentSnapshot ds, Location location) {
        if (ds.exists())
            setDataFromDocumentSnapshot(ds);
        else
            Log.e(TAG, "AssistRequest error: DocumentSnapshot does not exist");

        if (location != null)
            setDistance(location);
    }

    public void modifiedData(DocumentSnapshot ds) {
        boolean updateMarker = false;

        // TODO EDIT SNIPPET
        if (!this.title.equals(ds.getString("title")))
            updateMarker = true;

        setDataFromDocumentSnapshot(ds);

        if (marker != null && updateMarker) {
            marker.setTitle(this.title);
            marker.setSnippet(CustomDateTimeUtil.getDateWithTime(this.dateTime));
        }
    }

    private void setDataFromDocumentSnapshot(DocumentSnapshot ds) {
        this.id = ds.getId();
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
        ArrayList<HashMap<String, Object>> acceptedBy = (ArrayList<HashMap<String, Object>>) ds.get("acceptedBy");

        this.locationName = (String) location.get("name");
        this.locationAddress = (String) location.get("address");
        GeoPoint gp = (GeoPoint) location.get("latLng");
        this.locationLatLng = new LatLng(gp.getLatitude(), gp.getLongitude());

        this.postedBy = new User((String) postedBy.get("uid"), (String) postedBy.get("name"));

        this.acceptedBy = new ArrayList<>();
        if (acceptedBy != null && acceptedBy.size() > 0) {
            for (HashMap<String, Object> map : acceptedBy) {
                this.acceptedBy.add(new User((String) map.get("uid"), (String) map.get("name")));
            }
        }

        this.dateTimeString = CustomDateTimeUtil.getDateWithTime(this.dateTime);
        this.dateString = CustomDateTimeUtil.getDate(this.dateTime);
        this.timeString = CustomDateTimeUtil.getTime(this.dateTime);
        this.durationString = CustomDateTimeUtil.getFormattedDuration(this.duration);
    }

    private void setDistance(Location location) {
        Location requestLocation = new Location(LocationManager.GPS_PROVIDER);
        requestLocation.setLatitude(this.locationLatLng.latitude);
        requestLocation.setLongitude(this.locationLatLng.longitude);
        distance = (float) (requestLocation.distanceTo(location) * Constants.METER_TO_MILE);
    }

    public void setNewMarker(Context context, GoogleMap map) {
        this.marker = map.addMarker(new MarkerOptions().position(this.locationLatLng)
                .title(this.title).snippet(this.dateTimeString)
                .icon(BitMapDescriptorFromVector.requestTypeMarker(context, this.type)));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof String) {
            Log.e(TAG, "equals called with obj id " + obj);
            return this.id.equals(obj);}
        else
            return this == obj || obj instanceof AssistRequest && this.id.equals(((AssistRequest) obj).getId());
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }

    @Override
    public int compareTo(@NonNull AssistRequest o) {
        return Long.compare(this.dateTime, o.dateTime);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
        return this.postedBy.name;
    }

    public void setPostedByName(String postedByName) {
        this.postedBy.name = postedByName;
    }

    public String getPostedByUid() {
        return this.postedBy.uid;
    }

    public void setPostedByUid(String postedByUid) {
        this.postedBy.uid = postedByUid;
    }

    public long getDateTime() {
        return dateTime;
    }

    public void setDateTime(long dateTime) {
        this.dateTime = dateTime;
    }

    public String getDateTimeString() {
        return dateTimeString;
    }

    public String getDateString() {
        return dateString;
    }

    public String getTimeString() {
        return timeString;
    }

    public String getDurationString() {
        return durationString;
    }

    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public String getAcceptedByMainName() {
        if (this.acceptedBy.size() > 0)
            return this.acceptedBy.get(0).name;
        else
            return null;
    }

    public String getAcceptedByMainUid() {
        if (this.acceptedBy.size() > 0)
            return this.acceptedBy.get(0).uid;
        else
            return null;
    }

    public boolean isAccepted() {
        return this.acceptedBy.size() > 0;
    }

    public String getDistanceString() {
        return String.format(Locale.getDefault(), "%.1f mi", this.distance);
    }

    public static int insertInOrder(ArrayList<AssistRequest> arrayList, AssistRequest assistRequest) {
        int pos = Collections.binarySearch(arrayList, assistRequest);

        if (pos < 0) {
            pos = -pos - 1;
            arrayList.add(pos, assistRequest);
        } else {
            arrayList.add(pos, assistRequest);
        }

        return pos;
    }
}
