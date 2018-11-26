package com.caltruism.assist.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.caltruism.assist.R;
import com.caltruism.assist.util.Constants;
import com.caltruism.assist.util.SharedPreferencesHelper;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Objects;

public class GetMemberTypeActivity extends AppCompatActivity {
    private static final String TAG = "GetMemberActivity";
    private static final String ERROR = "Something went wrong. Please try again.";

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

        SharedPreferences sharedPreferences = getSharedPreferences(Constants.USER_DATA_SHARED_PREFERENCE, Context.MODE_PRIVATE);
        String firstName = "";

        if (userData == null && sharedPreferences.contains("firstName"))
            firstName = sharedPreferences.getString("firstName", null);
        else if (userData != null)
            firstName = (String) userData.get("firstName");

        TextView name = findViewById(R.id.textViewGetMemberTypeName);
        name.setText(String.format("Hello %s!", firstName));

        Button buttonVolunteer = findViewById(R.id.buttonVolunteer);
        Button buttonDisabled = findViewById(R.id.buttonDisabled);

        buttonVolunteer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addUserData(getResources().getString(R.string.volunteer_type));
            }
        });

        buttonDisabled.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addUserData(getResources().getString(R.string.disabled_type));
            }
        });
    }



    private void addUserData(final String memberType) {
        if (userData == null)
            userData = new HashMap<>();

        userData.put("memberType", memberType);
        userData.put("joinedOn", FieldValue.serverTimestamp());

        db.collection("users").document(Objects.requireNonNull(auth.getCurrentUser()).getUid()).set(userData, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "Document added/updated with ID: " + auth.getCurrentUser().getUid());

                Context mainContext = GetMemberTypeActivity.this;

                if (memberType.equals(getResources().getString(R.string.volunteer_type))) {
                    startActivity(new Intent(mainContext, VolunteerMainActivity.class));
                    finish();
                } else if (memberType.equals(getResources().getString(R.string.disabled_type))) {
                    startActivity(new Intent(mainContext, DisabledMainActivity.class));
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

        SharedPreferencesHelper.setPreferencesMemberType(GetMemberTypeActivity.this, memberType);
    }
}
