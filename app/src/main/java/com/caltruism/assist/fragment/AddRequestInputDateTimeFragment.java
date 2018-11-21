package com.caltruism.assist.fragment;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.caltruism.assist.R;
import com.caltruism.assist.util.Constants;
import com.caltruism.assist.util.CustomDateTimeUtil;
import com.caltruism.assist.util.DataListener;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;

public class AddRequestInputDateTimeFragment extends Fragment implements DataListener.AddRequestFragmentDataListener {

    private static final String IS_NOW_KEY = "isNow";
    private static final String DATE_TIME_KEY = "dateTime";
    private static final String DURATION_KEY = "duration";

    private DataListener.AddRequestActivityDataListener listener;
    private DatePickerDialog.OnDateSetListener dateSetListener;
    private TimePickerDialog.OnTimeSetListener timeSetListener;

    private ImageView imageViewType;
    private TextView textViewType;
    private TextView textViewTitle;
    private EditText editTextDate;
    private EditText editTextTime;
    private EditText editTextDuration;

    private HashMap<String, Object> dataMap = new HashMap<>();
    private int requestType;
    private String requestTitle;

    Calendar requestDateAndTime;
    boolean isNow = true;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof DataListener.AddRequestActivityDataListener) {
            listener = (DataListener.AddRequestActivityDataListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement DataListener.AddRequestActivityDataListener!");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        requestType = arguments.getInt("requestType");
        requestTitle = arguments.getString("requestTitle");

        return inflater.inflate(R.layout.fragment_add_request_input_date_time, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        imageViewType = view.findViewById(R.id.imageViewInputDateTimeRequestType);
        textViewType = view.findViewById(R.id.textViewInputDateTimeRequestType);
        textViewTitle = view.findViewById(R.id.textViewInputDateTimeRequestTitle);
        editTextDate = view.findViewById(R.id.editTextInputDate);
        editTextTime = view.findViewById(R.id.editTextInputTime);
        editTextDuration = view.findViewById(R.id.editTextInputDuration);

        requestDateAndTime = Calendar.getInstance();
        dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                requestDateAndTime.set(Calendar.YEAR, year);
                requestDateAndTime.set(Calendar.MONTH, month);
                requestDateAndTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateDate();
            }
        };

        timeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                requestDateAndTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                requestDateAndTime.set(Calendar.MINUTE, minute);
                updateTime();
            }
        };

        editTextDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long now = System.currentTimeMillis() - 1000;
                DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), dateSetListener,
                        requestDateAndTime.get(Calendar.YEAR),
                        requestDateAndTime.get(Calendar.MONTH),
                        requestDateAndTime.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.getDatePicker().setMinDate(now);
                datePickerDialog.getDatePicker().setMaxDate(now + 2 * DateUtils.WEEK_IN_MILLIS);
                datePickerDialog.show();
            }
        });

        editTextTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), timeSetListener,
                        requestDateAndTime.get(Calendar.HOUR_OF_DAY),
                        requestDateAndTime.get(Calendar.MINUTE), false);
                timePickerDialog.show();
            }
        });

        editTextDuration.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                handleDataChange(DURATION_KEY, s);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        setRequestType();
        handleDataChange(DATE_TIME_KEY, null);
    }

    @Override
    public void onDataChange(Object... data) {
        requestType = (int) data[0];
        requestTitle = (String) data[1];
        setRequestType();
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
        textViewTitle.setText(requestTitle);
    }

    private void updateDate() {
        String date = CustomDateTimeUtil.getDate(requestDateAndTime.getTimeInMillis());
        editTextDate.setText(date);
        updateTime();
    }

    private void updateTime() {
        String time;
        long now = System.currentTimeMillis() - 1000;
        long requestTime = requestDateAndTime.getTimeInMillis() + 3 * DateUtils.MINUTE_IN_MILLIS;

        if (requestTime < now) {
            // TODO: CHANGE PHRASE
            Toast.makeText(getActivity(), "Time past. Please select again.", Toast.LENGTH_SHORT).show();
            TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), timeSetListener,
                    requestDateAndTime.get(Calendar.HOUR_OF_DAY),
                    requestDateAndTime.get(Calendar.MINUTE), false);
            timePickerDialog.show();
            return;
        } else if (requestTime - 10 * DateUtils.MINUTE_IN_MILLIS <= now) {
            time = "Now";
            isNow = true;
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.US);
            time = sdf.format(requestDateAndTime.getTime());
            isNow = false;
        }
        editTextTime.setText(time);
        handleDataChange(DATE_TIME_KEY, null);
    }

    public void handleDataChange(String key, CharSequence value) {
        switch (key) {
            case DATE_TIME_KEY:
                dataMap.put(IS_NOW_KEY, isNow);
                dataMap.put(DATE_TIME_KEY, requestDateAndTime.getTimeInMillis());
                break;
            case DURATION_KEY:
                int duration = Integer.parseInt(value.toString());

                if (duration != 0)
                    dataMap.put(DURATION_KEY, duration);
                else
                    dataMap.remove(key);
                break;
        }

        listener.onDataChange(2, dataMap);
    }
}
