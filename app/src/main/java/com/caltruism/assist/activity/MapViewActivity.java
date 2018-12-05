package com.caltruism.assist.activity;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.caltruism.assist.R;
import com.caltruism.assist.util.BitMapDescriptorFromVector;
import com.caltruism.assist.util.Constants;
import com.caltruism.assist.util.CustomDateTimeUtil;
import com.caltruism.assist.util.CustomRequestAcceptedDialog;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapViewActivity extends AppCompatActivity implements OnMapReadyCallback {
    private Toolbar toolbar;
    private TextView title;

    private MapView mapView;

    private GoogleMap map;
    private LatLng locationLatLng;
    private String locationName;
    private String locationAddress;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_view);

        mapView = findViewById(R.id.mapViewMapViewActivity);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        locationLatLng = getIntent().getExtras().getParcelable("locationLatLng");
        locationName = getIntent().getExtras().getString("locationName");
        locationAddress = getIntent().getExtras().getString("locationAddress");;

        setupToolbar();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);

        mapView = findViewById(R.id.mapViewRequestDetails);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.getUiSettings().setMyLocationButtonEnabled(false);
        map.getUiSettings().setMapToolbarEnabled(false);
        map.setMyLocationEnabled(true);

        if (locationName == null || locationName.length() == 0) {
            map.addMarker(new MarkerOptions().position(locationLatLng)
                    .title(locationAddress)
                    .icon(BitMapDescriptorFromVector.regularMarker(this)));
        } else {
            map.addMarker(new MarkerOptions().position(locationLatLng)
                    .title(locationName).snippet(locationAddress)
                    .icon(BitMapDescriptorFromVector.regularMarker(this)));
        }
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(locationLatLng, Constants.DEFAULT_ZOOM));
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, new IntentFilter("disabled-request-accepted"));
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(messageReceiver);
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
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public void setupToolbar() {
        toolbar = findViewById(R.id.toolbarMapView);
        title = findViewById(R.id.textViewMapViewToolbarTitle);
        title.setText("Task Location");
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        toolbar.postDelayed(new Runnable()
        {
            @Override
            public void run ()
            {
                int maxWidth = toolbar.getWidth();
                int titleWidth = title.getWidth();
                int iconWidth = maxWidth - titleWidth;

                if (iconWidth > 0) {
                    int width = maxWidth - iconWidth * 2;
                    title.setMinimumWidth(width);
                    title.getLayoutParams().width = width;
                }
            }
        }, 0);
    }

    private BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, final Intent intent) {
            CustomRequestAcceptedDialog.showDialog(MapViewActivity.this, intent);
        }
    };

}
