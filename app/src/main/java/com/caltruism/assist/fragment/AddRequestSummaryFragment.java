package com.caltruism.assist.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.caltruism.assist.R;
import com.caltruism.assist.utils.AddRequestFragmentDataListener;
import com.caltruism.assist.utils.Constants;
import com.caltruism.assist.utils.CustomDateTimeUtil;
import com.caltruism.assist.utils.CustomMapView;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

public class AddRequestSummaryFragment extends Fragment implements AddRequestFragmentDataListener, OnMapReadyCallback, ActivityCompat.OnRequestPermissionsResultCallback {

    public static final String TAG = "AddRequestSummaryFragment";
    private static final float ZOOM_LEVEL = 18;

    TextView textViewTitle;
    TextView textViewDate;
    TextView textViewType;
    TextView textViewTime;
    TextView textViewDuration;
    TextView textViewLocationName;
    TextView textViewLocationAddress;
    TextView textViewDescription;
    ImageView imageViewType;
    CustomMapView mapView;

    GoogleMap map;
    Marker marker;

    private int requestType;
    private String requestTitle;
    private String requestDescription;
    private String requestLocationName;
    private String requestLocationAddress;
    private LatLng requestLocationLatLng;
    private boolean requestIsNow;
    private long requestDateTime;
    private int requestDuration;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        requestType = arguments.getInt("requestType");
        requestTitle = arguments.getString("requestTitle");
        requestDescription = arguments.getString("requestDescription");
        requestLocationAddress = arguments.getString("requestLocationAddress");
        requestLocationName = arguments.getString("requestLocationName");
        requestLocationLatLng = arguments.getParcelable("requestLocationLatLng");
        requestIsNow = arguments.getBoolean("requestIsNow");
        requestDateTime = arguments.getLong("requestDateTime");
        requestDuration = arguments.getInt("requestDuration");

        return inflater.inflate(R.layout.fragment_add_request_summary, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mapView = getView().findViewById(R.id.mapViewAddRequestSummary);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        textViewTitle = getView().findViewById(R.id.textViewAddRequestSummaryTitle);
        textViewDate = getView().findViewById(R.id.textViewAddRequestSummaryDate);
        textViewType = getView().findViewById(R.id.textViewAddRequestSummaryType);
        textViewTime = getView().findViewById(R.id.textViewAddRequestSummaryTime);
        textViewDuration = getView().findViewById(R.id.textViewAddRequestSummaryDuration);
        textViewLocationName = getView().findViewById(R.id.textViewAddRequestSummaryLocationName);
        textViewLocationAddress = getView().findViewById(R.id.textViewAddRequestSummaryLocationAddress);
        textViewDescription = getView().findViewById(R.id.textViewAddRequestSummaryDescription);
        imageViewType = getView().findViewById(R.id.imageViewAddRequestSummaryType);

        setUpViews();
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.getUiSettings().setMyLocationButtonEnabled(false);
        map.getUiSettings().setZoomGesturesEnabled(true);
        map.setMyLocationEnabled(true);

        marker = googleMap.addMarker(new MarkerOptions().position(requestLocationLatLng).title(requestTitle));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(requestLocationLatLng, ZOOM_LEVEL));
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
    public void onDataChange(Object... data) {
        requestType = (int) data[0];
        requestTitle = (String) data[1];
        requestDescription = (String) data[2];
        requestLocationName = (String) data[3];
        requestLocationAddress = (String) data[4];
        requestLocationLatLng = (LatLng) data[5];
        requestIsNow = (boolean) data[6];
        requestDateTime = (long) data[7];
        requestDuration = (int) data[8];

        setUpViews();
    }

    private void setUpViews() {
        textViewTitle.setText(requestTitle);
        textViewLocationAddress.setText(requestLocationAddress);
        textViewDescription.setText(requestDescription);
        textViewDuration.setText(CustomDateTimeUtil.getFormattedDuration(requestDuration));

        if (requestIsNow) {
            textViewDate.setText(CustomDateTimeUtil.getDateWithDay(requestDateTime));
            textViewTime.setText(Html.fromHtml("<font color='#f48760'>Now</font>", Html.FROM_HTML_MODE_LEGACY));
        } else {
            String[] dateTime = CustomDateTimeUtil.getDateWithDayAndTime(requestDateTime);
            if (dateTime.length == 2) {
                textViewDate.setText(dateTime[0]);
                textViewTime.setText(dateTime[1]);
            }
        }

        if (requestLocationName.length() > 0) {
            if (textViewLocationName.getVisibility() == View.GONE)
                textViewLocationName.setVisibility(View.VISIBLE);
            textViewLocationName.setText(requestLocationName);
        } else if (textViewLocationName.getVisibility() == View.VISIBLE) {
            textViewLocationName.setVisibility(View.GONE);
        }

        if (map != null && marker != null) {
            marker.remove();
            marker = map.addMarker(new MarkerOptions().position(requestLocationLatLng).title(requestTitle));
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(requestLocationLatLng, ZOOM_LEVEL));
        }

        switch (requestType) {
            case Constants.GROCERY_TYPE:
                imageViewType.setImageResource(R.drawable.ic_shopping_cart_solid);
                textViewType.setText("Grocery");
                break;

            case Constants.LAUNDRY_TYPE:
                imageViewType.setImageResource(R.drawable.ic_washer_solid);
                textViewType.setText("Laundry");
                break;

            case Constants.WALKING_TYPE:
                imageViewType.setImageResource(R.drawable.ic_walking_solid);
                textViewType.setText("Walking");
                break;

            case Constants.OTHER_TYPE:
                imageViewType.setImageResource(R.drawable.ic_question_solid);
                textViewType.setText("Other Request");
                break;
        }
    }
}