package com.caltruism.assist.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.caltruism.assist.BuildConfig;
import com.caltruism.assist.R;
import com.caltruism.assist.data.AssistRequest;
import com.caltruism.assist.fragment.CurrentRequestMapViewFragment;
import com.caltruism.assist.fragment.CurrentRequestWaitingViewFragment;
import com.caltruism.assist.util.Constants;
import com.caltruism.assist.util.CustomCallbackListener;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Objects;

public class CurrentRequestActivity extends AppCompatActivity implements CustomCallbackListener.CurrentRequestActivityCallbackListener {

    private static final String TAG = "CurrentRequestActivity";
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 0;
    private static final int REQUEST_CHECK_SETTINGS = 0x1;

    private Toolbar toolbar;
    private TextView title;

    private ImageView imageViewProfile;
    private View viewActionCall;

    private CurrentRequestWaitingViewFragment waitingViewFragment;
    private CurrentRequestMapViewFragment mapViewFragment;

    private FusedLocationProviderClient fusedLocationClient;
    private SettingsClient settingsClient;
    private LocationRequest locationRequest;
    private LocationSettingsRequest locationSettingsRequest;
    private LocationCallback locationCallback;
    private Location currentLocation;
    private boolean isSharedLocation = false;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private ListenerRegistration listenerRegistration;

    private boolean isVolunteerView;
    private AssistRequest assistRequest;
    private boolean isStored;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_request);

        isVolunteerView = getIntent().getExtras().getBoolean("isVolunteerView");
        assistRequest = getIntent().getExtras().getParcelable("requestData");

        setupToolbar();
        setupFragment();

        TextView textViewName = findViewById(R.id.textViewCurrentRequestName);
        TextView textViewViewProfile = findViewById(R.id.textViewCurrentRequestViewProfile);
        imageViewProfile = findViewById(R.id.imageViewCurrentRequestProfilePicture);
        viewActionCall = findViewById(R.id.viewCurrentRequestCall);

        if (isVolunteerView) {
            textViewName.setText(assistRequest.getPostedByName());
            setUpProfileImageViewAndPhoneView(assistRequest.getPostedByUid());
        } else {
            textViewName.setText(assistRequest.getAcceptedByMainName());
            setUpProfileImageViewAndPhoneView(assistRequest.getAcceptedByMainUid());
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        settingsClient = LocationServices.getSettingsClient(this);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                Log.d(TAG, "Location updated: " + locationResult.getLastLocation().getLongitude() + " " + locationResult.getLastLocation().getLatitude());
                currentLocation = locationResult.getLastLocation();
                mapViewFragment.onNewCurrentLocation(currentLocation);

                if (isVolunteerView) {
                    mapViewFragment.onNewOriginLocation(currentLocation);
                    storeData(null);
                }
            }
        };

        locationRequest = new LocationRequest().setInterval(Constants.UPDATE_INTERVAL_IN_MILLISECONDS)
                .setFastestInterval(Constants.FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        locationSettingsRequest = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest).build();

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        if (!isVolunteerView)
            queryData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_current_request, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.actionInfo) {
            Intent intent = new Intent(this, RequestDetailsActivity.class);
            intent.putExtra("isVolunteerView", isVolunteerView);
            intent.putExtra("requestData", assistRequest);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public void setupToolbar() {
        toolbar = findViewById(R.id.toolbarCurrentRequest);
        title = findViewById(R.id.textViewCurrentRequestToolbarTitle);
        setSupportActionBar(toolbar);
        title.setText(assistRequest.getTitle());

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        toolbar.postDelayed(new Runnable() {
            @Override
            public void run() {
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

    private void setupFragment() {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        mapViewFragment = new CurrentRequestMapViewFragment();

        Bundle arguments = new Bundle();
        arguments.putBoolean("isVolunteerView", isVolunteerView);
        arguments.putParcelable("requestData", assistRequest);
        mapViewFragment.setArguments(arguments);

        if (isVolunteerView) {
            fragmentTransaction.add(R.id.frameLayoutCurrentRequest, mapViewFragment);
        } else {
            waitingViewFragment = new CurrentRequestWaitingViewFragment();
            fragmentTransaction.add(R.id.frameLayoutCurrentRequest, waitingViewFragment);
        }

        fragmentTransaction.commit();
    }


    private void setUpProfileImageViewAndPhoneView(String uid) {
        FirebaseFirestore.getInstance().collection("users").document(uid)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();

                    assert document != null;

                    if (document.exists()) {
                        Context context = CurrentRequestActivity.this;
                        Object profileImageUrl = document.get("pictureURL");
                        final Object phoneNumber = document.get("phoneNumber");

                        if (profileImageUrl != null) {
                            String url = profileImageUrl.toString();

                            RequestOptions requestOptions = new RequestOptions().placeholder(R.drawable.ic_user_solid).centerCrop();
                            Glide.with(context).setDefaultRequestOptions(requestOptions).load(url).into(imageViewProfile);

                            viewActionCall.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent(Intent.ACTION_DIAL);
                                    intent.setData(Uri.parse(String.format("tel:%s", phoneNumber)));
                                    startActivity(intent);
                                }
                            });
                        }
                    } else {
                        Log.e(TAG, "DOCUMENT NOT EXIST");
                    }
                } else {
                    Log.e(TAG, "Get failed with ", task.getException());
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (!checkPermissions())
            requestPermissions();
        else
            startLocationUpdates();
    }

    @Override
    protected void onStop() {
        super.onStop();

        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (listenerRegistration != null)
            listenerRegistration.remove();
    }

    private void startLocationUpdates() {
        settingsClient.checkLocationSettings(locationSettingsRequest)
                .addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        int statusCode = ((ApiException) e).getStatusCode();
                        switch (statusCode) {
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                try {
                                    ResolvableApiException rae = (ResolvableApiException) e;
                                    rae.startResolutionForResult(CurrentRequestActivity.this, REQUEST_CHECK_SETTINGS);
                                } catch (IntentSender.SendIntentException sie) {
                                    Log.e(TAG, "PendingIntent unable to execute request.", sie);
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                String errorMessage = "Location settings are inadequate, and cannot be fixed here. Fix in Settings.";
                                Log.e(TAG, errorMessage);
                                Snackbar.make(CurrentRequestActivity.this.findViewById(R.id.currentRequestConstraintLayout), errorMessage, Snackbar.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void storeData(String durationInMins) {
        if (isSharedLocation || !isStored) {
            HashMap<String, Object> locationData = new HashMap<>();

            locationData.put("requestID", assistRequest.getId());
            locationData.put("isShared", isSharedLocation);

            if (isSharedLocation) {
                isStored = false;
                locationData.put("latLng", new GeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude()));
            } else {
                isStored = true;
            }

            if (durationInMins != null)
                locationData.put("duration", durationInMins);

            db.collection("locations").document(Objects.requireNonNull(auth.getCurrentUser()).getUid()).set(locationData, SetOptions.merge())
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "Document added/updated with ID: " + auth.getCurrentUser().getUid());
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, "Error adding document", e);
                }
            });
        }
    }

    private void queryData() {
        DocumentReference docRef = db.collection("locations").document(assistRequest.getAcceptedByMainUid());
        listenerRegistration = docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable DocumentSnapshot documentSnapshot, @javax.annotation.Nullable FirebaseFirestoreException e) {
                if (e == null && documentSnapshot != null) {
                    if (documentSnapshot.exists() && documentSnapshot.getString("requestID").equals(assistRequest.getId())) {
                        if (documentSnapshot.getBoolean("isShared")) {
                            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

                            if (waitingViewFragment.isVisible())
                                fragmentTransaction.hide(waitingViewFragment);

                            if (!mapViewFragment.isAdded())
                                fragmentTransaction.add(R.id.frameLayoutCurrentRequest, mapViewFragment);
                            else
                                fragmentTransaction.show(mapViewFragment);

                            fragmentTransaction.commit();

                            GeoPoint gp = (GeoPoint) documentSnapshot.get("latLng");
                            Location location = new Location(LocationManager.GPS_PROVIDER);
                            location.setLatitude(gp.getLatitude());
                            location.setLongitude(gp.getLongitude());
                            mapViewFragment.onNewOriginLocation(location);
                        } else {
                            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

                            if (mapViewFragment.isVisible())
                                fragmentTransaction.hide(mapViewFragment);

                            if (!waitingViewFragment.isAdded())
                                fragmentTransaction.add(R.id.frameLayoutCurrentRequest, waitingViewFragment);
                            else
                                fragmentTransaction.show(waitingViewFragment);

                            fragmentTransaction.commit();

                            waitingViewFragment.onNewDuration(documentSnapshot.getString("duration"));
                        }
                    } else {
                        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

                        if (mapViewFragment.isVisible())
                            fragmentTransaction.hide(mapViewFragment);

                        if (!waitingViewFragment.isAdded())
                            fragmentTransaction.add(R.id.frameLayoutCurrentRequest, waitingViewFragment);
                        else
                            fragmentTransaction.show(waitingViewFragment);

                        fragmentTransaction.commit();

                        waitingViewFragment.onVolunteerOffline();
                    }
                } else {
                    Log.e(TAG, "queryData Error: ", e);
                }
            }
        });
    }


    private boolean checkPermissions() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            // TODO: Rephrase
            Snackbar.make(findViewById(android.R.id.content), "Location permission is needed for core functionality",
                    Snackbar.LENGTH_INDEFINITE).setAction("Ok", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ActivityCompat.requestPermissions(CurrentRequestActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            REQUEST_PERMISSIONS_REQUEST_CODE);
                }
            }).show();
        } else {
            ActivityCompat.requestPermissions(CurrentRequestActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                Log.i(TAG, "User interaction was cancelled.");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates();
            } else {
                // TODO: Rephrase
                Snackbar.make(findViewById(android.R.id.content), "We need your location to provide better user experience.",
                        Snackbar.LENGTH_INDEFINITE).setAction("Settings", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        intent.setData(Uri.fromParts("package", BuildConfig.APPLICATION_ID, null));
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                }).show();
            }
        }
    }

    @Override
    public void onSharingLocationPermissionChange(boolean isSharing) {
        isSharedLocation = isSharing;
    }

    @Override
    public void onNewDurationData(int durationInMins) {
        storeData(String.valueOf(durationInMins));
    }
}
