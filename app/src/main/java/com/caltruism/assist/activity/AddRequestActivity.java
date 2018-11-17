package com.caltruism.assist.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.caltruism.assist.R;
import com.caltruism.assist.fragment.AddRequestInputDateTimeFragment;
import com.caltruism.assist.fragment.AddRequestInputDetailsFragment;
import com.caltruism.assist.fragment.AddRequestSelectTypeFragment;
import com.caltruism.assist.fragment.AddRequestSummaryFragment;
import com.caltruism.assist.utils.AddRequestDataListener;
import com.shuhart.stepview.StepView;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class AddRequestActivity extends AppCompatActivity implements AddRequestDataListener {

    private static final String TITLE_STEP0 = "Select a type";
    private static final String TITLE_STEP1 = "Details";
    private static final String TITLE_STEP2 = "Date and Time";
    private static final String TITLE_STEP3 = "Summary";

    private static final int STEP_COUNT = 4;
    private static final String TITLE_KEY = "title";
    private static final String DESCRIPTION_KEY = "description";
    private static final String LOCATION_KEY = "location";
    private static final String DATE_TIME_KEY = "dateTime";
    private static final String DURATION_KEY = "duration";

    private int currentStep = 0;
    private int highestStepCompleted = 0;

    private int selectedType;
    private String requestTitle;
    private String requestDescription;
    private String requestLocation;
    private String requestDateTime;
    private String requestDuration;

    private Toolbar toolbar;
    private TextView title;

    private FragmentManager fragmentManager;
    private AddRequestSelectTypeFragment selectTypeFragment;
    private AddRequestInputDetailsFragment inputDetailsFragment;
    private AddRequestInputDateTimeFragment inputDateTimeFragment;
    private AddRequestSummaryFragment summaryFragment;

    private StepView stepView;
    private Button nextButton;
    private Button editButton;
    private Button requestButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_request);

        setupToolbar();
        setupStepView();

        selectTypeFragment = new AddRequestSelectTypeFragment();
        inputDetailsFragment = new AddRequestInputDetailsFragment();
        inputDateTimeFragment = new AddRequestInputDateTimeFragment();
        summaryFragment = new AddRequestSummaryFragment();

        fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.fragmentCreateRequest, selectTypeFragment);
        fragmentTransaction.commit();

        nextButton = findViewById(R.id.buttonAddRequestNext);
        editButton = findViewById(R.id.buttonAddRequestEdit);
        requestButton = findViewById(R.id.buttonAddRequestRequest);

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (highestStepCompleted < currentStep + 1) {
                    highestStepCompleted = currentStep + 1;
                    nextButton.setEnabled(false);
                }
                updateUI(currentStep + 1);
            }
        });

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUI(0);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_create_request, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_close) {
            Intent intent = new Intent(this, RequestListDisabledActivity.class);
            startActivity(intent);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onDataChange(int step, Object data) {
        HashMap<String, String> dataMap;
        boolean enable;

        switch (currentStep) {
            case 0:
                selectedType = (int) data;
                if (selectedType != -1)
                    nextButton.setEnabled(true);
                else
                    nextButton.setEnabled(false);
                break;

            case 1:
                dataMap = (HashMap<String, String>) data;
                enable = dataMap.size() == 3;

                if (dataMap.containsKey(TITLE_KEY))
                    requestTitle = dataMap.get(TITLE_KEY);

                if (dataMap.containsKey(DESCRIPTION_KEY))
                    requestDescription = dataMap.get(DESCRIPTION_KEY);

                if (dataMap.containsKey(LOCATION_KEY))
                    requestLocation = dataMap.get(LOCATION_KEY);

                nextButton.setEnabled(enable);
                break;

            case 2:
                dataMap = (HashMap<String, String>) data;
                enable = dataMap.size() == 2;

                if (dataMap.containsKey(DATE_TIME_KEY))
                    requestDateTime = dataMap.get(DATE_TIME_KEY);

                if (dataMap.containsKey(DURATION_KEY)) {
                    requestDuration = dataMap.get(DURATION_KEY);
                    if (Integer.parseInt(requestDuration) == 0)
                        enable = false;
                }


                nextButton.setEnabled(enable);
                break;

        }
    }

    public void setupToolbar() {
        toolbar = findViewById(R.id.toolbarAddRequest);
        title = findViewById(R.id.textViewAddRequestTitle);
        setSupportActionBar(toolbar);
        title.setText(TITLE_STEP0);

        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        toolbar.postDelayed(new Runnable()
        {
            @Override
            public void run ()
            {
                int maxWidth = toolbar.getWidth();
                int titleWidth = title.getWidth();
                int iconWidth = maxWidth - titleWidth;

                if (iconWidth > 0)
                {
                    int width = maxWidth - iconWidth * 2;
                    title.setMinimumWidth(width);
                    title.getLayoutParams().width = width;
                }
            }
        }, 0);
    }

    private void setupStepView() {
        stepView = findViewById(R.id.stepView);
        List<String> steps = Arrays.asList("Type", "Details", "Date", "Summary");
        stepView.setSteps(steps);

        stepView.setOnStepClickListener(new StepView.OnStepClickListener() {
            @Override
            public void onStepClick(int step) {
                if (step <= highestStepCompleted) {
                    if (currentStep != step)
                        updateUI(step);
                } else {
                    Toast.makeText(AddRequestActivity.this, "Please complete this step first", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateUI(int nextStep) {
        updateCurrentStepAndStepView(nextStep);
        updateFragment();
        updateButton();
    }

    private void updateCurrentStepAndStepView(int nextStep) {
        if (nextStep < STEP_COUNT) {
            currentStep = nextStep;
            stepView.go(currentStep, true);
        } else {
            stepView.done(true);
        }
    }

    private void updateFragment() {
        Bundle arguments;
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        switch (currentStep) {
            case 0:
                fragmentTransaction.replace(R.id.fragmentCreateRequest, selectTypeFragment);
                title.setText(TITLE_STEP0);
                break;

            case 1:
                arguments = new Bundle();
                arguments.putInt("requestType", selectedType);
                inputDetailsFragment.setArguments(arguments);
                fragmentTransaction.replace(R.id.fragmentCreateRequest, inputDetailsFragment);
                title.setText(TITLE_STEP1);
                break;

            case 2:
                arguments = new Bundle();
                arguments.putInt("requestType", selectedType);
                arguments.putString("requestTitle", requestTitle);
                inputDateTimeFragment.setArguments(arguments);
                fragmentTransaction.replace(R.id.fragmentCreateRequest, inputDateTimeFragment);
                title.setText(TITLE_STEP2);
                break;

            case 3:
                arguments = new Bundle();
                arguments.putInt("requestType", selectedType);
                arguments.putString("requestTitle", requestTitle);
                arguments.putString("requestDescription", requestDescription);
                arguments.putString("requestLocation", requestLocation);
                arguments.putString("requestDateTime", requestDateTime);
                arguments.putString("requestDuration", requestDuration);
                summaryFragment.setArguments(arguments);
                fragmentTransaction.replace(R.id.fragmentCreateRequest, summaryFragment);
                title.setText(TITLE_STEP3);
                break;

        }
        fragmentTransaction.commit();
    }

    private void updateButton() {
        if (currentStep == 3) {
            nextButton.setVisibility(View.INVISIBLE);
            editButton.setVisibility(View.VISIBLE);
            requestButton.setVisibility(View.VISIBLE);
        } else {
            nextButton.setVisibility(View.VISIBLE);
            editButton.setVisibility(View.INVISIBLE);
            requestButton.setVisibility(View.INVISIBLE);
        }
    }
}
