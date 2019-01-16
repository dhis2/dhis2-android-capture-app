package org.dhis2.utils.custom_views;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import com.google.android.material.textfield.TextInputEditText;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import org.dhis2.R;
import org.dhis2.data.forms.dataentry.fields.datetime.OnDateSelected;
import org.dhis2.databinding.DateTimeViewBinding;
import org.dhis2.utils.DateUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import timber.log.Timber;

/**
 * QUADRAM. Created by frodriguez on 1/15/2018.
 */

public class DateTimeView extends RelativeLayout implements View.OnClickListener, View.OnFocusChangeListener {

    private TextInputEditText editText;
    private DateTimeViewBinding binding;

    private Calendar selectedCalendar;
    private DateFormat dateFormat;
    private LayoutInflater inflater;
    private boolean isBgTransparent;
    private OnDateSelected listener;
    private boolean allowFutureDates;
    private Date date;
    DatePickerDialog dateDialog;

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

    public void setDescription(String description) {
        binding.setDescription(description);
        binding.executePendingBindings();
    }

    public void initData(String data) {
        if (data != null) {
            date = null;
            try {
                date = DateUtils.databaseDateFormat().parse(data);
            } catch (ParseException e) {
                Timber.e(e);
            }

            if (date == null)
                try {
                    date = DateUtils.databaseDateFormatNoMillis().parse(data);
                } catch (ParseException e) {
                    Timber.e(e);
                }

            data = DateUtils.dateTimeFormat().format(date);
        } else {
            editText.setText("");
        }
        editText.setText(data);
    }

    public void setWarningOrError(String msg) {
        editText.setError(msg);
    }

    public void setIsBgTransparent(boolean isBgTransparent) {
        this.isBgTransparent = isBgTransparent;
        setLayout();
    }

    public void setAllowFutureDates(boolean allowFutureDates) {
        this.allowFutureDates = allowFutureDates;
    }

    private void setLayout() {
        binding = DateTimeViewBinding.inflate(inflater, this, true);
        editText = findViewById(R.id.inputEditText);
        selectedCalendar = Calendar.getInstance();
        dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
        editText.setFocusable(false); //Makes editText not editable
        editText.setClickable(true);//  but clickable
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
        if (date != null)
            c.setTime(date);
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        dateDialog = new DatePickerDialog(getContext(), (
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
        if (!allowFutureDates) {
            dateDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        }
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

    public TextInputEditText getEditText() {
        return editText;
    }

    public void setEditable(Boolean editable) {
        editText.setEnabled(editable);
    }
}
