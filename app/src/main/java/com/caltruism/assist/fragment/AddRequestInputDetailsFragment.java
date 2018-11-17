package com.caltruism.assist.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.caltruism.assist.R;
import com.caltruism.assist.utils.AddRequestDataListener;

import java.util.HashMap;

public class AddRequestInputDetailsFragment extends Fragment {

    private static final String TITLE_KEY = "title";
    private static final String DESCRIPTION_KEY = "description";
    private static final String LOCATION_KEY = "location";
    private static final String CURRENT_LOCATION = "Current Location";

    private AddRequestDataListener listener;

    private HashMap<String, String> data = new HashMap<>();
    private int requestType;

    private ImageView imageViewType;
    private TextView textViewType;
    private EditText editTextTitle;
    private EditText editTextDescription;
    private EditText editTextLocation;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof AddRequestDataListener) {
            listener = (AddRequestDataListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement AddRequestDataListener!");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        requestType = arguments.getInt("requestType");

        return inflater.inflate(R.layout.fragment_add_request_input_details, container, false);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        data.put(LOCATION_KEY, CURRENT_LOCATION);

        imageViewType = getView().findViewById(R.id.imageViewInputDetailsRequestType);
        textViewType = getView().findViewById(R.id.textViewInputDetailsRequestType);
        editTextTitle = getView().findViewById(R.id.editTextInputDetailsTitle);
        editTextDescription = getView().findViewById(R.id.editTextInputDetailsDescription);
        editTextLocation = getView().findViewById(R.id.editTextInputDetailsLocation);

        setRequestType();

        editTextTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                handleDataChange(TITLE_KEY, s);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        editTextDescription.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                handleDataChange(DESCRIPTION_KEY, s);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        editTextLocation.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                handleDataChange(LOCATION_KEY, s);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        editTextLocation.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(MotionEvent.ACTION_UP == event.getAction()) {
                    if (editTextLocation.getText().toString().equals(CURRENT_LOCATION))
                        editTextLocation.setText("");
                }
                return false;
            }
        });
    }

    private void setRequestType() {
        switch (requestType) {
            case 0:
                imageViewType.setImageResource(R.drawable.ic_add_request_grocery);
                textViewType.setText("Grocery");
                break;

            case 1:
                imageViewType.setImageResource(R.drawable.ic_add_request_laundry);
                textViewType.setText("Laundry");
                break;

            case 2:
                imageViewType.setImageResource(R.drawable.ic_add_request_walking);
                textViewType.setText("Walking");
                break;

            case 3:
                imageViewType.setImageResource(R.drawable.ic_add_request_other);
                textViewType.setText("Other Request");
                break;
        }
    }

    private void handleDataChange(String key, CharSequence value) {
        if (value.toString().length() != 0)
            data.put(key, value.toString());
        else
            data.remove(key);

        listener.onDataChange(1, data);
    }
}