package com.caltruism.assist.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.caltruism.assist.R;
import com.caltruism.assist.util.SharedPreferencesHelper;

import java.util.HashMap;

public class GetMemberPhoneNumberActivity extends AppCompatActivity {

    private final static String TAG = "GetMemberPhoneNumberActivity";

    private EditText editTextPhoneNumber;

    private HashMap<String, Object> userData;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_member_phone_number);

        Intent intent = getIntent();
        userData = (HashMap<String, Object>) intent.getSerializableExtra("userData");
        if (userData == null)
            userData = new HashMap<>();

        editTextPhoneNumber = findViewById(R.id.editTextGetMemberPhoneNumber);
        TextView textViewTermAndCondition = findViewById(R.id.textViewGetMemberPhoneTermAndCondition);
        textViewTermAndCondition.setText(Html.fromHtml("By continuing, you are indicating that you agree to <br /> the <font color='#ffb88e'>Privacy Policy</font> and <font color='#ffb88e'>Terms of Service</font>", Html.FROM_HTML_MODE_LEGACY));

        Button buttonNext = findViewById(R.id.buttonGetMemberPhoneNumberNext);
        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleNextClicked(editTextPhoneNumber.getText().toString());
            }
        });
    }

    private void handleNextClicked(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.length() == 0) {
            showSnackbar("Please enter your phone number.");
        } else if (!phoneNumber.matches("^[+]?[0-9]{10}$")) {
            showSnackbar("Please enter a valid phone number.");
        } else {
            phoneNumber = String.format("(%s) %s-%s", phoneNumber.substring(0,3), phoneNumber.substring(3,6), phoneNumber.substring(6,10));
            userData.put("phoneNumber", phoneNumber);
            Log.e(TAG, "phone number: " + phoneNumber);

            Intent intent;
            Context context = GetMemberPhoneNumberActivity.this;

            SharedPreferencesHelper.setPreferences(GetMemberPhoneNumberActivity.this, userData);

            if (userData.containsKey("memberType")) {
                String memberTypeString = userData.get("memberType").toString();

                if (memberTypeString.equals(getResources().getString(R.string.volunteer_type))) {
                    intent = new Intent(context, VolunteerMainActivity.class);
                } else if (memberTypeString.equals(getResources().getString(R.string.disabled_type))) {
                    intent = new Intent(context, DisabledMainActivity.class);
                } else {
                    Log.e(TAG, "Invalid memberTypeString " + memberTypeString);
                    intent = new Intent(context, GetMemberTypeActivity.class);
                    intent.putExtra("userData", userData);
                }
            } else {
                intent = new Intent(context, GetMemberTypeActivity.class);
                intent.putExtra("userData", userData);
            }

            startActivity(intent);
            finish();
        }
    }

    private void showSnackbar(String message) {
        Snackbar snackbar = Snackbar.make(GetMemberPhoneNumberActivity.this.findViewById(R.id.getMemberPhoneNumberConstraintLayout), message, Snackbar.LENGTH_SHORT);
        snackbar.show();
    }
}
