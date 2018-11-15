package com.caltruism.assist.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.caltruism.assist.R;
import com.google.firebase.auth.FirebaseAuth;

public class RequestListVolunteerActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_list_volunteer);
    }

    @Override
    protected void onStart() {
        super.onStart();

        Button logout = findViewById(R.id.logoutButton);

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(RequestListVolunteerActivity.this, LoginActivity.class));
                finish();
            }
        });
    }
}
