package com.caltruism.assist.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.caltruism.assist.R;
import com.caltruism.assist.activity.RequestDetailsActivity;
import com.caltruism.assist.data.AssistRequest;
import com.caltruism.assist.util.BitMapDescriptorFromVector;
import com.caltruism.assist.util.Constants;
import com.caltruism.assist.util.CustomDateTimeUtil;
import com.caltruism.assist.util.CustomCallbackListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.HashMap;

public class VolunteerRequestMapViewFragment extends Fragment implements CustomCallbackListener.VolunteerRequestListChildFragmentCallbackListener,
        OnMapReadyCallback, ActivityCompat.OnRequestPermissionsResultCallback {

    private static final String TAG = "VolunteerRequestMapViewFragment";

    private MapView mapView;
    private Snackbar snackbar;

    private GoogleMap map;
    private boolean isFirstUpdate = true;

    private Location currentLocation;
    private HashMap<String, AssistRequest> assistRequests = new HashMap<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
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

        map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                Intent intent = new Intent(getActivity(), RequestDetailsActivity.class);
                intent.putExtra("isVolunteerView", true);
                intent.putExtra("requestData", assistRequests.get(marker.getTag()));
                startActivity(intent);
            }
        });

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
    }

    @Override
    public void onResume() {
        mapView.onResume();
        super.onResume();
        if (assistRequests.size() == 0)
            onEmptyDataSet();
    }


    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
        if (snackbar != null && snackbar.isShown())
            snackbar.dismiss();
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
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);

        if (!hidden && assistRequests.size() == 0)
            onEmptyDataSet();
        else if (hidden && snackbar != null && snackbar.isShown())
            snackbar.dismiss();
    }

    @Override
    public void onNewLocations(Location newCurrentLocation, LatLng newCameraLocation, boolean isUsingCurrentLocation, boolean isNewSearch) {
        if (newCurrentLocation != null)
            currentLocation = newCurrentLocation;

        if (isUsingCurrentLocation || isNewSearch) {
            if (map != null && (isFirstUpdate || isNewSearch)) {
                isFirstUpdate = false;
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(newCameraLocation, Constants.DEFAULT_ZOOM));
            }
        }
    }

    @Override
    public void onNewDataSet(HashMap<String, AssistRequest> data) {
        if (map != null && data != null) {
            for (AssistRequest request : assistRequests.values())
                request.getMarker().remove();

            assistRequests.clear();

            if (data.size() == 0) {
                onEmptyDataSet();
            } else {
                assistRequests.putAll(data);
                for (AssistRequest assistRequest : assistRequests.values()) {
                    Marker marker = map.addMarker(new MarkerOptions().position(assistRequest.getLocationLatLng())
                            .title(assistRequest.getTitle()).snippet(CustomDateTimeUtil.getDateWithTime(assistRequest.getDateTime()))
                            .icon(BitMapDescriptorFromVector.requestTypeMarker(getActivity(), assistRequest.getType())));

                    marker.setTag(assistRequest.getId());
                    assistRequest.setMarker(marker);
                }
                dismissSnackbar();
            }
        }
    }

    @Override
    public void onDataAdded(DocumentSnapshot documentSnapshot) {
        AssistRequest assistRequest = new AssistRequest(documentSnapshot, currentLocation);
        Marker marker = map.addMarker(new MarkerOptions().position(assistRequest.getLocationLatLng())
                .title(assistRequest.getTitle()).snippet(CustomDateTimeUtil.getDateWithTime(assistRequest.getDateTime()))
                .icon(BitMapDescriptorFromVector.requestTypeMarker(getActivity(), assistRequest.getType())));

        marker.setTag(documentSnapshot.getId());
        assistRequest.setMarker(marker);

        assistRequests.put(documentSnapshot.getId(), assistRequest);
        dismissSnackbar();
    }

    @Override
    public void onDataRemoved(String documentId) {
        assistRequests.get(documentId).getMarker().remove();
        assistRequests.remove(documentId);

        if (assistRequests.size() == 0)
            onEmptyDataSet();
    }

    @Override
    public void onDataModified(DocumentSnapshot documentSnapshot) {
        assistRequests.get(documentSnapshot.getId()).modifiedData(documentSnapshot);
    }

    @Override
    public void onEmptyDataSet() {
        if (isVisible()) {
            snackbar = Snackbar.make(getView().findViewById(R.id.volunteerRequestMapViewConstraintLayout), "No posted requests within this area.",
                    Snackbar.LENGTH_INDEFINITE).setAction("Ok", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                }
            });
            snackbar.show();
        }
    }

    private void dismissSnackbar() {
        if (assistRequests.size() > 0 && snackbar != null && snackbar.isShown())
            snackbar.dismiss();
    }
}
