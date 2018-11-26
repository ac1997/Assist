package com.caltruism.assist.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.caltruism.assist.R;
import com.caltruism.assist.util.CustomLoadingDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.HashMap;

public class SignUpActivity extends AppCompatActivity {

    private final static String TAG = "SignUpActivity";

    private EditText editTextFirstName;
    private EditText editTextLastName;
    private EditText editTextEmail;
    private EditText editTextPassword;

    private CustomLoadingDialog customLoadingDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        editTextFirstName = findViewById(R.id.editTextSignUpFirstName);
        editTextLastName = findViewById(R.id.editTextSignUpLastName);
        editTextEmail = findViewById(R.id.editTextSignUpEmail);
        editTextPassword = findViewById(R.id.editTextSignUpPassword);

        Button signUpButton = findViewById(R.id.buttonSignUp);
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUp();
            }
        });

        TextView signIn = findViewById(R.id.textViewSignUpActivitySignIn);
        signIn.setText(Html.fromHtml("Already have an account?&nbsp;&nbsp;<font color='#ffb88e'>Sign In</font>", Html.FROM_HTML_MODE_LEGACY));
        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignUpActivity.this, SignInActivity.class));
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

    public void signUp() {
        final String firstName = editTextFirstName.getText().toString();
        final String lastName = editTextLastName.getText().toString();
        final String email = editTextEmail.getText().toString();
        final String password = editTextPassword.getText().toString();

        // TODO: Rephrase and reinforce password validation
        if (firstName.length() == 0) {
            showSnackbar("First name cannot be empty.");
            return;
        }

        if (lastName.length() == 0) {
            showSnackbar("Last name cannot be empty.");
            return;
        }

        if (password.length() == 0) {
            showSnackbar("Password cannot be empty.");
            return;
        } else if (password.length() < 6) {
            showSnackbar("Password needs to be at least 6 characters.");
            return;
        }

        if (email.length() == 0) {
            showSnackbar("Email cannot be empty.");
            return;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showSnackbar("Invalid email address.");
            return;
        }

        customLoadingDialog.showDialog();
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            initUser(firstName, lastName, email);
                        } else {
                            customLoadingDialog.hideDialog();
                            Log.w(TAG, "Sign up failed:", task.getException());
                            showSnackbar("Sign Up failed. Please try again later.");
                        }
                    }
                });
    }

    private void initUser(String firstName, String lastName, String email) {
        HashMap<String, Object> userData = new HashMap<>();
        userData.put("email", email);
        userData.put("firstName", firstName);
        userData.put("lastName", lastName);

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

    private void showSnackbar(String message) {
        Snackbar snackbar = Snackbar.make(SignUpActivity.this.findViewById(R.id.SignUpConstraintLayout), message, Snackbar.LENGTH_LONG);
        snackbar.show();
    }
}