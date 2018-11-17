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

import com.caltruism.assist.R;
import com.caltruism.assist.utils.AddRequestDataListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class AddRequestInputDateTimeFragment extends Fragment {

    private static final String DATE_TIME_KEY = "dateTime";
    private static final String DURATION_KEY = "duration";

    private static final int TEN_MINUTES = 1000 * 10 * 60;
    private static final int TWO_WEEKS = 1000 * 60 * 60 * 24 * 14;

    private AddRequestDataListener listener;

    private HashMap<String, String> data = new HashMap<>();
    private int requestType;
    private String requestTitle;

    Calendar requestDateAndTime;
    boolean isToday;

    private ImageView imageViewType;
    private TextView textViewType;
    private TextView textViewTitle;
    private EditText editTextDate;
    private EditText editTextTime;
    private EditText editTextDuration;

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
        requestTitle = arguments.getString("requestTitle");

        return inflater.inflate(R.layout.fragment_add_request_input_date_time, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        imageViewType = getView().findViewById(R.id.imageViewInputDateTimeRequestType);
        textViewType = getView().findViewById(R.id.textViewInputDateTimeRequestType);
        textViewTitle = getView().findViewById(R.id.textViewInputDateTimeRequestTitle);
        editTextDate = getView().findViewById(R.id.editTextInputDate);
        editTextTime = getView().findViewById(R.id.editTextInputTime);
        editTextDuration = getView().findViewById(R.id.editTextInputDuration);

        requestDateAndTime = Calendar.getInstance();
        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                requestDateAndTime.set(Calendar.YEAR, year);
                requestDateAndTime.set(Calendar.MONTH, month);
                requestDateAndTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                handleDataChange(DATE_TIME_KEY, null);
                updateDateEditText();
            }
        };

        final TimePickerDialog.OnTimeSetListener time = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                requestDateAndTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                requestDateAndTime.set(Calendar.MINUTE, minute);
                handleDataChange(DATE_TIME_KEY, null);
                updateTimeEditText();
            }
        };

        editTextDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long now = System.currentTimeMillis() - 1000;
                DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), date,
                        requestDateAndTime.get(Calendar.YEAR),
                        requestDateAndTime.get(Calendar.MONTH),
                        requestDateAndTime.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.getDatePicker().setMinDate(now);
                datePickerDialog.getDatePicker().setMaxDate(now + TWO_WEEKS);
                datePickerDialog.setTitle("Select Date");
                datePickerDialog.show();
            }
        });

        editTextTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), time,
                        requestDateAndTime.get(Calendar.HOUR_OF_DAY),
                        requestDateAndTime.get(Calendar.MINUTE), false);
                timePickerDialog.setTitle("Select Time");
                timePickerDialog.show();
            }
        });

        editTextDuration.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                handleDataChange(DURATION_KEY, s);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        setRequestType();
        handleDataChange(DATE_TIME_KEY, null);
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
        textViewTitle.setText(requestTitle);
    }

    private void updateDateEditText() {
        String date;
        long requestTime = requestDateAndTime.getTimeInMillis();

        isToday = false;
        if (DateUtils.isToday(requestTime)) {
            date = "Today";
            isToday = true;
        } else if (DateUtils.isToday(requestTime - DateUtils.DAY_IN_MILLIS)) {
            date = "Tomorrow";
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy", Locale.US);
            date = sdf.format(requestDateAndTime.getTime());
        }

        editTextDate.setText(date);
    }

    private void updateTimeEditText() {
        String time;
        long now = System.currentTimeMillis() - 1000;
        long requestTime = requestDateAndTime.getTimeInMillis() + DateUtils.MINUTE_IN_MILLIS;

        if (requestTime < now) {
            // TODO: Show snackbar error
//            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.US);
//            time = sdf.format(requestDateAndTime.getTime());
            time = "ERROR";
        } else if (requestTime - TEN_MINUTES <= now) {
            time = "Now";
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.US);
            time = sdf.format(requestDateAndTime.getTime());
        }
        editTextTime.setText(time);
    }

    public void handleDataChange(String key, CharSequence value) {
        switch (key) {
            case DATE_TIME_KEY:
                data.put(DATE_TIME_KEY, String.valueOf(requestDateAndTime.getTimeInMillis()));
                break;
            case DURATION_KEY:
                if (value.toString().length() != 0)
                    data.put(DURATION_KEY, value.toString());
                else
                    data.remove(key);
                break;
        }

        listener.onDataChange(2, data);
    }
}
