package com.caltruism.assist.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.caltruism.assist.R;

public class WelcomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        Button buttonVolunteer = findViewById(R.id.buttonWelcomSignUp);
        buttonVolunteer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(WelcomeActivity.this, SignUpActivity.class));
                finish();
            }
        });

        Button buttonDisabled = findViewById(R.id.buttonWelcomeSignIn);
        buttonDisabled.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(WelcomeActivity.this, SignInActivity.class));
                finish();
            }
        });
    }
}
