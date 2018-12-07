package com.caltruism.assist.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.caltruism.assist.R;

public class FilterActivity extends AppCompatActivity {

    private final static int TYPE_FILTER = 0;
    private final static int DURATTION_FILTER = 1;
    private final static int DISTANCE_FILTER = 2;

    private boolean[] taskSelected = new boolean[5];
    private boolean[] durationSelected = new boolean[5];
    private boolean[] distanceSelected = new boolean[5];

    private Toolbar toolbar;
    private TextView title;

    private TextView textViewTypeAll;
    private ImageView[] typeFilter = new ImageView[4];
    private TextView[] durationFilter = new TextView[5];
    private TextView[] distanceFilter = new TextView[5];

//    private ImageView imageViewTypeGrocery;
//    private ImageView imageViewTypeLaundry;
//    private ImageView imageViewTypeWalking;
//    private ImageView imageViewTypeOther;
//
//    private TextView textViewDurationAll;
//    private TextView textViewDuration15;
//    private TextView textViewDuration30;
//    private TextView textViewDuration45;
//    private TextView textViewDuration60;
//
//    private TextView textViewDistanceAll;
//    private TextView textViewDistance25;
//    private TextView textViewDistance50;
//    private TextView textViewDistance75;
//    private TextView textViewDistance100;

    private TextView textViewTimeFrom;
    private TextView textViewTimeTo;

    private int colorAccent;
    private int colorGrey3;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);

        colorAccent = ResourcesCompat.getColor(getResources(), R.color.colorAccent, null);
        colorGrey3 = ResourcesCompat.getColor(getResources(), R.color.colorGrey3, null);

        textViewTypeAll = findViewById(R.id.taskTypeToggleAll);
        typeFilter[0] = findViewById(R.id.taskTypeToggleGrocery);
        typeFilter[1] = findViewById(R.id.taskTypeToggleLaundry);
        typeFilter[2] = findViewById(R.id.taskTypeToggleWalking);
        typeFilter[3] = findViewById(R.id.taskTypeToggleOther);

        durationFilter[0] = findViewById(R.id.taskDurationToggleAll);
        durationFilter[1] = findViewById(R.id.taskDurationToggle15);
        durationFilter[2] = findViewById(R.id.taskDurationToggle30);
        durationFilter[3] = findViewById(R.id.taskDurationToggle45);
        durationFilter[4] = findViewById(R.id.taskDurationToggle60);

        distanceFilter[0] = findViewById(R.id.taskDistanceToggleAll);
        distanceFilter[1] = findViewById(R.id.taskDistanceToggle25);
        distanceFilter[2] = findViewById(R.id.taskDistanceToggle50);
        distanceFilter[3] = findViewById(R.id.taskDistanceToggle75);
        distanceFilter[4] = findViewById(R.id.taskDistanceToggle100);

        for (int i = 0; i < typeFilter.length; i++) {
            final int index = i + 1;
            typeFilter[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    textViewOnClickListener(TYPE_FILTER, index);
                }
            });
        }

        for (int i = 0; i < durationFilter.length; i++) {
            final int index = i;
            durationFilter[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    textViewOnClickListener(DURATTION_FILTER, index);
                }
            });
        }

        for (int i = 0; i < distanceFilter.length; i++) {
            final int index = i;
            distanceFilter[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    textViewOnClickListener(DISTANCE_FILTER, index);
                }
            });
        }

//        textViewTypeAll = findViewById(R.id.taskTypeToggleAll);
//        imageViewTypeGrocery = findViewById(R.id.taskTypeToggleGrocery);
//        imageViewTypeLaundry = findViewById(R.id.taskTypeToggleLaundry);
//        imageViewTypeWalking = findViewById(R.id.taskTypeToggleWalking);
//        imageViewTypeOther = findViewById(R.id.taskTypeToggleOther);
//
//        textViewDurationAll = findViewById(R.id.taskDurationToggleAll);
//        textViewDuration15 = findViewById(R.id.taskDurationToggle15);
//        textViewDuration30 = findViewById(R.id.taskDurationToggle30);
//        textViewDuration45 = findViewById(R.id.taskDurationToggle45);
//        textViewDuration60 = findViewById(R.id.taskDurationToggle60);
//
//        textViewDistanceAll = findViewById(R.id.taskDistanceToggleAll);
//        textViewDistance25 = findViewById(R.id.taskDistanceToggle25);
//        textViewDistance50 = findViewById(R.id.taskDistanceToggle50);
//        textViewDistance75 = findViewById(R.id.taskDistanceToggle75);
//        textViewDistance100 = findViewById(R.id.taskDistanceToggle100);

        textViewTimeFrom = findViewById(R.id.taskTimeFrom);
        textViewTimeTo = findViewById(R.id.taskTimeTo);


        setupToolbar();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_close, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.actionClose) {
            super.onBackPressed();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public void setupToolbar() {
        toolbar = findViewById(R.id.toolbarFilter);
        title = findViewById(R.id.textViewFilterToolbarTitle);
        setSupportActionBar(toolbar);
        title.setText("Filters");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        toolbar.postDelayed(new Runnable()
        {
            @Override
            public void run ()
            {
                int maxWidth = toolbar.getWidth();
                int titleWidth = title.getWidth();
                int iconWidth = maxWidth - titleWidth;

                if (iconWidth > 0) {
                    int width = maxWidth - iconWidth * 2;
                    title.setMinimumWidth(width);
                    title.getLayoutParams().width = width;
                }
            }
        }, 0);
    }

    private void textViewOnClickListener(int filterType, int index) {

        switch (filterType) {
            case TYPE_FILTER:
                if (index == 0) {

                } else {
                    if (taskSelected[index]) {
                        taskSelected[index] = false;
                    } else {
                        taskSelected[index] = true;

                    }
                }

                break;
            case DURATTION_FILTER:

                break;

            case DISTANCE_FILTER:

                break;
        }
    }

//    private void onTextViewClicked(TextView textView, boolean[] ) {
//        if (isOn) {
//            isOn = false;
//            textView.setBackgroundResource(R.drawable.toggle_button_unselected);
//            textView.setTextColor(colorGrey3);
//        } else {
//            isOn = true;
//            textView.setBackgroundResource(R.drawable.toggle_button_selected);
//            textView.setTextColor(Color.WHITE);
//        }
//    }
//
//    private void onImageViewClicked(ImageView imageView) {
//        if (isOn) {
//            isOn = false;
//            imageView.setBackgroundResource(R.drawable.toggle_button_unselected);
//            ImageViewCompat.setImageTintList(imageView, ColorStateList.valueOf(colorGrey3));
//        } else {
//            isOn = true;
//            imageView.setBackgroundResource(R.drawable.toggle_button_selected);
//            ImageViewCompat.setImageTintList(imageView, ColorStateList.valueOf(Color.WHITE));
//        }
//    }
}
