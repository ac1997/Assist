package com.caltruism.assist.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.caltruism.assist.R;
import com.caltruism.assist.utils.Constants;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;

import static java.lang.Float.NaN;


public class LoginActivity extends AppCompatActivity {

    private static final String TAG ="LoginActivity";
    private static final int RC_SIGN_IN = 9001;

    private static final String EMAIL = "Email";
    private static final String FACEBOOK = "Facebook";
    private static final String GOOGLE = "Google";
    private static final String EMAIL_LOGIN_ERROR = "Login failed. Please try agian.";
    private static final String FACEBOOK_LOGIN_ERROR = "Facebook login failed. Please try again.";
    private static final String GOOGLE_LOGIN_ERROR = "Google login failed. Please try again.";

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private GoogleSignInClient GoogleSignInClient;
    private CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        callbackManager = CallbackManager.Factory.create();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        GoogleSignInClient = GoogleSignIn.getClient(this, gso);

        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "Facebook login canceled");
            }

            @Override
            public void onError(FacebookException exception) {
                Log.e(TAG, "Facebook login error", exception);
            }
        });

        Button loginButtonEmail = findViewById(R.id.buttonLoginEmail);
        loginButtonEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText editTextEmail = findViewById(R.id.editTextEmail);
                EditText editTextPassword = findViewById(R.id.editTextPassword);
                String email = editTextEmail.getText().toString();
                String password = editTextPassword.getText().toString();

                // TODO: Verify format

                handleEmailPasswordLogin(email, password);
            }
        });

        Button loginButtonFacebook = findViewById(R.id.buttonLoginFacebook);
        loginButtonFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoginManager.getInstance().logInWithReadPermissions(LoginActivity.this, Arrays.asList("email", "public_profile", "user_gender", "user_age_range"));
            }
        });

        Button loginButtonGoogle = findViewById(R.id.buttonLoginGoogle);
        loginButtonGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = GoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                assert account != null;
                handleGoogleLogin(account);
            } catch (ApiException e) {
                Log.e(TAG, "Google sign in failed", e);
                Snackbar snackbar = Snackbar.make(LoginActivity.this.findViewById(R.id.loginConstraintLayout), GOOGLE_LOGIN_ERROR, Snackbar.LENGTH_LONG);
                snackbar.show();
            }
        } else {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void handleEmailPasswordLogin(String email, String password) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "Email login success");
                    checkUserInDatabase(EMAIL, null, null);
                } else {
                    Log.e(TAG, "Email authentication failed", task.getException());
                    Snackbar snackbar = Snackbar.make(LoginActivity.this.findViewById(R.id.loginConstraintLayout), EMAIL_LOGIN_ERROR, Snackbar.LENGTH_LONG);
                    snackbar.show();
                }
            }
        });
    }

    private void handleFacebookAccessToken(final AccessToken token) {
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        auth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "Facebook login success");
                    checkUserInDatabase(FACEBOOK, token, null);
                } else {
                    Log.e(TAG, "Facebook authentication failed", task.getException());
                    Snackbar snackbar = Snackbar.make(LoginActivity.this.findViewById(R.id.loginConstraintLayout), FACEBOOK_LOGIN_ERROR, Snackbar.LENGTH_LONG);
                    snackbar.show();
                }
            }
        });
    }

    private void handleGoogleLogin(final GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        auth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "Google login success");
                    checkUserInDatabase(GOOGLE, null, acct);
                } else {
                    Log.e(TAG, "Google authentication failed", task.getException());
                    Snackbar snackbar = Snackbar.make(LoginActivity.this.findViewById(R.id.loginConstraintLayout), GOOGLE_LOGIN_ERROR, Snackbar.LENGTH_LONG);
                    snackbar.show();
                }
            }
        });
    }

    private void checkUserInDatabase(final String method, final AccessToken token, final GoogleSignInAccount acct)
    {
        DocumentReference docRef = db.collection("users").document(auth.getCurrentUser().getUid());

        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();

                    assert document != null;

                    if (document.exists()) {
                        Intent activityIntent;
                        Context mainContext = LoginActivity.this;

                        SharedPreferences sharedPreferences = getSharedPreferences(Constants.USER_DATA_SHARED_PREFERENCE, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();

                        Object memberNameObject = document.get("name");
                        Object memberPictureURLObject = document.get("pictureURL");
                        Object memberRatingsObject = document.get("ratings");
                        Object memberTypeObject = document.get("memberType");

                        if (memberNameObject != null)
                            editor.putString("name", memberNameObject.toString());

                        if (memberPictureURLObject != null)
                            editor.putString("pictureURL", memberPictureURLObject.toString());

                        if (memberRatingsObject != null)
                            editor.putFloat("ratings", Float.parseFloat(memberRatingsObject.toString()));

                        // TODO: Get joinedOn timestamp

                        if (memberTypeObject == null) {
                            Log.d(TAG, document.getData().toString());
                            activityIntent = new Intent(mainContext, GetMemberTypeActivity.class);
                        } else {
                            String memberTypeString = memberTypeObject.toString();

                            if (memberTypeString.equals(getResources().getString(R.string.volunteer_type)))
                                activityIntent = new Intent(mainContext, RequestListVolunteerActivity.class);
                            else if (memberTypeString.equals(getResources().getString(R.string.disabled_type)))
                                activityIntent = new Intent(mainContext, RequestListDisabledActivity.class);
                            else
                                activityIntent = new Intent(mainContext, GetMemberTypeActivity.class);

                            editor.putString("memberType", memberTypeString);
                        }
                        editor.apply();
                        startActivity(activityIntent);
                        finish();
                    } else {
                        if (method.equals(EMAIL)) {
                            Log.e(TAG, "Email sign in with user data not exist in database");
                            Snackbar snackbar = Snackbar.make(LoginActivity.this.findViewById(R.id.loginConstraintLayout), "Please contact use.", Snackbar.LENGTH_LONG);
                            snackbar.show();
                        }
                        else if (method.equals(FACEBOOK))
                            setFacebookData(token);
                        else if (method.equals(GOOGLE))
                            setGoogleData(acct);
                    }
                } else {
                    Log.e(TAG, "Get failed with ", task.getException());
                    Snackbar snackbar = Snackbar.make(LoginActivity.this.findViewById(R.id.loginConstraintLayout), FACEBOOK_LOGIN_ERROR, Snackbar.LENGTH_LONG);
                    snackbar.show();
                }
            }
        });
    }

    private void setFacebookData(final AccessToken token)
    {
        GraphRequest request = GraphRequest.newMeRequest(token, new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {
                try {
                    String email = response.getJSONObject().getString("email");
                    String firstName = response.getJSONObject().getString("first_name");
                    String lastName = response.getJSONObject().getString("last_name");
                    String gender = response.getJSONObject().getString("gender");
                    String ageRange = response.getJSONObject().getString("age_range");
                    String pictureURL = Profile.getCurrentProfile().getProfilePictureUri(200, 200).toString();

                    initUser(email, firstName, lastName, pictureURL, gender, ageRange);
                } catch (JSONException e) {
                    Log.e(TAG, "Facebook sign in failed", e);
                    Snackbar snackbar = Snackbar.make(LoginActivity.this.findViewById(R.id.loginConstraintLayout), FACEBOOK_LOGIN_ERROR, Snackbar.LENGTH_LONG);
                    snackbar.show();
                }
            }
        });

        Bundle parameters = new Bundle();
        parameters.putString("fields", "id, email, first_name, last_name, gender, age_range");
        request.setParameters(parameters);
        request.executeAsync();
    }

    private void setGoogleData(GoogleSignInAccount acct) {
        String email = acct.getEmail();
        String firstName = acct.getGivenName();
        String lastName = acct.getFamilyName();
        String pictureURL = acct.getPhotoUrl().toString();

        initUser(email, firstName, lastName, pictureURL, null, null);
    }

    private void initUser(String email, String firstName, String lastName, String pictureURL, String gender, String ageRange) {
        HashMap<String, Object> userData = new HashMap<>();
        userData.put("email", email);
        userData.put("firstName", firstName);
        userData.put("lastName", lastName);
        userData.put("name", firstName + " " + lastName);
        userData.put("pictureURL", pictureURL);

        if (gender != null)
            userData.put("gender", gender);
        if (ageRange != null)
            userData.put("ageRange", ageRange);

        userData.put("ratings", 0.0);

        HashMap<String, Object> stats = new HashMap<>();
        stats.put("servicedTime", 0);
        stats.put("requestCompleted", 0);

        userData.put("stats", stats);

        Intent intent = new Intent(this, GetMemberTypeActivity.class);
        intent.putExtra("userData", userData);
        startActivity(intent);
        finish();
    }
}
