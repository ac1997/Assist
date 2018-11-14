package com.caltruism.assist;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class MainEmptyActivity extends AppCompatActivity {

    private final String TAG = "MainEmptyActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Context current = this;
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null) {
            DocumentReference docRef = db.collection("users").document(mAuth.getCurrentUser().getUid());

            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        assert document != null;

                        if (document.exists()) {
                            Log.i(TAG, "DocumentSnapshot data: " + document.getData());
                            String lastLoggedInType = Objects.requireNonNull(document.get("memberType")).toString();
                            if (lastLoggedInType.equals("volunteer"))
                                startActivity(new Intent(current, RequestListVolunteerActivity.class));
                            else if (lastLoggedInType.equals("disabled"))
                                startActivity(new Intent(current, RequestListDisabledActivity.class));
                        } else {
                            Log.e(TAG, "Users not exist.");
                        }
                    } else {
                        Log.e(TAG, "Get failed with ", task.getException());
                    }
                }
            });
        } else {
            startActivity(new Intent(current, LoginActivity.class));
        }

        finish();
    }
}
