package com.caltruism.assist.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.caltruism.assist.BuildConfig;
import com.caltruism.assist.R;
import com.caltruism.assist.util.SharedPreferencesHelper;
import com.caltruism.assist.util.TokenUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.HashMap;
import java.util.Objects;

public class MainEmptyActivity extends AppCompatActivity {

    private static final String TAG = "MainEmptyActivity";
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.launch_screen);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!checkPermissions())
            requestPermissions();
        else
            permissionsGranted();
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
                    ActivityCompat.requestPermissions(MainEmptyActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            REQUEST_PERMISSIONS_REQUEST_CODE);
                }
            }).show();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_PERMISSIONS_REQUEST_CODE);
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                permissionsGranted();
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

    private void permissionsGranted() {
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final FirebaseAuth auth = FirebaseAuth.getInstance();

        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        db.setFirestoreSettings(settings);

        if (auth.getCurrentUser() != null) {
            TokenUtil.onNewToken();

            db.collection("users").document(auth.getCurrentUser().getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    Intent intent;
                    Context context = MainEmptyActivity.this;

                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();

                        assert document != null;

                        if (document.exists()) {

                            SharedPreferencesHelper.setPreferences(context, document);
                            Object memberPhoneNumber = document.get("phoneNumber");
                            Object memberTypeObject = document.get("memberType");

                            if (memberPhoneNumber == null) {
                                intent = new Intent(context, GetMemberPhoneNumberActivity.class);
                                if (memberTypeObject != null)
                                    intent.putExtra("memberType", memberTypeObject.toString());
                            } else if (memberTypeObject == null) {
                                intent = new Intent(context, GetMemberTypeActivity.class);
                            } else {
                                String memberTypeString = memberTypeObject.toString();

                                if (memberTypeString.equals(getResources().getString(R.string.volunteer_type))) {
                                    intent = new Intent(context, VolunteerMainActivity.class);
                                } else if (memberTypeString.equals(getResources().getString(R.string.disabled_type))) {
                                    intent = new Intent(context, DisabledMainActivity.class);
                                } else {
                                    Log.e(TAG, "Invalid memberTypeString " + memberTypeString);
                                    intent = new Intent(context, GetMemberTypeActivity.class);
                                }
                            }
                        } else {
                            Log.d(TAG, "Users not exist.");
                            intent = new Intent(context, SignInActivity.class);
                        }
                    } else {
                        Log.e(TAG, "Get failed with ", task.getException());
                        intent = new Intent(context, SignInActivity.class);
                    }

                    startActivity(intent);
                    finish();
                }
            });
        } else {
            startActivity(new Intent(this, WelcomeActivity.class));
            finish();
        }
    }
}
