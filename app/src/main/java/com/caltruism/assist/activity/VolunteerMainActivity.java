package com.caltruism.assist.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.arlib.floatingsearchview.FloatingSearchView;
import com.caltruism.assist.R;
import com.caltruism.assist.fragment.VolunteerRequestListFragment;
import com.caltruism.assist.util.VolunteerBaseFragment;
import com.google.firebase.auth.FirebaseAuth;

public class VolunteerMainActivity extends AppCompatActivity implements
        VolunteerBaseFragment.VolunteerBaseFragmentCallback, NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_volunteer);

        drawerLayout = findViewById(R.id.volunteerDrawerLayout);
        NavigationView navigationView = findViewById(R.id.volunteerNavView);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().getItem(1).setChecked(true);

        showFragment(new VolunteerRequestListFragment());
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        drawerLayout.closeDrawer(GravityCompat.START);
        switch (menuItem.getItemId()) {
            case R.id.volunteerprofile:
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
            case R.id.volunteerLogout:
                // TODO: Rephrase
                new AlertDialog.Builder(this).setTitle("Logout?")
                        .setMessage("Are you sure you want to logout?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                FirebaseAuth.getInstance().signOut();
                                startActivity(new Intent(VolunteerMainActivity.this, LoginActivity.class));
                                finish();
                            }
                        }).setNegativeButton("No", null).show();
                return false;
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
                .replace(R.id.volunteerMainFragmentContainer, fragment).commit();
    }
}
