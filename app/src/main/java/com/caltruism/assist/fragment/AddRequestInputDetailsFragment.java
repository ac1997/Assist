package com.caltruism.assist.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.caltruism.assist.R;
import com.caltruism.assist.util.CustomCallbackListener;
import com.caltruism.assist.util.Constants;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;

import java.util.HashMap;

import static android.app.Activity.RESULT_OK;

public class AddRequestInputDetailsFragment extends Fragment implements CustomCallbackListener.AddRequestFragmentCallbackListener {

    private static final String TAG = "AddRequestInputDetailsFragment";
    private static final int REQUEST_CODE_AUTOCOMPLETE = 1;

    private static final String GOOGLE_PLAY_SERVICE_ERROR = "Google Play Services is not available";

    private static final String TITLE_KEY = "title";
    private static final String DESCRIPTION_KEY = "description";
    private static final String IS_CURRENT_LOCATION_KEY = "isCurrentLocation";
    private static final String LOCATION_NAME_KEY = "locationName";
    private static final String LOCATION_ADDRESS_KEY = "locationAddress";
    private static final String LOCATION_LAT_LNG_KEY = "locationLatLng";

    private CustomCallbackListener.AddRequestActivityCallbackListener callbackListener;

    private ImageView imageViewType;
    private TextView textViewType;
    private EditText editTextLocation;

    private HashMap<String, Object> dataMap = new HashMap<>();
    private int requestType;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof CustomCallbackListener.AddRequestActivityCallbackListener) {
            callbackListener = (CustomCallbackListener.AddRequestActivityCallbackListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement CustomCallbackListener.AddRequestActivityCallbackListener!");
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

        dataMap.put(IS_CURRENT_LOCATION_KEY, true);

        imageViewType = view.findViewById(R.id.imageViewInputDetailsRequestType);
        textViewType = view.findViewById(R.id.textViewInputDetailsRequestType);
        editTextLocation = view.findViewById(R.id.editTextInputDetailsLocation);
        setRequestType();

        EditText editTextTitle = view.findViewById(R.id.editTextInputDetailsTitle);
        EditText editTextDescription = view.findViewById(R.id.editTextInputDetailsDescription);
        editTextDescription.setImeOptions(EditorInfo.IME_ACTION_DONE);
        editTextDescription.setRawInputType(InputType.TYPE_CLASS_TEXT);

        editTextTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                handleDataChange(TITLE_KEY, s, true);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        editTextDescription.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                handleDataChange(DESCRIPTION_KEY, s, true);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });


        editTextLocation.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(MotionEvent.ACTION_UP == event.getAction()) {
                    openAutocompleteActivity();
                    editTextLocation.setClickable(false);
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_AUTOCOMPLETE) {
            editTextLocation.setClickable(true);

            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(getActivity(), data);

                editTextLocation.setText(place.getAddress());
                dataMap.put(IS_CURRENT_LOCATION_KEY, false);
                handleDataChange(LOCATION_NAME_KEY, place.getName(), false);
                handleDataChange(LOCATION_ADDRESS_KEY, place.getAddress(), false);
                handleDataChange(LOCATION_LAT_LNG_KEY, place.getLatLng(), true);
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(getActivity(), data);
                Log.e(TAG, status.getStatusMessage());
            }
        }
    }

    @Override
    public void onDataChange(Object... data) {
        requestType = (int) data[0];
        setRequestType();
    }

    private void openAutocompleteActivity() {
        try {
            AutocompleteFilter typeFilter = new AutocompleteFilter.Builder().setCountry("US").build();
            Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY).setFilter(typeFilter).build(getActivity());
            startActivityForResult(intent, REQUEST_CODE_AUTOCOMPLETE);
        } catch (GooglePlayServicesRepairableException e) {
            GoogleApiAvailability.getInstance().getErrorDialog(getActivity(), e.getConnectionStatusCode(), 0).show();
        } catch (GooglePlayServicesNotAvailableException e) {
            String message = GOOGLE_PLAY_SERVICE_ERROR+ ": " + GoogleApiAvailability.getInstance().getErrorString(e.errorCode);

            Log.e(TAG, message);
            Toast.makeText(getActivity(), GOOGLE_PLAY_SERVICE_ERROR, Toast.LENGTH_SHORT).show();
        }
    }

    private void setRequestType() {
        switch (requestType) {
            case Constants.GROCERY_TYPE:
                imageViewType.setImageResource(R.drawable.ic_add_request_grocery);
                textViewType.setText("Grocery");
                break;

            case Constants.LAUNDRY_TYPE:
                imageViewType.setImageResource(R.drawable.ic_add_request_laundry);
                textViewType.setText("Laundry");
                break;

            case Constants.WALKING_TYPE:
                imageViewType.setImageResource(R.drawable.ic_add_request_walking);
                textViewType.setText("Walking");
                break;

            case Constants.OTHER_TYPE:
                imageViewType.setImageResource(R.drawable.ic_add_request_other);
                textViewType.setText("Other Request");
                break;
        }
    }

    private void handleDataChange(String key, Object value, boolean signal) {
        if (value instanceof CharSequence) {
            if (value.toString().length() != 0)
                dataMap.put(key, value.toString());
            else
                dataMap.remove(key);
        } else {
            dataMap.put(key, value);
        }

        if (signal)
            callbackListener.onDataChange(1, dataMap);
    }
}