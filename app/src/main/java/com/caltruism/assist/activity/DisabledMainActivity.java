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

import com.caltruism.assist.R;
import com.caltruism.assist.fragment.DisabledRequestListFragment;
import com.caltruism.assist.util.CustomCallbackListener;
import com.caltruism.assist.util.SharedPreferencesHelper;
import com.google.firebase.auth.FirebaseAuth;

public class DisabledMainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, CustomCallbackListener.DisabledMainActivityCallbackListener {

    private DrawerLayout drawerLayout;

    private DisabledRequestListFragment disabledRequestListFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_disabled);

        drawerLayout = findViewById(R.id.drawerLayoutDisabled);
        NavigationView navigationView = findViewById(R.id.navViewDisabled);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().getItem(1).setChecked(true);

        disabledRequestListFragment = new DisabledRequestListFragment();
        showFragment(disabledRequestListFragment);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        drawerLayout.closeDrawer(GravityCompat.START);
        switch (menuItem.getItemId()) {
            case R.id.disabledProfile:
                return true;

            case R.id.disabledRequestList:
                showFragment(new DisabledRequestListFragment());
                return true;

            case R.id.disabledRequestHistory:
//                showFragment(new ScrollingSearchExampleFragment());
                return true;

            case R.id.disabledSocial:

                return true;
            case R.id.disabledLeaderboard:

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

    @Override
    public void onDisabledRequestListChildFragmentDataSetEmpty(boolean isWaitingView) {
        disabledRequestListFragment.onDataSetEmpty(isWaitingView);
    }
}
