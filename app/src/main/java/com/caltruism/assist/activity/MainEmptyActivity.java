package com.caltruism.assist.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.caltruism.assist.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainEmptyActivity extends AppCompatActivity {

    private static final String TAG = "MainEmptyActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() != null) {
            DocumentReference docRef = db.collection("users").document(auth.getCurrentUser().getUid());

            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    Intent activityIntent;
                    Context mainContext = MainEmptyActivity.this;

                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();

                        assert document != null;

                        if (document.exists()) {
                            Object memberTypeObject = document.get("memberType");
                            if (memberTypeObject == null) {
                                Log.d(TAG, document.getData().toString());
                                activityIntent = new Intent(mainContext, GetMemberTypeActivity.class);
                            } else {
                                String memberTypeString = memberTypeObject.toString();

                                if (memberTypeString.equals(getResources().getString(R.string.volunteer_type)))
                                    activityIntent = new Intent(mainContext, VolunteerMainActivity.class);
                                else if (memberTypeString.equals(getResources().getString(R.string.disabled_type)))
                                    activityIntent = new Intent(mainContext, DisabledMainActivity.class);
                                else
                                    activityIntent = new Intent(mainContext, GetMemberTypeActivity.class);
                            }
                        } else {
                            Log.d(TAG, "Users not exist.");
                            activityIntent = new Intent(mainContext, LoginActivity.class);
                        }
                    } else {
                        Log.e(TAG, "Get failed with ", task.getException());
                        activityIntent = new Intent(mainContext, LoginActivity.class);
                    }

                    startActivity(activityIntent);
                    finish();
                }
            });
        } else {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }
}
