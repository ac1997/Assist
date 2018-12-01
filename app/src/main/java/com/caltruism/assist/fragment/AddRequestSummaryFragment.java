package com.caltruism.assist.fragment;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.caltruism.assist.R;
import com.caltruism.assist.util.BitMapDescriptorFromVector;
import com.caltruism.assist.util.Constants;
import com.caltruism.assist.util.CustomCallbackListener;
import com.caltruism.assist.util.CustomDateTimeUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class AddRequestSummaryFragment extends Fragment implements
        OnMapReadyCallback, ActivityCompat.OnRequestPermissionsResultCallback,
        CustomCallbackListener.AddRequestFragmentCallbackListener {

    public static final String TAG = "AddRequestSummaryFragment";

    private TextView textViewTitle;
    private TextView textViewDate;
    private TextView textViewType;
    private TextView textViewTime;
    private TextView textViewDuration;
    private TextView textViewLocationName;
    private TextView textViewLocationAddress;
    private TextView textViewDescription;
    private ImageView imageViewType;
    private MapView mapView;

    private GoogleMap map;
    private Marker marker;

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

        mapView = view.findViewById(R.id.mapViewAddRequestSummary);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        textViewTitle = view.findViewById(R.id.textViewAddRequestSummaryTitle);
        textViewDate = view.findViewById(R.id.textViewAddRequestSummaryDate);
        textViewType = view.findViewById(R.id.textViewAddRequestSummaryType);
        textViewTime = view.findViewById(R.id.textViewAddRequestSummaryTime);
        textViewDuration = view.findViewById(R.id.textViewAddRequestSummaryDuration);
        textViewLocationName = view.findViewById(R.id.textViewAddRequestSummaryLocationName);
        textViewLocationAddress = view.findViewById(R.id.textViewAddRequestSummaryLocationAddress);
        textViewDescription = view.findViewById(R.id.textViewAddRequestSummaryDescription);
        imageViewType = view.findViewById(R.id.imageViewAddRequestSummaryType);

        setUpViews(false);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setMapStyle(MapStyleOptions.loadRawResourceStyle(getActivity(), R.raw.map_style_json));
        map.getUiSettings().setMyLocationButtonEnabled(false);
        map.getUiSettings().setMapToolbarEnabled(false);
        googleMap.getUiSettings().setScrollGesturesEnabled(false);
        map.setMinZoomPreference(Constants.MIN_ZOOM);
        map.setMaxZoomPreference(Constants.MAX_ZOOM);
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            map.setMyLocationEnabled(true);
        }

        marker = googleMap.addMarker(new MarkerOptions().position(requestLocationLatLng)
                .icon(BitMapDescriptorFromVector.regularMarker(getActivity())));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(requestLocationLatLng, Constants.DEFAULT_ZOOM));
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

        setUpViews(true);
    }

    private void setUpViews(boolean isNew) {
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

        if (map != null && marker != null && isNew) {
            marker.remove();
            marker = map.addMarker(new MarkerOptions().position(requestLocationLatLng).title(requestTitle)
                    .icon(BitMapDescriptorFromVector.regularMarker(getActivity())));
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(requestLocationLatLng, Constants.DEFAULT_ZOOM));
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