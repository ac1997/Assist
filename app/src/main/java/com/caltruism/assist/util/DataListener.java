package com.caltruism.assist.util;

import android.location.Location;

import com.caltruism.assist.data.AssistRequest;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.HashMap;

public class DataListener {

    public interface VolunteerRequestListViewDataListener {
        void onDataChange(HashMap<String, AssistRequest> data);
    }

    public interface VolunteerRequestMapViewDataListener {
        void onNewLocations(Location newCurrentLocation, LatLng newCameraLocation);
        void onNewDataSet(HashMap<String, AssistRequest> data);
        void onDataAdded(DocumentSnapshot documentSnapshot);
        void onDataRemoved(DocumentSnapshot documentSnapshot);
        void onDataModified(DocumentSnapshot documentSnapshot);
    }

    public interface AddRequestFragmentDataListener {
        void onDataChange(Object... data);
    }

    public interface AddRequestActivityDataListener {
        void onDataChange(int step, Object data);
    }
}
