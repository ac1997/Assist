package com.caltruism.assist.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.caltruism.assist.BuildConfig;
import com.caltruism.assist.R;
import com.caltruism.assist.fragment.AddRequestInputDateTimeFragment;
import com.caltruism.assist.fragment.AddRequestInputDetailsFragment;
import com.caltruism.assist.fragment.AddRequestSelectTypeFragment;
import com.caltruism.assist.fragment.AddRequestSummaryFragment;
import com.caltruism.assist.util.CustomCallbackListener;
import com.caltruism.assist.util.Constants;
import com.caltruism.assist.util.CustomDateTimeUtil;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.shuhart.stepview.StepView;

import org.imperiumlabs.geofirestore.GeoLocation;
import org.imperiumlabs.geofirestore.core.GeoHash;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class AddRequestActivity extends AppCompatActivity implements CustomCallbackListener.AddRequestActivityCallbackListener {

    private static final String TAG = "AddRequestActivity";
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 0;
    private static final int REQUEST_CHECK_SETTINGS = 0x1;

    private static final String TITLE_STEP0 = "Select a type";
    private static final String TITLE_STEP1 = "Details";
    private static final String TITLE_STEP2 = "Date and Time";
    private static final String TITLE_STEP3 = "Summary";

    private static final int STEP_COUNT = 4;
    private static final String TITLE_KEY = "title";
    private static final String DESCRIPTION_KEY = "description";
    private static final String IS_CURRENT_LOCATION_KEY = "isCurrentLocation";
    private static final String LOCATION_NAME_KEY = "locationName";
    private static final String LOCATION_ADDRESS_KEY = "locationAddress";
    private static final String LOCATION_LAT_LNG_KEY = "locationLatLng";
    private static final String IS_NOW_KEY = "isNow";
    private static final String DATE_TIME_KEY = "dateTime";
    private static final String DURATION_KEY = "duration";

    private int currentStep = 0;
    private int highestStepCompleted = -1;

    private int requestType;
    private String requestTitle;
    private String requestDescription;
    private boolean requestIsCurrentLocation;
    private String requestLocationName;
    private String requestLocationAddress;
    private LatLng requestLocationLatLng;
    private boolean requestIsNow;
    private long requestDateTime;
    private int requestDuration;

    private Toolbar toolbar;
    private TextView title;
    private StepView stepView;
    private Button nextButton;
    private Button editButton;
    private Button requestButton;

    private AddRequestSelectTypeFragment selectTypeFragment;
    private AddRequestInputDetailsFragment inputDetailsFragment;
    private AddRequestInputDateTimeFragment inputDateTimeFragment;
    private AddRequestSummaryFragment summaryFragment;

    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 1000;
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    private FusedLocationProviderClient fusedLocationClient;
    private SettingsClient settingsClient;
    private LocationRequest locationRequest;
    private LocationSettingsRequest locationSettingsRequest;
    private LocationCallback locationCallback;
    private Location currentLocation;

    FirebaseFirestore db;
    FirebaseAuth auth;

    Geocoder geocoder;
    private boolean locationNameAndAddressUpdated = false;
    private boolean[] isCompleted = new boolean[3];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_request);

        selectTypeFragment = new AddRequestSelectTypeFragment();
        inputDetailsFragment = new AddRequestInputDetailsFragment();
        inputDateTimeFragment = new AddRequestInputDateTimeFragment();
        summaryFragment = new AddRequestSummaryFragment();

        nextButton = findViewById(R.id.buttonAddRequestNext);
        editButton = findViewById(R.id.buttonAddRequestEdit);
        requestButton = findViewById(R.id.buttonAddRequestRequest);

        setupToolbar();
        setupStepView();

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.fragmentCreateRequest, selectTypeFragment);
        fragmentTransaction.commit();

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (highestStepCompleted < currentStep + 1) {
                    highestStepCompleted = currentStep + 1;
                    nextButton.setEnabled(false);
                }
                updateUI(currentStep + 1);
            }
        });

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUI(0);
            }
        });

        requestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                commitData();
            }
        });

        geocoder = new Geocoder(this, Locale.getDefault());
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        settingsClient = LocationServices.getSettingsClient(this);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                Log.d(TAG, "Location updated: " + locationResult.getLastLocation().getLongitude() + " " + locationResult.getLastLocation().getLatitude());
                currentLocation = locationResult.getLastLocation();
                locationNameAndAddressUpdated = false;
            }
        };

        locationRequest = new LocationRequest().setInterval(UPDATE_INTERVAL_IN_MILLISECONDS)
                .setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        locationSettingsRequest = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest).build();

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_create_request, menu);
        return true;
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
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.actionClose) {
            if (highestStepCompleted != -1)
                showCloseAlertDialog();
            else
                super.onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        if (currentStep > 0) {
            updateUI(currentStep - 1);
        } else if (highestStepCompleted != -1) {
                showCloseAlertDialog();
        } else {
            super.onBackPressed();
            finish();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    @Override
    public void onDataChange(int step, Object data) {
        HashMap<String, Object> dataMap;
        boolean completed = false;

        switch (step) {
            case 0:
                requestType = (int) data;
                if (requestType != -1)
                    completed = true;
                else
                    completed = false;
                break;

            case 1:
                dataMap = (HashMap<String, Object>) data;
                completed = dataMap.size() >= 3;

                if (dataMap.containsKey(TITLE_KEY))
                    requestTitle = (String) dataMap.get(TITLE_KEY);

                if (dataMap.containsKey(DESCRIPTION_KEY))
                    requestDescription = (String) dataMap.get(DESCRIPTION_KEY);

                if (dataMap.containsKey(IS_CURRENT_LOCATION_KEY))
                    requestIsCurrentLocation = (boolean) dataMap.get(IS_CURRENT_LOCATION_KEY);

                if (dataMap.containsKey(LOCATION_NAME_KEY))
                    requestLocationName = (String) dataMap.get(LOCATION_NAME_KEY);

                if (dataMap.containsKey(LOCATION_ADDRESS_KEY))
                    requestLocationAddress = (String) dataMap.get(LOCATION_ADDRESS_KEY);

                if (dataMap.containsKey(LOCATION_LAT_LNG_KEY))
                    requestLocationLatLng = (LatLng) dataMap.get(LOCATION_LAT_LNG_KEY);
                break;

            case 2:
                dataMap = (HashMap<String, Object>) data;
                completed = dataMap.size() == 3;

                if (dataMap.containsKey(IS_NOW_KEY))
                    requestIsNow = (boolean) dataMap.get(IS_NOW_KEY);

                if (dataMap.containsKey(DATE_TIME_KEY))
                    requestDateTime = (long) dataMap.get(DATE_TIME_KEY);

                if (dataMap.containsKey(DURATION_KEY)) {
                    requestDuration = (int) dataMap.get(DURATION_KEY);
                    if (requestDuration == 0)
                        completed = false;
                }
                break;
        }

        nextButton.setEnabled(completed);
        isCompleted[step] = completed;
    }

    public void setupToolbar() {
        toolbar = findViewById(R.id.toolbarAddRequest);
        title = findViewById(R.id.textViewAddRequestToolbarTitle);
        setSupportActionBar(toolbar);
        title.setText(TITLE_STEP0);

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

    private void setupStepView() {
        stepView = findViewById(R.id.stepView);
        List<String> steps = Arrays.asList("Type", "Details", "Date", "Summary");
        stepView.setSteps(steps);

        stepView.setOnStepClickListener(new StepView.OnStepClickListener() {
            @Override
            public void onStepClick(int step) {
                if (step <= highestStepCompleted) {
                    if (currentStep != step)
                        updateUI(step);
                } else {
                    Toast.makeText(AddRequestActivity.this, "Please complete this step first", Toast.LENGTH_SHORT).show();
                }
            }
        });
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
                                    rae.startResolutionForResult(AddRequestActivity.this, REQUEST_CHECK_SETTINGS);
                                } catch (IntentSender.SendIntentException sie) {
                                    Log.e(TAG, "PendingIntent unable to execute request.", sie);
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                String errorMessage = "Location settings are inadequate, and cannot be fixed here. Fix in Settings.";
                                Log.e(TAG, errorMessage);
                                Toast.makeText(AddRequestActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void showCloseAlertDialog() {
        // TODO: Rephrase
        new AlertDialog.Builder(this).setTitle("Exit from creating request?")
                .setMessage("Are you sure you want to exit?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }).setNegativeButton("No", null).show();
    }

    private void updateUI(int nextStep) {
        updateCurrentStepAndStepView(nextStep);
        updateFragment();
    }

    private void updateCurrentStepAndStepView(int nextStep) {
        if (nextStep < STEP_COUNT) {
            currentStep = nextStep;
            stepView.go(currentStep, true);
        } else {
            stepView.done(true);
        }
    }

    private void updateFragment() {
        switch (currentStep) {
            case 0:
                showFragmentType();
                title.setText(TITLE_STEP0);
                break;

            case 1:
                showFragmentDetails();
                title.setText(TITLE_STEP1);
                break;

            case 2:
                showFragmentDate();
                title.setText(TITLE_STEP2);
                break;

            case 3:
                showFragmentSummary();
                title.setText(TITLE_STEP3);
                break;
        }
    }

    private void showFragmentType() {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

        if (selectTypeFragment.isAdded())
            fragmentTransaction.show(selectTypeFragment);
        else
            fragmentTransaction.add(R.id.fragmentCreateRequest, selectTypeFragment);

        if (inputDetailsFragment.isAdded())
            fragmentTransaction.hide(inputDetailsFragment);

        if (inputDateTimeFragment.isAdded())
            fragmentTransaction.hide(inputDateTimeFragment);

        if (summaryFragment.isAdded())
            fragmentTransaction.hide(summaryFragment);

        fragmentTransaction.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out).commit();
        updateButton();
    }

    private void showFragmentDetails() {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

        if (!checkPermissions()) {
            requestPermissions();
        } else {
            if (inputDetailsFragment.isAdded()) {
                inputDetailsFragment.onDataChange(requestType);
                fragmentTransaction.show(inputDetailsFragment);
            } else {
                Bundle arguments = new Bundle();
                arguments.putInt("requestType", requestType);
                inputDetailsFragment.setArguments(arguments);
                fragmentTransaction.add(R.id.fragmentCreateRequest, inputDetailsFragment);
            }

            if (selectTypeFragment.isAdded())
                fragmentTransaction.hide(selectTypeFragment);

            if (inputDateTimeFragment.isAdded())
                fragmentTransaction.hide(inputDateTimeFragment);

            if (summaryFragment.isAdded())
                fragmentTransaction.hide(summaryFragment);

            fragmentTransaction.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out).commit();
            updateButton();
        }
    }

    private void showFragmentDate() {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

        if (inputDateTimeFragment.isAdded()) {
            inputDateTimeFragment.onDataChange(requestType, requestTitle);
            fragmentTransaction.show(inputDateTimeFragment);
        } else {
            Bundle arguments = new Bundle();
            arguments.putInt("requestType", requestType);
            arguments.putString("requestTitle", requestTitle);
            inputDateTimeFragment.setArguments(arguments);
            fragmentTransaction.add(R.id.fragmentCreateRequest, inputDateTimeFragment);
        }

        if (selectTypeFragment.isAdded())
            fragmentTransaction.hide(selectTypeFragment);

        if (inputDetailsFragment.isAdded())
            fragmentTransaction.hide(inputDetailsFragment);

        if (summaryFragment.isAdded())
            fragmentTransaction.hide(summaryFragment);

        fragmentTransaction.commit();
        updateButton();
    }

    @SuppressLint("MissingPermission")
    private void showFragmentSummary() {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

        if (!checkPermissions()) {
            requestPermissions();
        } else if (requestIsCurrentLocation && !locationNameAndAddressUpdated) {
            if (currentLocation == null) {
                fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            currentLocation = location;
                            getCurrentLocationNameAndAddress();
                        } else {
                            // TODO: Rephrase
                            new AlertDialog.Builder(AddRequestActivity.this).setTitle("Could not get current location")
                                    .setMessage("Failed to get current location. Please input the address.")
                                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            updateUI(1);
                                        }
                                    }).show();
                        }
                    }
                });
            } else {
                getCurrentLocationNameAndAddress();
            }
        } else {
            if (summaryFragment.isAdded()) {
                summaryFragment.onDataChange(requestType, requestTitle, requestDescription, requestLocationName,
                        requestLocationAddress, requestLocationLatLng, requestIsNow, requestDateTime, requestDuration);
                fragmentTransaction.show(summaryFragment);
            } else {
                fragmentTransaction.add(R.id.fragmentCreateRequest, summaryFragment);
                Bundle arguments = new Bundle();
                arguments.putInt("requestType", requestType);
                arguments.putString("requestTitle", requestTitle);
                arguments.putString("requestDescription", requestDescription);
                arguments.putString("requestLocationName", requestLocationName);
                arguments.putString("requestLocationAddress", requestLocationAddress);
                arguments.putParcelable("requestLocationLatLng", requestLocationLatLng);
                arguments.putBoolean("requestIsNow", requestIsNow);
                arguments.putLong("requestDateTime", requestDateTime);
                arguments.putInt("requestDuration", requestDuration);
                summaryFragment.setArguments(arguments);
            }

            if (selectTypeFragment.isAdded())
                fragmentTransaction.hide(selectTypeFragment);

            if (inputDetailsFragment.isAdded())
                fragmentTransaction.hide(inputDetailsFragment);

            if (inputDateTimeFragment.isAdded())
                fragmentTransaction.hide(inputDateTimeFragment);

            fragmentTransaction.commit();
            updateButton();
        }

    }

    private void updateButton() {
        if (currentStep == 3) {
            nextButton.setVisibility(View.INVISIBLE);
            editButton.setVisibility(View.VISIBLE);
            requestButton.setVisibility(View.VISIBLE);
        } else {
            nextButton.setVisibility(View.VISIBLE);
            editButton.setVisibility(View.INVISIBLE);
            requestButton.setVisibility(View.INVISIBLE);

            nextButton.setEnabled(isCompleted[currentStep]);
        }
    }

    private void getCurrentLocationNameAndAddress() {

        boolean isNewLocation = true;

        if (requestLocationLatLng != null) {
            Location oldLocation = new Location(LocationManager.GPS_PROVIDER);
            oldLocation.setLatitude(requestLocationLatLng.latitude);
            oldLocation.setLongitude(requestLocationLatLng.longitude);

            isNewLocation = oldLocation.distanceTo(currentLocation) > 5;
        }

        requestLocationLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());

        if (isNewLocation) {
            try {
                List<Address> addresses = geocoder.getFromLocation(currentLocation.getLatitude(), currentLocation.getLongitude(), 1);
                if (addresses != null) {
                    Address returnedAddress = addresses.get(0);
                    StringBuilder strReturnedAddress = new StringBuilder("");

                    for (int i = 0; i <= returnedAddress.getMaxAddressLineIndex(); i++)
                        strReturnedAddress.append(returnedAddress.getAddressLine(i));

                    requestLocationAddress = strReturnedAddress.toString();
                    requestLocationLatLng = new LatLng(returnedAddress.getLatitude(), returnedAddress.getLongitude());
                } else {
                    Log.w(TAG, "No Address returned for current location.");
                    requestLocationAddress = "";
                }

                requestLocationName = "";
                locationNameAndAddressUpdated = true;
                showFragmentSummary();
            } catch (Exception e) {
                Log.e(TAG, "Failed to get address: ", e);

                requestLocationAddress = "";
                requestLocationName = "";
                locationNameAndAddressUpdated = true;
                showFragmentSummary();
            }
        } else {
            locationNameAndAddressUpdated = true;
            showFragmentSummary();
        }
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
                            ActivityCompat.requestPermissions(AddRequestActivity.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    REQUEST_PERMISSIONS_REQUEST_CODE);
                        }
                    }).show();
        } else {
            ActivityCompat.requestPermissions(AddRequestActivity.this,
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
                updateFragment();
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

    private void commitData() {
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.USER_DATA_SHARED_PREFERENCE, Context.MODE_PRIVATE);
        String name = sharedPreferences.getString("name", null);

        int startTime = CustomDateTimeUtil.getStartTime(requestDateTime);
        double latitude = requestLocationLatLng.latitude;
        double longitude = requestLocationLatLng.longitude;

        HashMap<String, Object> requestData = new HashMap<>();
        HashMap<String, Object> location = new HashMap<>();
        HashMap<String, Object> postedBy = new HashMap<>();

        location.put("latLng", new GeoPoint(latitude, longitude));
        location.put("address", requestLocationAddress);
        location.put("name", requestLocationName);

        postedBy.put("uid", auth.getCurrentUser().getUid());
        postedBy.put("name", name);

        requestData.put("status", Constants.REQUEST_STATUS_WAITING);
        requestData.put("type", requestType);
        requestData.put("title", requestTitle);
        requestData.put("description", requestDescription);
        requestData.put("isNow", requestIsNow);
        requestData.put("date", requestDateTime / DateUtils.MINUTE_IN_MILLIS);
        requestData.put("startTime", startTime);
        requestData.put("endTime", startTime + requestDuration);
        requestData.put("duration", requestDuration);
        requestData.put("location", location);
        requestData.put("postedBy", postedBy);
        requestData.put("postedOn", FieldValue.serverTimestamp());
        requestData.put("acceptedBy", new ArrayList<HashMap<String, Object>>());

        GeoHash geoHash = new GeoHash(new GeoLocation(latitude, longitude));
        requestData.put("g", geoHash.getGeoHashString());
        requestData.put("l", Arrays.asList(latitude, longitude));

        final CollectionReference collectionRef = db.collection("requests");

        collectionRef.add(requestData).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                Log.d(TAG, "DocumentSnapshot written with ID: " + documentReference.getId());
                setResult(Activity.RESULT_OK);
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "Error adding document", e);
                Snackbar snackbar = Snackbar.make(AddRequestActivity.this.findViewById(R.id.addRequestConstraintLayout), "Error adding request, please try again.", Snackbar.LENGTH_LONG);
                snackbar.show();
            }
        });
    }
}
