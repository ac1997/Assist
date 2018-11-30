package com.caltruism.assist.util;

import android.location.Location;

import com.arlib.floatingsearchview.FloatingSearchView;
import com.caltruism.assist.data.AssistRequest;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class CustomCallbackListener {
    public interface DisabledRequestListChildFragmentCallbackListener {
        void onNewDataSet(ArrayList<AssistRequest> dataSet);
        void onDataAdded(ArrayList<AssistRequest> dataSet);
        void onDataRemoved(HashSet<String> removedId);
        void onDataModified(ArrayList<DocumentSnapshot> dataSet);
    }

    public interface VolunteerRequestListFragmentCallbackListener {
        void onAttachSearchViewToDrawer(FloatingSearchView searchView);
    }

    public interface VolunteerRequestListChildFragmentCallbackListener {
        void onNewLocations(Location newCurrentLocation, LatLng newCameraLocation, boolean isUsingCurrentLocation,  boolean isNewSearch);
        void onNewDataSet(HashMap<String, AssistRequest> data);
        void onDataAdded(DocumentSnapshot documentSnapshot);
        void onDataRemoved(String documentId);
        void onDataModified(DocumentSnapshot documentSnapshot);
    }

    public interface AddRequestFragmentCallbackListener {
        void onDataChange(Object... data);
    }

    public interface AddRequestActivityCallbackListener {
        void onDataChange(int step, Object data);
    }

    public interface CurrentRequestActivityCallbackListener {
        void onSharingLocationPermissionChange(boolean isSharing);
        void onNewDurationData(int durationInMins);
    }

    public interface CurrentRequestMapViewFragmentCallbackListener {
        void onNewCurrentLocation(Location newCurrentLocation);
        void onNewOriginLocation(Location newOriginLocation);
    }

    public interface CurrentRequestWaitingViewFragmentCallbackListener {
        void onNewDuration(String durationInMins);
        void onVolunteerOffline();
    }

    public interface RouteParserCallbackListener {
        void onLatLngAndDurationCompleted(ArrayList<Object> data);
        void onTaskCompleted(PolylineOptions polylineOptions);
    }
}
