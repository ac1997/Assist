package com.caltruism.assist.util;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.caltruism.assist.R;
import com.caltruism.assist.activity.CurrentRequestActivity;
import com.caltruism.assist.activity.RequestDetailsActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class CustomRequestAcceptedDialog {
    private static final String TAG = "CustomRequestAcceptedDialog";

    public static void showDialog(final Activity activity, Intent intent) {
        final String requestId = intent.getStringExtra("requestId");
        String title = intent.getStringExtra("title");
        String acceptedByName = intent.getStringExtra("acceptedByName");
        String acceptedByUid = intent.getStringExtra("acceptedByUid");

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setNegativeButton("View Task", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(activity, RequestDetailsActivity.class);

                intent.putExtra("requestId", requestId);
                activity.startActivity(intent);
            }
        }).setPositiveButton("Close", null);

        final AlertDialog dialog = builder.create();
        dialog.setTitle("Request Accepted");
        dialog.setMessage(title);
        LayoutInflater inflater = activity.getLayoutInflater();
        View dialogLayoutView = inflater.inflate(R.layout.dialog_request_accepted_layout, null);
        dialog.setView(dialogLayoutView);

        TextView textView = dialogLayoutView.findViewById(R.id.textViewRequestAcceptedDialogUsername);
        textView.setText(acceptedByName);

        final ImageView imageViewProfile = dialogLayoutView.findViewById(R.id.imageViewRequestAcceptedDialogProfilePicture);

        // TODO: Set view profile button listener
        FirebaseFirestore.getInstance().collection("users").document(acceptedByUid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();

                    assert document != null;

                    if (document.exists()) {
                        Object profileImageUrl = document.get("pictureUrl");

                        if (profileImageUrl != null) {
                            String url = profileImageUrl.toString();

                            RequestOptions requestOptions = new RequestOptions().placeholder(R.drawable.ic_user_solid).centerCrop();
                            Glide.with(activity).setDefaultRequestOptions(requestOptions).load(url).into(imageViewProfile);

                            dialog.show();
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
}
