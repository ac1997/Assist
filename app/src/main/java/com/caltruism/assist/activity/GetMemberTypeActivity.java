package com.caltruism.assist.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.caltruism.assist.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Objects;

public class GetMemberTypeActivity extends AppCompatActivity {
    private final String TAG = "GetMemberActivity";
    private final String ERROR = "Something went wrong. Please try again.";

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    HashMap<String, Object> userData;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_member_type);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        Intent intent = getIntent();
        userData = (HashMap<String, Object>)intent.getSerializableExtra("userData");

        Button buttonVolunteer = findViewById(R.id.buttonVolunteer);
        Button buttonDisabled = findViewById(R.id.buttonDisabled);

        buttonVolunteer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setUserData(getResources().getString(R.string.volunteer_type));
            }
        });

        buttonDisabled.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setUserData(getResources().getString(R.string.disabled_type));
            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();

        Log.d(TAG, "Get member activity started");
    }

    private void setUserData(final String memberType) {
        if (userData == null)
            userData = new HashMap<>();

        userData.put("memberType", memberType);

        db.collection("users").document(Objects.requireNonNull(auth.getCurrentUser()).getUid()).set(userData, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "Document added/updated with ID: " + auth.getCurrentUser().getUid());

                Context mainContext = GetMemberTypeActivity.this;

                if (memberType.equals(getResources().getString(R.string.volunteer_type))) {
                    startActivity(new Intent(mainContext, RequestListVolunteerActivity.class));
                    finish();
                } else if (memberType.equals(getResources().getString(R.string.disabled_type))) {
                    startActivity(new Intent(mainContext, RequestListDisabledActivity.class));
                    finish();
                } else {
                    Log.e(TAG, "Invalid member type: " + memberType);
                    Snackbar snackbar = Snackbar.make(GetMemberTypeActivity.this.findViewById(R.id.getMemberTypeConstraintLayout), ERROR, Snackbar.LENGTH_LONG);
                    snackbar.show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "Error adding document", e);
                Snackbar snackbar = Snackbar.make(GetMemberTypeActivity.this.findViewById(R.id.getMemberTypeConstraintLayout), ERROR, Snackbar.LENGTH_LONG);
                snackbar.show();
            }
        });
    }
}
