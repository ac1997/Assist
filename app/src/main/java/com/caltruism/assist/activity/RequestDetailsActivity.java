package com.caltruism.assist.activity;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.caltruism.assist.R;
import com.caltruism.assist.data.AssistRequest;
import com.caltruism.assist.util.BitMapDescriptorFromVector;
import com.caltruism.assist.util.Constants;
import com.caltruism.assist.util.CustomDateTimeUtil;
import com.caltruism.assist.util.CustomRequestAcceptedDialog;
import com.caltruism.assist.util.SharedPreferencesHelper;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Objects;

public class RequestDetailsActivity extends AppCompatActivity implements OnMapReadyCallback {

    public static final String TAG = "RequestDetailsActivity";

    private Toolbar toolbar;
    private TextView title;

    private TextView textViewTitle;
    private TextView textViewUsername;
    private TextView textViewDate;
    private TextView textViewType;
    private TextView textViewTime;
    private TextView textViewDuration;
    private TextView textViewLocationName;
    private TextView textViewLocationAddress;
    private TextView textViewDescription;
    private ImageView imageViewProfile;
    private ImageView imageViewType;
    private MapView mapView;
    private Button buttonViewProfile;
    private Button buttonActionButton;

    private FirebaseFirestore db;
    private GoogleMap map;

    private boolean isVolunteerView;
    private AssistRequest assistRequest;
    private boolean isQueryData;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_details);

        db = FirebaseFirestore.getInstance();

        if (Objects.requireNonNull(getIntent().getExtras()).containsKey("requestData")) {
            isVolunteerView = getIntent().getExtras().getBoolean("isVolunteerView");
            assistRequest = getIntent().getExtras().getParcelable("requestData");
            isQueryData = false;
        } else {
            isVolunteerView = false;
            queryRequestData(getIntent().getExtras().getString("requestId"));
            isQueryData = true;
        }

        mapView = findViewById(R.id.mapViewRequestDetails);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        textViewTitle = findViewById(R.id.textViewRequestDetailsTitle);
        textViewUsername = findViewById(R.id.textViewRequestDetailsUsername);
        textViewDate = findViewById(R.id.textViewRequestDetailsDate);
        textViewType = findViewById(R.id.textViewRequestDetailsType);
        textViewTime = findViewById(R.id.textViewRequestDetailsTime);
        textViewDuration = findViewById(R.id.textViewRequestDetailsDuration);
        textViewLocationName = findViewById(R.id.textViewRequestDetailsLocationName);
        textViewLocationAddress = findViewById(R.id.textViewRequestDetailsLocationAddress);
        textViewDescription = findViewById(R.id.textViewRequestDetailsDescription);
        imageViewProfile = findViewById(R.id.imageViewRequestDetailsProfilePicture);
        imageViewType = findViewById(R.id.imageViewRequestDetailsType);
        mapView = findViewById(R.id.mapViewRequestDetails);
        buttonViewProfile = findViewById(R.id.buttonRequestDetailsViewProfile);
        buttonActionButton = findViewById(R.id.buttonRequestDetailsActionButton);

        setupToolbar();

        if (!isQueryData)
            setUpViews();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style_json));
        googleMap.getUiSettings().setMyLocationButtonEnabled(false);
        googleMap.getUiSettings().setMapToolbarEnabled(false);
        googleMap.getUiSettings().setAllGesturesEnabled(false);
        googleMap.setMinZoomPreference(Constants.MIN_ZOOM);
        googleMap.setMaxZoomPreference(Constants.MAX_ZOOM);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
        }

        if (!isQueryData) {
            googleMap.addMarker(new MarkerOptions().position(assistRequest.getLocationLatLng())
                    .icon(BitMapDescriptorFromVector.regularMarker(this)));
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(assistRequest.getLocationLatLng(), Constants.DEFAULT_ZOOM));
        }

        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                Intent intent = new Intent(RequestDetailsActivity.this, MapViewActivity.class);
                intent.putExtra("locationLatLng", assistRequest.getLocationLatLng());
                intent.putExtra("locationName", assistRequest.getLocationName());
                intent.putExtra("locationAddress", assistRequest.getLocationAddress());
                startActivity(intent);
            }
        });
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

    @Override
    public void onBackPressed() {
        if (!isTaskRoot()) {
            super.onBackPressed();
            finish();
        } else {
            if (isVolunteerView) {
                startActivity(new Intent(this, VolunteerMainActivity.class));
                finish();
            } else {
                startActivity(new Intent(this, DisabledMainActivity.class));
                finish();
            }
        }
    }

    private void queryRequestData(final String requestId) {
        db.collection("requests").document(requestId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                Context context = RequestDetailsActivity.this;

                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    assert document != null;
                    if (document.exists()) {
                        assistRequest = new AssistRequest(document);

                        if (CustomDateTimeUtil.isCurrent(assistRequest.getDateTime(), Constants.MINUTES_TO_CURRENT_REQUEST_VIEW)) {
                            Intent intent = new Intent(RequestDetailsActivity.this, CurrentRequestActivity.class);
                            intent.putExtra("isVolunteerView", false);
                            intent.putExtra("requestData", assistRequest);
                            startActivity(intent);
                            finish();
                        } else {
                            setUpViews();

                            if (map != null) {
                                map.addMarker(new MarkerOptions().position(assistRequest.getLocationLatLng())
                                        .icon(BitMapDescriptorFromVector.regularMarker(RequestDetailsActivity.this)));
                                map.moveCamera(CameraUpdateFactory.newLatLngZoom(assistRequest.getLocationLatLng(), Constants.DEFAULT_ZOOM));
                            } else {
                                isQueryData = false;
                            }
                        }
                    } else {
                        Log.e(TAG, "Requests not exist with id: " + requestId);
                        startActivity(new Intent(context, DisabledMainActivity.class));
                        finish();
                    }
                } else {
                    Log.e(TAG, "Get failed with ", task.getException());
                    startActivity(new Intent(context, DisabledMainActivity.class));
                    finish();
                }
            }
        });
    }

    public void setupToolbar() {
        toolbar = findViewById(R.id.toolbarRequestDetails);
        title = findViewById(R.id.textViewRequestDetailsToolbarTitle);
        setSupportActionBar(toolbar);

        if (isVolunteerView)
            title.setText("Task Details");
        else
            title.setText("Request Details");

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

    private void setUpViews() {
        textViewTitle.setText(assistRequest.getTitle());
        textViewLocationAddress.setText(assistRequest.getLocationAddress());
        textViewDescription.setText(assistRequest.getDescription());
        textViewDuration.setText(assistRequest.getDurationString());
        textViewDate.setText(CustomDateTimeUtil.getDateWithDay(assistRequest.getDateTime()));

        if (assistRequest.isNow() && !CustomDateTimeUtil.isExpired(assistRequest.getDateTime(), assistRequest.getDuration())) {
            textViewTime.setText(Html.fromHtml("<font color='#f48760'>Now</font>", Html.FROM_HTML_MODE_LEGACY));
        } else {
            textViewTime.setText(assistRequest.getTimeString());
        }

        if (assistRequest.getLocationName().length() > 0) {
            if (textViewLocationName.getVisibility() == View.GONE)
                textViewLocationName.setVisibility(View.VISIBLE);
            textViewLocationName.setText(assistRequest.getLocationName());
        } else if (textViewLocationName.getVisibility() == View.VISIBLE) {
            textViewLocationName.setVisibility(View.GONE);
        }

        switch (assistRequest.getType()) {
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

        if (isVolunteerView) {
            setUpProfileImageView(assistRequest.getPostedByUid());
            textViewUsername.setText(assistRequest.getPostedByName());

            // TODO: Set view profile button listener
            if (assistRequest.getStatus() == Constants.REQUEST_STATUS_ACCEPTED) {
                buttonActionButton.setText("Cancel Request");
                buttonActionButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // TODO: Rephrase
                        new AlertDialog.Builder(RequestDetailsActivity.this).setTitle("Cancel request?")
                                .setMessage("Are you sure you want to cancel this request?")
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        cancelAcceptedRequest();
                                    }
                                }).setNegativeButton("No", null).show();
                    }
                });
            } else if (assistRequest.getStatus() == Constants.REQUEST_STATUS_WAITING) {
                buttonActionButton.setText("Accept Request");
                buttonActionButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // TODO: Rephrase
                        new AlertDialog.Builder(RequestDetailsActivity.this).setTitle("Accept request?")
                                .setMessage("The scheduled meeting time is " + assistRequest.getDateTimeString())
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        acceptRequest();
                                    }
                                }).setNegativeButton("No", null).show();
                    }
                });
            } else {
                buttonActionButton.setVisibility(View.GONE);
            }
        } else {
            if (assistRequest.getStatus() == Constants.REQUEST_STATUS_ACCEPTED) {
                setUpProfileImageView(assistRequest.getAcceptedByMainUid());
                textViewUsername.setText(assistRequest.getAcceptedByMainName());
                // TODO: Set view profile button listener
            } else {
                textViewUsername.setText(Html.fromHtml("<font color='#f48760'>Waiting on volunteer</font>", Html.FROM_HTML_MODE_LEGACY));
                buttonViewProfile.setVisibility(View.GONE);
            }

            if (assistRequest.getStatus() == Constants.REQUEST_STATUS_WAITING || assistRequest.getStatus() == Constants.REQUEST_STATUS_ACCEPTED) {
                buttonActionButton.setText("Cancel Request");
                buttonActionButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // TODO: Rephrase
                        new AlertDialog.Builder(RequestDetailsActivity.this).setTitle("Cancel request?")
                                .setMessage("Are you sure you want to cancel this request? This action cannot be undone.")
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        cancelRequest();
                                    }
                                }).setNegativeButton("No", null).show();
                    }
                });
            } else {
                buttonActionButton.setVisibility(View.GONE);
            }
        }
    }

    private void acceptRequest() {
        HashMap<String, Object> acceptedBy = new HashMap<>();
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.USER_DATA_SHARED_PREFERENCE, Context.MODE_PRIVATE);
        String acceptedByName = sharedPreferences.getString("name", null);
        String acceptedByUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        acceptedBy.put("name", acceptedByName);
        acceptedBy.put("uid", acceptedByUid);
        assistRequest.addAcceptedBy(acceptedByUid, acceptedByName);

        db.collection("requests").document(assistRequest.getId()).update(
                "status", Constants.REQUEST_STATUS_ACCEPTED,
                "acceptedBy", FieldValue.arrayUnion(acceptedBy)
        ).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d(TAG, "DocumentSnapshot updated with ID: " + assistRequest.getId());
                    Intent intent = new Intent(RequestDetailsActivity.this, CurrentRequestActivity.class);
                    intent.putExtra("isVolunteerView", isVolunteerView);
                    intent.putExtra("requestData", assistRequest);
                    startActivity(intent);
                    finish();
                }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error updating document", e);
                        Snackbar snackbar = Snackbar.make(RequestDetailsActivity.this.findViewById(R.id.requestDetailsConstraintLayout), "Error accepting request, please try again.", Snackbar.LENGTH_SHORT);
                        snackbar.show();
                    }
                });
    }

    private void cancelRequest() {
        db.collection("requests").document(assistRequest.getId()).update("status", Constants.REQUEST_STATUS_CANCELED)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot updated with ID: " + assistRequest.getId());
                        setResult(Activity.RESULT_FIRST_USER);
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error updating document", e);
                        Snackbar snackbar = Snackbar.make(RequestDetailsActivity.this.findViewById(R.id.requestDetailsConstraintLayout), "Error cancelling request, please try again.", Snackbar.LENGTH_SHORT);
                        snackbar.show();
                    }
                });
    }

    private void cancelAcceptedRequest() {
        HashMap<String, Object> acceptedBy = new HashMap<>();
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.USER_DATA_SHARED_PREFERENCE, Context.MODE_PRIVATE);

        acceptedBy.put("name", sharedPreferences.getString("name", null));
        acceptedBy.put("uid", FirebaseAuth.getInstance().getCurrentUser().getUid());

        db.collection("requests").document(assistRequest.getId()).update(
                "status", Constants.REQUEST_STATUS_WAITING,
                "acceptedBy", FieldValue.arrayRemove(acceptedBy))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot updated with ID: " + assistRequest.getId());
                        setResult(Activity.RESULT_FIRST_USER);
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(TAG, "Error updating document", e);
                Snackbar snackbar = Snackbar.make(RequestDetailsActivity.this.findViewById(R.id.requestDetailsConstraintLayout), "Error cancelling request, please try again.", Snackbar.LENGTH_SHORT);
                snackbar.show();
            }
        });
    }

    private void setUpProfileImageView(String uid) {
        db.collection("users").document(uid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();

                    assert document != null;

                    if (document.exists()) {
                        Context context = RequestDetailsActivity.this;
                        Object profileImageUrl = document.get("pictureUrl");

                        if (profileImageUrl != null) {
                            String url = profileImageUrl.toString();

                            RequestOptions requestOptions = new RequestOptions().placeholder(R.drawable.ic_user_solid).centerCrop();
                            Glide.with(context).setDefaultRequestOptions(requestOptions).load(url).into(imageViewProfile);
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

    private BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, final Intent intent) {
            CustomRequestAcceptedDialog.showDialog(RequestDetailsActivity.this, intent);
        }
    };
}
