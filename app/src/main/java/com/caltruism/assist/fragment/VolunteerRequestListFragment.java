package com.caltruism.assist.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.arlib.floatingsearchview.FloatingSearchView;
import com.caltruism.assist.BuildConfig;
import com.caltruism.assist.R;
import com.caltruism.assist.data.AssistRequest;
import com.caltruism.assist.util.Constants;
import com.caltruism.assist.util.VolunteerBaseFragment;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
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
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import org.imperiumlabs.geofirestore.GeoFirestore;
import org.imperiumlabs.geofirestore.GeoQuery;
import org.imperiumlabs.geofirestore.GeoQueryDataEventListener;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import static android.app.Activity.RESULT_OK;
import static com.google.android.gms.location.places.AutocompleteFilter.TYPE_FILTER_CITIES;

public class VolunteerRequestListFragment extends VolunteerBaseFragment {

    public static final String TAG = "VolunteerRequestListFragment";
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 0;
    private static final int REQUEST_CHECK_SETTINGS = 0x1;
    private static final int REQUEST_CODE_AUTOCOMPLETE = 1;

    private static final double DEFAULT_QUERY_RANGE_MILES = 5;
    private static final double DEFAULT_QUERY_RANGE_KM = DEFAULT_QUERY_RANGE_MILES * 1.609344;

    private FloatingSearchView searchView;

    private VolunteerRequestListViewFragment listViewFragment;
    private VolunteerRequestMapViewFragment mapViewFragment;

    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 1000;
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    private FusedLocationProviderClient fusedLocationClient;
    private SettingsClient settingsClient;
    private LocationRequest locationRequest;
    private LocationSettingsRequest locationSettingsRequest;
    private LocationCallback locationCallback;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private CollectionReference collectionReference;
    private GeoQuery geoQuery;
    private GeoQueryDataEventListener geoQueryDataEventListener;
    private boolean isInitialQueryCompleted = false;

    private int currentFragment;
    private Set<String> oldLocationKeySet = new HashSet<>();
    private HashMap<String, AssistRequest> assistRequests = new HashMap<>();
    private boolean isUsingCurrentLocation = true;
    private Location currentLocation;
    private Location lastUpdatedLocation;
    private LatLng cameraLocation;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_volunteer_request_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        listViewFragment = new VolunteerRequestListViewFragment();
        mapViewFragment = new VolunteerRequestMapViewFragment();

        searchView = view.findViewById(R.id.searchViewVolunteer);

        setupSearchView();
        attachSearchViewActivityDrawer(searchView);

        showListViewFragment();
        currentFragment = R.id.actionListView;

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        settingsClient = LocationServices.getSettingsClient(getActivity());

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                if (locationResult != null) {
                    currentLocation = locationResult.getLastLocation();

                    if (lastUpdatedLocation == null || (lastUpdatedLocation.distanceTo(currentLocation) > 5)) {
                        lastUpdatedLocation = currentLocation;
                        if (isUsingCurrentLocation) {
                            cameraLocation = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                            getNearByRequests();
                        }
                    } else if (isUsingCurrentLocation) {
                            cameraLocation = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                            notifyNewLocations();
                    }
                }

                Log.d(TAG, "Location updated: " + locationResult.getLastLocation().getLongitude() + " " + locationResult.getLastLocation().getLatitude());
            }
        };

        locationRequest = new LocationRequest().setInterval(UPDATE_INTERVAL_IN_MILLISECONDS)
                .setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        locationSettingsRequest = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest).build();

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        collectionReference = db.collection("requests");

        setupGeoQueryDataEventListener();
    }

    @Override
    public void onStart() {
        super.onStart();

        if (!checkPermissions())
            requestPermissions();
        else
            startLocationUpdates();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_AUTOCOMPLETE) {
            searchView.setSearchFocused(false);

            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(getActivity(), data);
                searchView.setSearchText(place.getAddress());
                cameraLocation = place.getLatLng();
                isUsingCurrentLocation = false;
                getNearByRequests();
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Log.e(TAG, "Error: Status = " + PlaceAutocomplete.getStatus(getActivity(), data).toString());
            }
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    private void setupSearchView() {
        searchView.setOnFocusChangeListener(new FloatingSearchView.OnFocusChangeListener() {
            @Override
            public void onFocus() {
                searchView.setSearchText("");
                openAutocompleteActivity();
            }

            @Override
            public void onFocusCleared() {}
        });

        searchView.setOnMenuItemClickListener(new FloatingSearchView.OnMenuItemClickListener() {
            @Override
            public void onActionMenuItemSelected(MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.actionFilter) {
                    // FILTER
                    getNearByRequests();
                } else if (currentFragment != itemId) {
                    currentFragment = itemId;

                    if (itemId == R.id.actionListView)
                        showListViewFragment();
                    else if (itemId == R.id.actionMapView)
                        showMapViewFragment();
                }
            }
        });
    }

    private void startLocationUpdates() {
        settingsClient.checkLocationSettings(locationSettingsRequest)
                .addOnSuccessListener(getActivity(), new OnSuccessListener<LocationSettingsResponse>() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
                    }
                })
                .addOnFailureListener(getActivity(), new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        int statusCode = ((ApiException) e).getStatusCode();
                        switch (statusCode) {
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                try {
                                    ResolvableApiException rae = (ResolvableApiException) e;
                                    rae.startResolutionForResult(getActivity(), REQUEST_CHECK_SETTINGS);
                                } catch (IntentSender.SendIntentException sie) {
                                    Log.e(TAG, "PendingIntent unable to execute request.", sie);
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                String errorMessage = "Location settings are inadequate, and cannot be fixed here. Fix in Settings.";
                                Log.e(TAG, errorMessage);
                                Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void setupGeoQueryDataEventListener() {
        geoQueryDataEventListener = new GeoQueryDataEventListener() {
            @Override
            public void onDocumentEntered(DocumentSnapshot documentSnapshot, GeoPoint geoPoint) {
                Log.e(TAG, "Document entered");
                if (isInitialQueryCompleted)
                    notifyDataAdded(documentSnapshot);
                else
                    assistRequests.put(documentSnapshot.getId(), new AssistRequest(documentSnapshot));
            }

            @Override
            public void onDocumentExited(DocumentSnapshot documentSnapshot) {
                Log.e(TAG, "Document exited");
                assistRequests.remove(documentSnapshot.getId());
                if (isInitialQueryCompleted)
                    notifyDataRemoved(documentSnapshot);
            }

            @Override
            public void onDocumentMoved(DocumentSnapshot documentSnapshot, GeoPoint geoPoint) {}

            @Override
            public void onDocumentChanged(DocumentSnapshot documentSnapshot, GeoPoint geoPoint) {
                Log.e(TAG, "Document changed");
                if (isInitialQueryCompleted)
                    notifyDataModified(documentSnapshot);
            }

            @Override
            public void onGeoQueryReady() {
                notifyNewDataSet();
                notifyNewLocations();
                Log.e(TAG, "Initial data completed");
            }

            @Override
            public void onGeoQueryError(Exception e) {
                Log.e(TAG, "onGeoQueryError: ", e);
            }
        };
    }

    private void openAutocompleteActivity() {
        try {
            AutocompleteFilter typeFilter = new AutocompleteFilter.Builder().setTypeFilter(TYPE_FILTER_CITIES).build();
            Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY).setFilter(typeFilter).build(getActivity());
            startActivityForResult(intent, REQUEST_CODE_AUTOCOMPLETE);
        } catch (GooglePlayServicesRepairableException e) {
            GoogleApiAvailability.getInstance().getErrorDialog(getActivity(), e.getConnectionStatusCode(), 0).show();
        } catch (GooglePlayServicesNotAvailableException e) {
            String message = "Google Play Services is not available: " + GoogleApiAvailability.getInstance().getErrorString(e.errorCode);

            Log.e(TAG, message);
            Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
        }
    }

    private void showListViewFragment() {
        FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();

        if (listViewFragment.isAdded()) {
            fragmentTransaction.show(listViewFragment);
        } else {
            Bundle arguments = new Bundle();
//            arguments.putInt("requestType", requestType);
//            arguments.putString("requestTitle", requestTitle);
            listViewFragment.setArguments(arguments);
            fragmentTransaction.add(R.id.fragmentVolunteerMain, listViewFragment);
        }

        if (mapViewFragment.isAdded())
            fragmentTransaction.hide(mapViewFragment);

        fragmentTransaction.commit();
    }

    private void showMapViewFragment() {
        FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();

        if (mapViewFragment.isAdded()) {
            fragmentTransaction.show(mapViewFragment);
        } else {
            Bundle arguments = new Bundle();
            arguments.putParcelable("location", cameraLocation);
            arguments.putSerializable("requests", assistRequests);
            mapViewFragment.setArguments(arguments);
            fragmentTransaction.add(R.id.fragmentVolunteerMain, mapViewFragment);
        }

        if (listViewFragment.isAdded())
            fragmentTransaction.hide(listViewFragment);

        fragmentTransaction.commit();
    }

    private void getNearByRequests() {
        isInitialQueryCompleted = false;
        assistRequests.clear();

        if (geoQuery != null)
            geoQuery.removeAllListeners();

        GeoPoint queryLocation;
        if (isUsingCurrentLocation)
            queryLocation = new GeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude());
        else
            queryLocation = new GeoPoint(cameraLocation.latitude, cameraLocation.longitude);
        geoQuery = new GeoFirestore(collectionReference).queryAtLocation(queryLocation, DEFAULT_QUERY_RANGE_KM);
        geoQuery.addGeoQueryDataEventListener(geoQueryDataEventListener, Constants.REQUEST_STATUS_ONGOING);
    }

    private void notifyNewDataSet() {
        isInitialQueryCompleted = true;

        if (oldLocationKeySet != null && !oldLocationKeySet.equals(assistRequests.keySet())) {
            oldLocationKeySet.clear();
            oldLocationKeySet.addAll(assistRequests.keySet());

//            listViewFragment.onDataChange(assistRequests);
            mapViewFragment.onNewDataSet(assistRequests);
        }
    }

    private void notifyDataAdded(DocumentSnapshot documentSnapshot) {
        assistRequests.put(documentSnapshot.getId(), new AssistRequest(documentSnapshot));
        mapViewFragment.onDataAdded(documentSnapshot);
    }

    private void notifyDataRemoved(DocumentSnapshot documentSnapshot) {
        assistRequests.remove(documentSnapshot.getId());
        mapViewFragment.onDataRemoved(documentSnapshot);
    }

    private void notifyDataModified(DocumentSnapshot documentSnapshot) {
        assistRequests.get(documentSnapshot.getId()).modifiedData(documentSnapshot);
        mapViewFragment.onDataModified(documentSnapshot);
    }

    private void notifyNewLocations() {
        if (currentLocation != null) {
            if (isUsingCurrentLocation)
                mapViewFragment.onNewLocations(currentLocation, null);
            else
                mapViewFragment.onNewLocations(currentLocation, cameraLocation);
        }
    }

    private boolean checkPermissions() {
        return ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)) {
            // TODO: Rephrase
            Snackbar.make(getView().findViewById(android.R.id.content), "Location permission is needed for core functionality",
                    Snackbar.LENGTH_INDEFINITE).setAction("Ok", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ActivityCompat.requestPermissions(getActivity(),
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            REQUEST_PERMISSIONS_REQUEST_CODE);
                }
            }).show();
        } else {
            ActivityCompat.requestPermissions(getActivity(),
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
                if (isUsingCurrentLocation)
                    getNearByRequests();
            } else {
                // TODO: Rephrase
                Snackbar.make(getView().findViewById(android.R.id.content), "We need your location to provide better user experience.",
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
}
