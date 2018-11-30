package com.caltruism.assist.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.caltruism.assist.R;
import com.caltruism.assist.fragment.DisabledRequestHistoryFragment;
import com.caltruism.assist.fragment.DisabledRequestListFragment;
import com.caltruism.assist.fragment.VolunteerRequestListFragment;
import com.caltruism.assist.util.Constants;
import com.caltruism.assist.util.CustomCallbackListener;
import com.caltruism.assist.util.SharedPreferencesHelper;
import com.google.firebase.auth.FirebaseAuth;

public class DisabledMainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private NavigationView navigationView;
    private DrawerLayout drawerLayout;

    private DisabledRequestListFragment disabledRequestListFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_disabled);

        drawerLayout = findViewById(R.id.drawerLayoutDisabled);
        navigationView = findViewById(R.id.navViewDisabled);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().getItem(1).setChecked(true);

        View headerView = navigationView.getHeaderView(0);
        ImageView imageViewProfile = headerView.findViewById(R.id.imageViewNavHeaderProfile);
        TextView textViewUsername = headerView.findViewById(R.id.textViewNavHeaderUsername);
        TextView textViewPhoneNumber = headerView.findViewById(R.id.textViewNavHeaderPhoneNumber);

        SharedPreferences sharedPreferences = getSharedPreferences(Constants.USER_DATA_SHARED_PREFERENCE, Context.MODE_PRIVATE);

        RequestOptions requestOptions = new RequestOptions().placeholder(R.drawable.ic_user_solid).centerCrop();
        Glide.with(this).setDefaultRequestOptions(requestOptions).load(sharedPreferences.getString("pictureURL", null)).into(imageViewProfile);

        textViewUsername.setText(sharedPreferences.getString("name", null));
        textViewPhoneNumber.setText(sharedPreferences.getString("phoneNumber", null));

        disabledRequestListFragment = new DisabledRequestListFragment();
        showFragment(disabledRequestListFragment);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        navigationView.setCheckedItem(menuItem);
        drawerLayout.closeDrawer(GravityCompat.START);
        switch (menuItem.getItemId()) {
            case R.id.disabledProfile:
                return true;

            case R.id.disabledRequestList:
                showFragment(new DisabledRequestListFragment());
                return true;

            case R.id.disabledRequestHistory:
                showFragment(new DisabledRequestHistoryFragment());
                return true;

            case R.id.disabledSocial:
                return true;

            case R.id.disabledLeaderboard:
                return true;

            case R.id.disabledSettings:
                return true;

            case R.id.disabledLogout:
                // TODO: Rephrase
                new AlertDialog.Builder(this).setTitle("Logout?")
                        .setMessage("Are you sure you want to logout?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                FirebaseAuth.getInstance().signOut();
                                SharedPreferencesHelper.clearPreferences(DisabledMainActivity.this);
                                startActivity(new Intent(DisabledMainActivity.this, WelcomeActivity.class));
                                finish();
                            }
                        }).setNegativeButton("No", null).show();
                return false;

            case R.id.disabledHelpAndSupport:
                return true;

            case R.id.disabledAboutUs:
                return true;

            default:
                return true;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            drawerLayout.openDrawer(GravityCompat.START, true);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frameLayoutDisabledMain, fragment).commit();
    }
}
