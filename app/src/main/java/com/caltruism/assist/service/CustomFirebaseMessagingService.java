package com.caltruism.assist.service;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CustomFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "MyFirebaseMsgService";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (remoteMessage.getData().size() > 0) {
            sendMessage(remoteMessage.getData());
        }
    }

    @Override
    public void onNewToken(String token) {
        storeTokenToServer(token);
    }

    private void sendMessage(Map<String, String> data) {
        Log.d("sender", "Broadcasting message");
        Intent intent = new Intent("disabled-request-accepted");
        intent.putExtra("requestId", data.get("requestId"));
        intent.putExtra("title", data.get("title"));
        intent.putExtra("acceptedByName", data.get("acceptedByName"));
        intent.putExtra("acceptedByUid", data.get("acceptedByUid"));
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void storeTokenToServer(final String token) {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            HashMap<String, Object> tokenData = new HashMap<>();
            tokenData.put("tokenId", token);

            FirebaseFirestore.getInstance().collection("tokens").document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).set(tokenData)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "Document added/updated with token: " + token);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, "Error adding document", e);
                }
            });
        }
    }
}
