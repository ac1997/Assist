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

import com.arlib.floatingsearchview.FloatingSearchView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.caltruism.assist.R;
import com.caltruism.assist.fragment.VolunteerRequestListFragment;
import com.caltruism.assist.util.Constants;
import com.caltruism.assist.util.CustomCallbackListener;
import com.caltruism.assist.util.SharedPreferencesHelper;
import com.google.firebase.auth.FirebaseAuth;

public class VolunteerMainActivity extends AppCompatActivity implements
        CustomCallbackListener.VolunteerRequestListFragmentCallbackListener, NavigationView.OnNavigationItemSelectedListener {

    private NavigationView navigationView;
    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_volunteer);

        drawerLayout = findViewById(R.id.drawerLayoutVolunteer);
        navigationView = findViewById(R.id.navViewVolunteer);
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

        showFragment(new VolunteerRequestListFragment());
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        navigationView.setCheckedItem(menuItem);
        drawerLayout.closeDrawer(GravityCompat.START);
        switch (menuItem.getItemId()) {
            case R.id.volunteerProfile:
                return true;

            case R.id.volunteerTaskList:
                showFragment(new VolunteerRequestListFragment());
                return true;

            case R.id.volunteerTaskHistory:
//                showFragment(new ScrollingSearchExampleFragment());
                return true;

            case R.id.volunteerSocial:
                return true;

            case R.id.volunteerLeaderboard:
                return true;

            case R.id.volunteerSettings:
                return true;

            case R.id.volunteerLogout:
                // TODO: Rephrase
                new AlertDialog.Builder(this).setTitle("Logout?")
                        .setMessage("Are you sure you want to logout?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                FirebaseAuth.getInstance().signOut();
                                SharedPreferencesHelper.clearPreferences(VolunteerMainActivity.this);
                                startActivity(new Intent(VolunteerMainActivity.this, WelcomeActivity.class));
                                finish();
                            }
                        }).setNegativeButton("No", null).show();
                return false;

            case R.id.volunteerHelpAndSupport:
                return true;

            case R.id.volunteerAboutUs:
                return true;

            default:
                return true;
        }
    }

    @Override
    public void onAttachSearchViewToDrawer(FloatingSearchView searchView) {
        searchView.attachNavigationDrawerToMenuButton(drawerLayout);
    }

    private void showFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frameLayoutVolunteerMain, fragment).commit();
    }
}
