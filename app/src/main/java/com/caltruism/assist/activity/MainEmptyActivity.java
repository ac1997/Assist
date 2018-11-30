package com.caltruism.assist.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.caltruism.assist.R;
import com.caltruism.assist.util.SharedPreferencesHelper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

public class MainEmptyActivity extends AppCompatActivity {

    private static final String TAG = "MainEmptyActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.launch_screen);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();

        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        db.setFirestoreSettings(settings);

        if (auth.getCurrentUser() != null) {
            DocumentReference docRef = db.collection("users").document(auth.getCurrentUser().getUid());

            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
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
