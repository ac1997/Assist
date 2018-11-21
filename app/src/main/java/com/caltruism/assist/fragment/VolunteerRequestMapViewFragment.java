package com.caltruism.assist.fragment;

import android.annotation.SuppressLint;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.caltruism.assist.R;
import com.caltruism.assist.data.AssistRequest;
import com.caltruism.assist.util.BitMapDescriptorFromVector;
import com.caltruism.assist.util.CustomDateTimeUtil;
import com.caltruism.assist.util.DataListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class VolunteerRequestMapViewFragment extends Fragment implements DataListener.VolunteerRequestMapViewDataListener,
        OnMapReadyCallback, ActivityCompat.OnRequestPermissionsResultCallback {

    private static final String TAG = "VolunteerRequestMapViewFragment";
    private static final float DEFAULT_ZOOM = 14;

    private MapView mapView;

    private GoogleMap map;
    private boolean isMapReady = false;

    private Location currentLocation;
    private LatLng cameraLocation;
    private HashMap<String, AssistRequest> assistRequests = new HashMap<>();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        cameraLocation = arguments.getParcelable("location");
        assistRequests = (HashMap<String, AssistRequest>) ((HashMap<String, AssistRequest>) arguments.getSerializable("requests")).clone();

        for (String key : assistRequests.keySet()) {

            Log.e(TAG, "Initial data: " + key);
        }

        return inflater.inflate(R.layout.fragment_volunteer_request_map_view, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mapView = view.findViewById(R.id.mapViewVolunteerRequestList);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setMapStyle(MapStyleOptions.loadRawResourceStyle(getActivity(), R.raw.map_style_json));
        map.getUiSettings().setMyLocationButtonEnabled(false);
        map.getUiSettings().setMapToolbarEnabled(false);
        map.setMyLocationEnabled(true);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(cameraLocation, DEFAULT_ZOOM));
        isMapReady = true;

        FloatingActionButton fab = getView().findViewById(R.id.fabVolunteerMapViewCurrentLocation);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currentLocation != null) {
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(
                            new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()),
                            map.getCameraPosition().zoom));
                }
            }
        });

        for (String key : assistRequests.keySet()) {
            // TODO: Set onclick listener
            assistRequests.get(key).setNewMarker(getActivity(), map);
        }
    }

    @Override
    public void onResume() {
        mapView.onResume();
        super.onResume();
    }


    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onNewLocations(Location newCurrentLocation, LatLng newCameraLocation) {
        if (newCurrentLocation != null)
            currentLocation = newCurrentLocation;

        if (newCameraLocation != null) {
            cameraLocation = newCameraLocation;
            if (isMapReady)
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(cameraLocation, DEFAULT_ZOOM));
        }
    }

    @Override
    public void onNewDataSet(HashMap<String, AssistRequest> data) {
        if (isMapReady && data != null) {
            Iterator<Map.Entry<String, AssistRequest>> iter = assistRequests.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<String, AssistRequest> entry = iter.next();

                if (!data.containsKey(entry.getKey())) {
                    entry.getValue().getMarker().remove();
                    iter.remove();
                }
            }

            HashMap<String, AssistRequest> toBeAdded = new HashMap<>();

            for (String key : data.keySet()) {
                if (!assistRequests.containsKey(key))
                    toBeAdded.put(key, data.get(key));
            }
            assistRequests.putAll(toBeAdded);

            for (String key : toBeAdded.keySet()) {
                // TODO: Set onclick listener
                assistRequests.get(key).setNewMarker(getActivity(), map);
            }
        }
    }

    @Override
    public void onDataAdded(DocumentSnapshot documentSnapshot) {
        AssistRequest assistRequest = new AssistRequest(documentSnapshot);
        assistRequest.setMarker(map.addMarker(new MarkerOptions().position(assistRequest.getLocationLatLng())
                .title(assistRequest.getTitle()).snippet(CustomDateTimeUtil.getDateWithTime(assistRequest.getDateTime()))
                .icon(BitMapDescriptorFromVector.requestTypeMarker(getActivity(), assistRequest.getType()))));

        assistRequests.put(documentSnapshot.getId(), assistRequest);
    }

    @Override
    public void onDataRemoved(DocumentSnapshot documentSnapshot) {
        assistRequests.get(documentSnapshot.getId()).getMarker().remove();
        assistRequests.remove(documentSnapshot.getId());
    }

    @Override
    public void onDataModified(DocumentSnapshot documentSnapshot) {
        assistRequests.get(documentSnapshot.getId()).modifiedData(documentSnapshot);
    }
}
