package com.caltruism.assist.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.bumptech.glide.signature.ObjectKey;
import com.caltruism.assist.R;
import com.caltruism.assist.util.CustomLoadingDialog;
import com.caltruism.assist.util.SharedPreferencesHelper;
import com.caltruism.assist.util.TokenUtil;
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
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;


public class SignInActivity extends AppCompatActivity {

    private static final String TAG ="SignInActivity";
    private static final int RC_SIGN_IN = 9001;

    private static final int EMAIL = 0;
    private static final int FACEBOOK = 1;
    private static final int GOOGLE = 2;
    private static final int OTHER = 3;

    private EditText editTextEmail;
    private EditText editTextPassword;

    private FirebaseAuth auth;
    private GoogleSignInClient GoogleSignInClient;
    private CallbackManager callbackManager;

    private CustomLoadingDialog customLoadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);

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
                customLoadingDialog.hideDialog();
                Log.d(TAG, "Facebook sign in canceled");
            }

            @Override
            public void onError(FacebookException exception) {
                customLoadingDialog.hideDialog();
                Log.e(TAG, "Facebook sign in error", exception);
                showSnackbar(FACEBOOK);
            }
        });

        Button signInButtonEmail = findViewById(R.id.buttonSignInEmail);
        signInButtonEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = editTextEmail.getText().toString();
                String password = editTextPassword.getText().toString();

                // TODO: Rephrase and reinforce password validation
                if (email.length() == 0) {
                    showSnackbar("Email cannot be empty.");
                    return;
                } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    showSnackbar("Invalid email address.");
                    return;
                }

                if (password.length() == 0) {
                    showSnackbar("Password cannot be empty.");
                    return;
                } else if (password.length() < 6) {
                    showSnackbar("Password needs to be at least 6 characters.");
                    return;
                }

                customLoadingDialog.showDialog();
                handleEmailPasswordSignIn(email, password);
            }
        });

        Button signInButtonFacebook = findViewById(R.id.buttonSignInFacebook);
        signInButtonFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoginManager.getInstance().logInWithReadPermissions(SignInActivity.this, Arrays.asList("email", "public_profile"));
            }
        });

        Button signInButtonGoogle = findViewById(R.id.buttonSignInGoogle);
        signInButtonGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = GoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });

        TextView signUp = findViewById(R.id.textViewSignInActivitySignUp);
        signUp.setText(Html.fromHtml("Don't have an account?&nbsp;&nbsp;<font color='#ffb88e'>Sign Up</font>", Html.FROM_HTML_MODE_LEGACY));
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignInActivity.this, SignUpActivity.class));
                finish();
            }
        });

        customLoadingDialog = new CustomLoadingDialog(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        customLoadingDialog.hideDialog();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        customLoadingDialog.showDialog();

        if (requestCode == RC_SIGN_IN) {

            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                assert account != null;
                handleGoogleSignIn(account);
            } catch (ApiException e) {
                Log.e(TAG, "Google sign in failed", e);
                customLoadingDialog.hideDialog();
                showSnackbar(GOOGLE);
            }
        } else {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void handleEmailPasswordSignIn(String email, String password) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    checkUserInDatabase(EMAIL, null, null);
                } else {
                    Log.e(TAG, "Email authentication failed", task.getException());
                    customLoadingDialog.hideDialog();
                    showSnackbar(EMAIL);
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
                    checkUserInDatabase(FACEBOOK, token, null);
                } else {
                    Log.e(TAG, "Facebook authentication failed", task.getException());
                    customLoadingDialog.hideDialog();
                    showSnackbar(FACEBOOK);
                }
            }
        });
    }

    private void handleGoogleSignIn(final GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        auth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    checkUserInDatabase(GOOGLE, null, acct);
                } else {
                    Log.e(TAG, "Google authentication failed", task.getException());
                    customLoadingDialog.hideDialog();
                    showSnackbar(GOOGLE);
                }
            }
        });
    }

    private void checkUserInDatabase(final int method, final AccessToken token, final GoogleSignInAccount acct)
    {
        FirebaseFirestore.getInstance().collection("users").document(auth.getCurrentUser().getUid())
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    TokenUtil.onNewToken();
                    DocumentSnapshot document = task.getResult();

                    assert document != null;

                    if (document.exists()) {
                        Intent intent;
                        Context context = SignInActivity.this;

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
                        startActivity(intent);
                        finish();
                    } else {
                        Log.e(TAG, "DOCUMENT NOT EXIST");
                        switch (method) {
                            case EMAIL:
                                customLoadingDialog.hideDialog();
                                Log.e(TAG, "Email sign in with user data not exist in database");
                                showSnackbar(OTHER);
                                break;
                            case FACEBOOK:
                                setFacebookData(token);
                                break;
                            case GOOGLE:
                                setGoogleData(acct);
                                break;
                        }
                    }
                } else {
                    Log.e(TAG, "Get failed with ", task.getException());
                    customLoadingDialog.hideDialog();
                    showSnackbar(EMAIL);
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
                    String pictureUrl = Profile.getCurrentProfile().getProfilePictureUri(200, 200).toString();

                    initUser(email, firstName, lastName, pictureUrl);
                } catch (JSONException e) {
                    customLoadingDialog.hideDialog();
                    Log.e(TAG, "Facebook sign in failed", e);
                    showSnackbar(FACEBOOK);
                }
            }
        });

        Bundle parameters = new Bundle();
        parameters.putString("fields", "id, email, first_name, last_name");
        request.setParameters(parameters);
        request.executeAsync();
    }

    private void setGoogleData(GoogleSignInAccount acct) {
        String email = acct.getEmail();
        String firstName = acct.getGivenName();
        String lastName = acct.getFamilyName();
        String pictureUrl = acct.getPhotoUrl().toString();

        initUser(email, firstName, lastName, pictureUrl);
    }

    private void initUser(String email, String firstName, String lastName, String pictureUrl) {
        TokenUtil.onNewToken();

        HashMap<String, Object> userData = new HashMap<>();
        userData.put("email", email);
        userData.put("firstName", firstName);
        userData.put("lastName", lastName);
        userData.put("pictureUrl", pictureUrl);

        userData.put("ratings", 0.0);

        HashMap<String, Object> stats = new HashMap<>();
        stats.put("servicedTime", 0);
        stats.put("requestCompleted", 0);

        userData.put("stats", stats);

        SharedPreferencesHelper.setPreferences(this, userData);

        Intent intent = new Intent(this, GetMemberPhoneNumberActivity.class);
        intent.putExtra("userData", userData);
        startActivity(intent);
        finish();
    }

    private void showSnackbar(int method) {
        String message = "";
        switch (method) {
            case EMAIL:
                message = "Sign in failed. Please try again later.";
                break;
            case FACEBOOK:
                message = "Facebook sign in failed. Please try again later.";
                break;
            case GOOGLE:
                message = "Google sign in failed. Please try again later.";
                break;
            case OTHER:
                message = "Sign in failed. Please contact us.";
        }
        Snackbar snackbar = Snackbar.make(SignInActivity.this.findViewById(R.id.SignInConstraintLayout), message, Snackbar.LENGTH_SHORT);
        snackbar.show();
    }

    private void showSnackbar(String message) {
        Snackbar snackbar = Snackbar.make(SignInActivity.this.findViewById(R.id.SignInConstraintLayout), message, Snackbar.LENGTH_SHORT);
        snackbar.show();
    }
}
