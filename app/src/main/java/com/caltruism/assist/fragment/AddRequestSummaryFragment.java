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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.caltruism.assist.R;
import com.caltruism.assist.utils.CustomDateTimeUtil;
import com.caltruism.assist.utils.CustomMapView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

public class AddRequestSummaryFragment extends Fragment implements OnMapReadyCallback, ActivityCompat.OnRequestPermissionsResultCallback {

    public static final String TAG = "AddRequestSummaryFragment";
    private static final int REQUEST_LOCATION = 0;

    private int requestType;
    private String requestTitle;
    private String requestDescription;
    private String requestLocation;
    private String requestDateTime;
    private String requestDuration;

    CustomMapView mapView;
    GoogleMap map;

    TextView textViewTitle;
    TextView textViewDate;
    TextView textViewType;
    TextView textViewTime;
    TextView textViewDuration;
    TextView textViewLocationAddress;
    TextView textViewDescription;
    ImageView imageViewType;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        requestType = arguments.getInt("requestType");
        requestTitle = arguments.getString("requestTitle");
        requestDescription = arguments.getString("requestDescription");
        requestLocation = arguments.getString("requestLocation");
        requestDateTime = arguments.getString("requestDateTime");
        requestDuration = arguments.getString("requestDuration");

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
        textViewLocationAddress = getView().findViewById(R.id.textViewAddRequestSummaryLocationAddress);
        textViewDescription = getView().findViewById(R.id.textViewAddRequestSummaryDescription);
        imageViewType = getView().findViewById(R.id.imageViewAddRequestSummaryType);

        setUpViews();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.getUiSettings().setMyLocationButtonEnabled(false);
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestLocationPermission();
        } else {
            map.setMyLocationEnabled(true);
        }

        // Updates the location and zoom of the MapView
        /*CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(43.1, -87.9), 10);
        map.animateCamera(cameraUpdate);*/
        LatLng sydney = new LatLng(-33.852, 151.211);
        googleMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 15));
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

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        if (requestCode == REQUEST_LOCATION) {
            // BEGIN_INCLUDE(permission_result)
            // Received permission result for camera permission.
            Log.i(TAG, "Received response for Camera permission request.");

            // Check if the only required permission has been granted
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Camera permission has been granted, preview can be displayed
                Log.i(TAG, "CAMERA permission has now been granted. Showing preview.");
                Snackbar.make(getView(), "granted",
                        Snackbar.LENGTH_SHORT).show();
                map.setMyLocationEnabled(true);

            } else {
                Log.i(TAG, "CAMERA permission was NOT granted.");
                Snackbar.make(getView(), "not granted",
                        Snackbar.LENGTH_SHORT).show();

            }
            // END_INCLUDE(permission_result)

        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void setUpViews() {
        textViewTitle.setText(requestTitle);
        textViewLocationAddress.setText(requestLocation);
        textViewDescription.setText(requestDescription);
        textViewDuration.setText(CustomDateTimeUtil.getFormattedDuration(requestDuration));

        List<String> dateTime = CustomDateTimeUtil.getDateWithDayandTime(requestDateTime);
        if (dateTime != null && dateTime.size() == 2) {
            textViewDate.setText(dateTime.get(0));
            textViewTime.setText(dateTime.get(1));
        }

        switch (requestType) {
            case 0:
                imageViewType.setImageResource(R.drawable.ic_shopping_cart_solid);
                textViewType.setText("Grocery");
                break;

            case 1:
                imageViewType.setImageResource(R.drawable.ic_washer_solid);
                textViewType.setText("Laundry");
                break;

            case 2:
                imageViewType.setImageResource(R.drawable.ic_walking_solid);
                textViewType.setText("Walking");
                break;

            case 3:
                imageViewType.setImageResource(R.drawable.ic_question_solid);
                textViewType.setText("Other Request");
                break;
        }
    }

    private void requestLocationPermission() {
        Log.i(TAG, "CAMERA permission has NOT been granted. Requesting permission.");

        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // For example if the user has previously denied the permission.
            Log.i(TAG,
                    "Displaying camera permission rationale to provide additional context.");
            Snackbar.make(getView(), "get permission",
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction("OK", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ActivityCompat.requestPermissions(getActivity(),
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    REQUEST_LOCATION);
                        }
                    })
                    .show();
        } else {

            // Camera permission has not been granted yet. Request it directly.
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION);
        }
    }
}