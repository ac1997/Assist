package com.caltruism.assist.activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.caltruism.assist.R;
import com.google.firebase.auth.FirebaseAuth;

public class DisabledMainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_ADD_REQUEST = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_disabled);

        FloatingActionButton fab = findViewById(R.id.fabAddRequest);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(DisabledMainActivity.this, AddRequestActivity.class), REQUEST_CODE_ADD_REQUEST);
            }
        });

        Button logout = findViewById(R.id.logoutButton);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(DisabledMainActivity.this, LoginActivity.class));
                finish();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_ADD_REQUEST && resultCode == Activity.RESULT_OK) {
            // TODO: Redesign and rephrase
            new AlertDialog.Builder(this).setTitle("Request Posted")
                    .setMessage("We will let you know when someone accepted the request.")
                    .setPositiveButton("Ok", null).show();
        }
    }
}
