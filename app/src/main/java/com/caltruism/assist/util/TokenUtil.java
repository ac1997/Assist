package com.caltruism.assist.util;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.HashMap;
import java.util.Objects;

public class TokenUtil {
    private static final String TAG = "TokenUtil";

    public static void onNewToken() {
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (task.isSuccessful()) {
                            HashMap<String, Object> tokenData = new HashMap<>();
                            tokenData.put("tokenId", Objects.requireNonNull(task.getResult()).getToken());

                            FirebaseFirestore.getInstance().collection("tokens").document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).set(tokenData)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.d(TAG, "Document added/updated with token");
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.e(TAG, "Error adding document", e);
                                }
                            });
                        } else {
                            Log.w(TAG, "getInstanceId failed", task.getException());
                        }
                    }
                });
    }
}
