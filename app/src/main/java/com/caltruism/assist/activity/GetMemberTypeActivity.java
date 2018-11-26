package com.caltruism.assist.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.caltruism.assist.R;
import com.caltruism.assist.util.Constants;
import com.caltruism.assist.util.SharedPreferencesHelper;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Objects;

public class GetMemberTypeActivity extends AppCompatActivity {
    private static final String TAG = "GetMemberActivity";

    private static final int IMAGE_REQUEST_CODE = 1;

    private static final int ERROR0 = 0;
    private static final int ERROR1 = 1;
    private static final int ERROR2 = 2;

    private ImageView imageViewProfile;

    private FirebaseAuth auth;

    HashMap<String, Object> userData;
    String profilePictureURL;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_member_type);

        auth = FirebaseAuth.getInstance();

        Intent intent = getIntent();
        userData = (HashMap<String, Object>)intent.getSerializableExtra("userData");

        SharedPreferences sharedPreferences = getSharedPreferences(Constants.USER_DATA_SHARED_PREFERENCE, Context.MODE_PRIVATE);
        String firstName;
        if (sharedPreferences.contains("firstName"))
            firstName = sharedPreferences.getString("firstName", null);
        else
            firstName = (String) userData.get("firstName");

        imageViewProfile = findViewById(R.id.imageViewGetMemberTypeProfilePicture);

        if (userData.containsKey("pictureURL")) {
            profilePictureURL = (String) userData.get("pictureURL");

            RequestOptions requestOptions = new RequestOptions().placeholder(R.drawable.ic_profile_image_placeholder).centerCrop();
            Glide.with(this).setDefaultRequestOptions(requestOptions).load(profilePictureURL).into(imageViewProfile);
        }

        TextView name = findViewById(R.id.textViewGetMemberTypeName);
        name.setText(String.format("Hello %s!", firstName));

        TextView uploadImage = findViewById(R.id.textViewGetMemberTypeUploadImage);
        uploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/*");
                startActivityForResult(intent, IMAGE_REQUEST_CODE);
            }
        });

        Button buttonVolunteer = findViewById(R.id.buttonVolunteer);
        buttonVolunteer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (profilePictureURL == null)
                    showSnackbar(ERROR2);
                else
                    addUserData(getResources().getString(R.string.volunteer_type));
            }
        });

        Button buttonDisabled = findViewById(R.id.buttonDisabled);
        buttonDisabled.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (profilePictureURL == null)
                    showSnackbar(ERROR2);
                else
                    addUserData(getResources().getString(R.string.disabled_type));
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_REQUEST_CODE && resultCode == RESULT_OK) {
            Uri selectedProfilePicture = data.getData();
            imageViewProfile.setImageURI(selectedProfilePicture);

            final StorageReference profileImageStorageReference = FirebaseStorage.getInstance().
                    getReference().child(String.format("profilePictures/%s.jpg", auth.getCurrentUser().getUid()));

            profileImageStorageReference.putFile(selectedProfilePicture)
                    .continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()) {
                        return profileImageStorageReference.getDownloadUrl();
                    } else {
                        Log.e(TAG, "Failed to upload profile picture");
                        showSnackbar(ERROR1);
                        return null;
                    }
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        profilePictureURL = task.getResult().toString();
                        userData.put("pictureURL", profilePictureURL);
                    } else {
                        Log.e(TAG, "Failed to get profile picture url");
                        showSnackbar(ERROR1);
                    }
                }
            });

        }
    }

    private void addUserData(final String memberType) {
        if (userData == null)
            userData = new HashMap<>();

        userData.put("memberType", memberType);
        userData.put("joinedOn", FieldValue.serverTimestamp());

        FirebaseFirestore.getInstance().collection("users").document(Objects.requireNonNull(auth.getCurrentUser()).getUid()).set(userData, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
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
                    showSnackbar(ERROR0);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "Error adding document", e);
                showSnackbar(ERROR0);
            }
        });

        SharedPreferencesHelper.setPreferencesMemberType(GetMemberTypeActivity.this, memberType);
    }

    private void showSnackbar(int method) {
        String message;
        switch (method) {
            case ERROR1:
                message = "Failed to upload. Please try again later.";
                break;
            case ERROR2:
                message = "Please upload a profile picture.";
                break;
            case ERROR0:
            default:
                message = "Something went wrong. Please try again later.";
                break;
        }
        Snackbar snackbar = Snackbar.make(GetMemberTypeActivity.this.findViewById(R.id.getMemberTypeConstraintLayout), message, Snackbar.LENGTH_LONG);
        snackbar.show();
    }
}
