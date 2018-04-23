package com.dhis2.utils.CustomViews;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.design.widget.TextInputEditText;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import com.dhis2.R;
import com.dhis2.data.forms.dataentry.fields.datetime.OnDateSelected;
import com.dhis2.databinding.DateTimeViewBinding;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by frodriguez on 1/15/2018.
 */

public class DateTimeView extends RelativeLayout implements View.OnClickListener, View.OnFocusChangeListener {

    private TextInputEditText editText;
    private DateTimeViewBinding binding;

    private Calendar selectedCalendar;
    private DateFormat dateFormat;
    private LayoutInflater inflater;
    private boolean isBgTransparent;
    private OnDateSelected listener;

    public DateTimeView(Context context) {
        super(context);
        init(context);
    }

    public DateTimeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public DateTimeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        inflater = LayoutInflater.from(context);
    }


    public void setLabel(String label) {
        binding.setLabel(label);
        binding.executePendingBindings();
    }

    public void initData(String data) {
        editText.setText(data);
    }

    public void setIsBgTransparent(boolean isBgTransparent) {
        this.isBgTransparent = isBgTransparent;
        setLayout();
    }

    private void setLayout() {
        binding = DateTimeViewBinding.inflate(inflater, this, true);
        editText = findViewById(R.id.inputEditText);
        selectedCalendar = Calendar.getInstance();
        dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
        editText.setOnFocusChangeListener(this);
        editText.setOnClickListener(this);
    }

    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        if (hasFocus)
            onClick(view);
    }

    public void setDateListener(OnDateSelected listener) {
        this.listener = listener;
    }

    @Override
    public void onClick(View view) {
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dateDialog = new DatePickerDialog(getContext(), (
                (datePicker, year1, month1, day1) -> {
                    selectedCalendar.set(Calendar.YEAR, year1);
                    selectedCalendar.set(Calendar.MONTH, month1);
                    selectedCalendar.set(Calendar.DAY_OF_MONTH, day1);
                    showTimePicker();
                }),
                year,
                month,
                day);
        dateDialog.setTitle(binding.getLabel());
        dateDialog.show();
    }

    private void showTimePicker() {
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        boolean is24HourFormat = android.text.format.DateFormat.is24HourFormat(getContext());

        TimePickerDialog dialog = new TimePickerDialog(getContext(), (
                timePicker, hourOfDay, minutes) -> {
            selectedCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            selectedCalendar.set(Calendar.MINUTE, minutes);
            Date selectedDate = selectedCalendar.getTime();
            String result = dateFormat.format(selectedDate);
            editText.setText(result);
            listener.onDateSelected(selectedDate);
        },
                hour,
                minute,
                is24HourFormat);
        dialog.setTitle(binding.getLabel());
        dialog.show();
    }
}
