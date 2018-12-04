package com.caltruism.assist.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.caltruism.assist.R;
import com.caltruism.assist.data.AssistRequest;
import com.caltruism.assist.util.BitMapDescriptorFromVector;
import com.caltruism.assist.util.Constants;
import com.caltruism.assist.util.CustomCallbackListener;
import com.caltruism.assist.util.DirectionsJSONParser;
import com.caltruism.assist.util.GoogleMapsRoutes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CustomCap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONObject;

import java.util.ArrayList;

public class CurrentRequestMapViewFragment extends Fragment implements
        OnMapReadyCallback, CustomCallbackListener.RouteParserCallbackListener,
        CustomCallbackListener.CurrentRequestMapViewFragmentCallbackListener {

    private CustomCallbackListener.CurrentRequestActivityCallbackListener callbackListener;

    private MapView mapView;
    private Button buttonShareLocation;
    private Button buttonStopSharingLocation;

    private GoogleMap map;
    private Location currentLocation;
    private LatLng originLatLng;
    private Polyline polyline;
    private LatLng boundNortheast;
    private LatLng boundSouthwest;

    private boolean isVolunteerView;
    private AssistRequest assistRequest;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof CustomCallbackListener.CurrentRequestActivityCallbackListener) {
            callbackListener = (CustomCallbackListener.CurrentRequestActivityCallbackListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement CustomCallbackListener.CurrentRequestActivityCallbackListener!");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        isVolunteerView = arguments.getBoolean("isVolunteerView");
        assistRequest = arguments.getParcelable("requestData");

        return inflater.inflate(R.layout.fragment_current_request_map_view, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mapView = view.findViewById(R.id.mapViewCurrentRequest);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        buttonShareLocation = view.findViewById(R.id.buttonCurrentRequestShareLocation);
        buttonStopSharingLocation = view.findViewById(R.id.buttonCurrentRequestStopSharingLocation);

        if (isVolunteerView) {
            buttonShareLocation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    buttonShareLocation.setVisibility(View.INVISIBLE);
                    buttonStopSharingLocation.setVisibility(View.VISIBLE);

                    callbackListener.onSharingLocationPermissionChange(true);
                }
            });

            buttonStopSharingLocation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    buttonShareLocation.setVisibility(View.VISIBLE);
                    buttonStopSharingLocation.setVisibility(View.GONE);

                    callbackListener.onSharingLocationPermissionChange(false);
                }
            });
        } else {
            buttonShareLocation.setVisibility(View.GONE);
            buttonStopSharingLocation.setVisibility(View.GONE);
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setMapStyle(MapStyleOptions.loadRawResourceStyle(getActivity(), R.raw.map_style_json));
        map.getUiSettings().setMyLocationButtonEnabled(false);
        map.getUiSettings().setMapToolbarEnabled(false);
        map.setMinZoomPreference(Constants.MIN_ZOOM);
        map.setMaxZoomPreference(Constants.MAX_ZOOM);
        map.setMyLocationEnabled(true);

        if (isVolunteerView)
            map.setPadding(0, 0, 0, 168);

        if (originLatLng != null) {
            GoogleMapsRoutes googleMapsRoutes = new GoogleMapsRoutes(getActivity(),
                    originLatLng, assistRequest.getLocationLatLng(), this);
            originLatLng = null;
        }

        googleMap.addMarker(new MarkerOptions().position(assistRequest.getLocationLatLng())
                .icon(BitMapDescriptorFromVector.regularMarker(getActivity())));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(assistRequest.getLocationLatLng(), Constants.DEFAULT_ZOOM));

        FloatingActionButton fab = getView().findViewById(R.id.fabCurrentRequestCurrentLocation);
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
    public void onLatLngAndDurationCompleted(ArrayList<Object> data) {
        boundNortheast = (LatLng) data.get(0);
        boundSouthwest = (LatLng) data.get(1);
        callbackListener.onNewDurationData((Integer) data.get(2));
    }

    @Override
    public void onTaskCompleted(PolylineOptions polylineOptions) {
        if (!isVolunteerView)
            polylineOptions.startCap(new CustomCap(BitMapDescriptorFromVector.otherUserCap(getActivity())));

        if (polyline != null)
            polyline.remove();

        polyline = map.addPolyline(polylineOptions);

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(boundSouthwest);
        builder.include(boundNortheast);
        builder.include(assistRequest.getLocationLatLng());
        map.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 100));
    }

    @Override
    public void onNewCurrentLocation(Location newCurrentLocation) {
        currentLocation = newCurrentLocation;
    }

    @Override
    public void onNewOriginLocation(Location newOriginLocation) {
        if (getView() != null) {
            GoogleMapsRoutes googleMapsRoutes = new GoogleMapsRoutes(getActivity(),
                    new LatLng(newOriginLocation.getLatitude(), newOriginLocation.getLongitude()),
                    assistRequest.getLocationLatLng(), this);
        } else {
            originLatLng = new LatLng(newOriginLocation.getLatitude(), newOriginLocation.getLongitude());
        }
    }
}
