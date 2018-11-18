package com.caltruism.assist.fragment;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.widget.ImageViewCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.caltruism.assist.R;
import com.caltruism.assist.utils.AddRequestActivityDataListener;
import com.caltruism.assist.utils.Constants;

public class AddRequestSelectTypeFragment extends Fragment {

    private AddRequestActivityDataListener listener;

    private ConstraintLayout groceryConstraintLayout;
    private ImageView groceryImageView;
    private TextView groceryTextView;

    private ConstraintLayout laundryConstraintLayout;
    private ImageView laundryImageView;
    private TextView laundryTextView;

    private ConstraintLayout walkingConstraintLayout;
    private ImageView walkingImageView;
    private TextView walkingTextView;

    private ConstraintLayout otherConstraintLayout;
    private ImageView otherImageView;
    private TextView otherTextView;

    private int previousSelected = -1;
    private float elevationDP;
    private int colorAccent;
    private int colorAccentLight;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof AddRequestActivityDataListener) {
            listener = (AddRequestActivityDataListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement AddRequestActivityDataListener!");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_request_select_type, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        elevationDP = 8 * getResources().getDisplayMetrics().density;
        colorAccent = ResourcesCompat.getColor(getResources(), R.color.colorAccent, null);
        colorAccentLight = ResourcesCompat.getColor(getResources(), R.color.colorAccentLight, null);

        groceryConstraintLayout = getView().findViewById(R.id.requestTypeGrocery);
        groceryImageView = getView().findViewById(R.id.imageSelectTypeGrocery);
        groceryTextView = getView().findViewById(R.id.textViewSelectTypeGrocery);
        groceryConstraintLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateViewStyle(Constants.GROCERY_TYPE);
            }
        });

        laundryConstraintLayout = getView().findViewById(R.id.requestTypeLaundry);
        laundryImageView = getView().findViewById(R.id.imageSelectTypeLaundry);
        laundryTextView = getView().findViewById(R.id.textViewSelectTypeLaundry);
        laundryConstraintLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateViewStyle(Constants.LAUNDRY_TYPE);
            }
        });

        walkingConstraintLayout = getView().findViewById(R.id.requestTypeWalking);
        walkingImageView = getView().findViewById(R.id.imageSelectTypeWalking);
        walkingTextView = getView().findViewById(R.id.textViewSelectTypeWalking);
        walkingConstraintLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateViewStyle(Constants.WALKING_TYPE);
            }
        });

        otherConstraintLayout = getView().findViewById(R.id.requestTypeOther);
        otherImageView = getView().findViewById(R.id.imageSelectTypeOther);
        otherTextView = getView().findViewById(R.id.textViewSelectTypeOther);
        otherConstraintLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateViewStyle(Constants.OTHER_TYPE);
            }
        });
    }

    public void updateViewStyle(int currentSelected) {
        if (previousSelected != -1)
            updateUnselectedView();

        if (previousSelected != currentSelected) {
            previousSelected = currentSelected;
            updateSelectedView();
        } else {
            previousSelected = -1;
        }

        listener.onDataChange(0, previousSelected);
    }

    public void updateUnselectedView() {
        switch (previousSelected) {
            case Constants.GROCERY_TYPE:
                updateGrocery(Color.WHITE, colorAccentLight, elevationDP);
                break;

            case Constants.LAUNDRY_TYPE:
                updateLaundry(Color.WHITE, colorAccentLight, elevationDP);
                break;

            case Constants.WALKING_TYPE:
                updateWalking(Color.WHITE, colorAccentLight, elevationDP);
                break;

            case Constants.OTHER_TYPE:
                updateOther(Color.WHITE, colorAccentLight, elevationDP);
                break;
        }

    }

    public void updateSelectedView() {
        switch (previousSelected) {
            case Constants.GROCERY_TYPE:
                updateGrocery(colorAccent, Color.WHITE, 0);
                break;

            case Constants.LAUNDRY_TYPE:
                updateLaundry(colorAccent, Color.WHITE, 0);
                break;

            case Constants.WALKING_TYPE:
                updateWalking(colorAccent, Color.WHITE, 0);
                break;

            case Constants.OTHER_TYPE:
                updateOther(colorAccent, Color.WHITE, 0);
                break;
        }
    }

    private void updateGrocery(int bgColor, int tintAndTextColor, float elevation) {
        groceryConstraintLayout.setBackgroundColor(bgColor);
        ImageViewCompat.setImageTintList(groceryImageView, ColorStateList.valueOf(tintAndTextColor));
        groceryTextView.setTextColor(tintAndTextColor);
        groceryConstraintLayout.setElevation(elevation);
    }

    private void updateLaundry(int bgColor, int tintAndTextColor, float elevation) {
        laundryConstraintLayout.setBackgroundColor(bgColor);
        ImageViewCompat.setImageTintList(laundryImageView, ColorStateList.valueOf(tintAndTextColor));
        laundryTextView.setTextColor(tintAndTextColor);
        laundryConstraintLayout.setElevation(elevation);
    }

    private void updateWalking(int bgColor, int tintAndTextColor, float elevation) {
        walkingConstraintLayout.setBackgroundColor(bgColor);
        ImageViewCompat.setImageTintList(walkingImageView, ColorStateList.valueOf(tintAndTextColor));
        walkingTextView.setTextColor(tintAndTextColor);
        walkingConstraintLayout.setElevation(elevation);
    }

    private void updateOther(int bgColor, int tintAndTextColor, float elevation) {
        otherConstraintLayout.setBackgroundColor(bgColor);
        ImageViewCompat.setImageTintList(otherImageView, ColorStateList.valueOf(tintAndTextColor));
        otherTextView.setTextColor(tintAndTextColor);
        otherConstraintLayout.setElevation(elevation);
    }
}