package com.caltruism.assist.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
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
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.arlib.floatingsearchview.FloatingSearchView;
import com.arlib.floatingsearchview.util.view.MenuView;
import com.caltruism.assist.BuildConfig;
import com.caltruism.assist.R;
import com.caltruism.assist.activity.FilterActivity;
import com.caltruism.assist.activity.MapViewActivity;
import com.caltruism.assist.activity.RequestDetailsActivity;
import com.caltruism.assist.data.AssistRequest;
import com.caltruism.assist.util.Constants;
import com.caltruism.assist.util.CustomCallbackListener;
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
import static com.google.android.gms.location.places.AutocompleteFilter.TYPE_FILTER_REGIONS;

public class VolunteerRequestListFragment extends Fragment {

    public static final String TAG = "VolunteerRequestListFragment";
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 0;
    private static final int REQUEST_CHECK_SETTINGS = 0x1;
    private static final int REQUEST_CODE_AUTOCOMPLETE = 1;

    private static final double DEFAULT_QUERY_RANGE_MILES = 5;
    private static final double DEFAULT_QUERY_RANGE_KM = DEFAULT_QUERY_RANGE_MILES * Constants.MILES_TO_KM;

    private CustomCallbackListener.VolunteerRequestListFragmentCallbackListener callbackListener;

    private FloatingSearchView searchView;

    private VolunteerRequestListViewFragment listViewFragment;
    private VolunteerRequestMapViewFragment mapViewFragment;
    private boolean isListView;

    private FusedLocationProviderClient fusedLocationClient;
    private SettingsClient settingsClient;
    private LocationRequest locationRequest;
    private LocationSettingsRequest locationSettingsRequest;
    private LocationCallback locationCallback;

    private CollectionReference collectionReference;
    private GeoQuery geoQuery;
    private GeoQueryDataEventListener geoQueryDataEventListener;
    private boolean isInitialQueryCompleted = false;

    private Set<String> oldLocationKeySet = new HashSet<>();
    private HashMap<String, AssistRequest> assistRequests = new HashMap<>();
    private boolean isUsingCurrentLocation = true;
    private Location currentLocation;
    private Location lastUpdatedLocation;
    private LatLng cameraLocation;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof CustomCallbackListener.VolunteerRequestListFragmentCallbackListener)
            callbackListener = (CustomCallbackListener.VolunteerRequestListFragmentCallbackListener) context;
        else
            throw new RuntimeException(context.toString() + " must implement CustomCallbackListener.VolunteerRequestListFragmentCallbackListener");
    }

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

        if(callbackListener != null){
            callbackListener.onAttachSearchViewToDrawer(searchView);
        }

        initializeFragments();
        isListView = true;

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

        locationRequest = new LocationRequest().setInterval(Constants.UPDATE_INTERVAL_IN_MILLISECONDS)
                .setFastestInterval(Constants.FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        locationSettingsRequest = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest).build();

        collectionReference = FirebaseFirestore.getInstance().collection("requests");

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
                searchView.setSearchText(place.getAddress().toString().replaceAll(", USA", ""));
                searchView.moveSelectorBeginning();
                cameraLocation = place.getLatLng();
                isUsingCurrentLocation = false;
                mapViewFragment.onNewLocations(currentLocation, cameraLocation, false, true);
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

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (geoQuery != null)
            geoQuery.removeAllListeners();
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
                if (item.getItemId() == R.id.actionFilter) {
                    Intent intent = new Intent(getActivity(), FilterActivity.class);
                    startActivity(intent);
                } else if (item.getItemId() == R.id.actionChangeView) {
                    if (isListView) {
                        isListView = false;
                        item.setIcon(R.drawable.ic_menu_list);
                        searchView.swapMenuIcon(item);
                        showMapViewFragment();
                    } else {
                        isListView = true;
                        item.setIcon(R.drawable.ic_menu_map);
                        searchView.swapMenuIcon(item);
                        showListViewFragment();
                    }
                }
            }
        });
    }

    private void initializeFragments() {
        FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.fragmentVolunteerMain, listViewFragment);
        fragmentTransaction.add(R.id.fragmentVolunteerMain, mapViewFragment);
        fragmentTransaction.hide(mapViewFragment);
        fragmentTransaction.commit();
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
                                Snackbar.make(getView().findViewById(R.id.volunteerRequestListConstraintLayout), errorMessage, Snackbar.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void setupGeoQueryDataEventListener() {
        geoQueryDataEventListener = new GeoQueryDataEventListener() {
            @Override
            public void onDocumentEntered(DocumentSnapshot documentSnapshot, GeoPoint geoPoint) {
                if (isInitialQueryCompleted)
                    notifyDataAdded(documentSnapshot);
                else
                    assistRequests.put(documentSnapshot.getId(), new AssistRequest(documentSnapshot, currentLocation));
            }

            @Override
            public void onDocumentExited(DocumentSnapshot documentSnapshot) {
                assistRequests.remove(documentSnapshot.getId());
                if (isInitialQueryCompleted)
                    notifyDataRemoved(documentSnapshot.getId());
            }

            @Override
            public void onDocumentMoved(DocumentSnapshot documentSnapshot, GeoPoint geoPoint) {}

            @Override
            public void onDocumentChanged(DocumentSnapshot documentSnapshot, GeoPoint geoPoint) {
                if (isInitialQueryCompleted)
                    notifyDataModified(documentSnapshot);
            }

            @Override
            public void onGeoQueryReady() {
                Log.e(TAG, "Initial query completed");
                notifyNewDataSet();
                notifyNewLocations();
            }

            @Override
            public void onGeoQueryError(Exception e) {
                Log.e(TAG, "setupGeoQueryDataEventListener Error: ", e);
            }
        };
    }

    private void openAutocompleteActivity() {
        try {
            AutocompleteFilter typeFilter = new AutocompleteFilter.Builder().setTypeFilter(TYPE_FILTER_REGIONS).setCountry("US").build();
            Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY).setFilter(typeFilter).build(getActivity());
            startActivityForResult(intent, REQUEST_CODE_AUTOCOMPLETE);
        } catch (GooglePlayServicesRepairableException e) {
            GoogleApiAvailability.getInstance().getErrorDialog(getActivity(), e.getConnectionStatusCode(), 0).show();
        } catch (GooglePlayServicesNotAvailableException e) {
            String message = "Google Play Services is not available: " + GoogleApiAvailability.getInstance().getErrorString(e.errorCode);

            Log.e(TAG, message);
            Snackbar.make(getView().findViewById(R.id.volunteerRequestListConstraintLayout), message, Snackbar.LENGTH_SHORT).show();
        }
    }

    private void showListViewFragment() {
        FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
        fragmentTransaction.show(listViewFragment);
        fragmentTransaction.hide(mapViewFragment);
        fragmentTransaction.commit();
    }

    private void showMapViewFragment() {
        FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
        fragmentTransaction.show(mapViewFragment);
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
        geoQuery.addGeoQueryDataEventListener(geoQueryDataEventListener, Constants.REQUEST_STATUS_WAITING);
    }

    private void notifyNewDataSet() {
        isInitialQueryCompleted = true;

        if (!oldLocationKeySet.equals(assistRequests.keySet())) {
            oldLocationKeySet.clear();
            oldLocationKeySet.addAll(assistRequests.keySet());

            listViewFragment.onNewDataSet(assistRequests);
            mapViewFragment.onNewDataSet(assistRequests);
        } else if (assistRequests.size() == 0) {
            listViewFragment.onEmptyDataSet();
        }
    }

    private void notifyDataAdded(DocumentSnapshot documentSnapshot) {
        assistRequests.put(documentSnapshot.getId(), new AssistRequest(documentSnapshot, currentLocation));
        listViewFragment.onDataAdded(documentSnapshot);
        mapViewFragment.onDataAdded(documentSnapshot);
    }

    private void notifyDataRemoved(String documentId) {
        assistRequests.remove(documentId);
        listViewFragment.onDataRemoved(documentId);
        mapViewFragment.onDataRemoved(documentId);
    }

    private void notifyDataModified(DocumentSnapshot documentSnapshot) {
        assistRequests.get(documentSnapshot.getId()).modifiedData(documentSnapshot, currentLocation);
        listViewFragment.onDataModified(documentSnapshot);
        mapViewFragment.onDataModified(documentSnapshot);
    }

    private void notifyNewLocations() {
        if (currentLocation != null) {
            listViewFragment.onNewLocations(currentLocation, null, isUsingCurrentLocation, false);
            mapViewFragment.onNewLocations(currentLocation, cameraLocation, isUsingCurrentLocation, false);
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
                Log.i(TAG, "User interaction was canceled.");
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
