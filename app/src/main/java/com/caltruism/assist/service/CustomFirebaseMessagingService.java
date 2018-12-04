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

//    private void sendNotification(String messageBody) {
//        Intent intent = new Intent(this, MainEmptyActivity.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
//                PendingIntent.FLAG_ONE_SHOT);
//
//        String channelId = "fcm_default_channel";
//        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
//        NotificationCompat.Builder notificationBuilder =
//                new NotificationCompat.Builder(this, channelId)
//                        .setSmallIcon(R.drawable.ic_add_request_other)
//                        .setContentTitle("TEST!")
//                        .setContentText(messageBody)
//                        .setAutoCancel(true)
//                        .setSound(defaultSoundUri)
//                        .setContentIntent(pendingIntent);
//
//        NotificationManager notificationManager =
//                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//
//        // Since android Oreo notification channel is needed.
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            NotificationChannel channel = new NotificationChannel(channelId,
//                    "Channel human readable title",
//                    NotificationManager.IMPORTANCE_DEFAULT);
//            notificationManager.createNotificationChannel(channel);
//        }
//
//        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
//    }
}
