package org.dhis2.utils.custom_views;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.DatePicker;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.dhis2.R;
import org.dhis2.data.forms.dataentry.fields.datetime.OnDateSelected;
import org.dhis2.databinding.DateTimeViewBinding;
import org.dhis2.utils.DatePickerUtils;
import org.dhis2.utils.DateUtils;
import org.dhis2.utils.OnDatePickerClickListener;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import timber.log.Timber;

/**
 * QUADRAM. Created by frodriguez on 1/15/2018.
 */

public class DateTimeView extends FieldLayout implements View.OnClickListener, View.OnFocusChangeListener {

    private TextInputEditText editText;
    private TextInputLayout inputLayout;
    private DateTimeViewBinding binding;

    private Calendar selectedCalendar;
    private DateFormat dateFormat;
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


    @Override
    public void performOnFocusAction() {
        editText.performClick();
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
                date = DateUtils.Companion.databaseDateFormat().parse(data);
            } catch (ParseException e) {
                Timber.w(e);
            }

            if (date == null)
                try {
                    if (DateUtils.Companion.dateHasNoSeconds(data))
                        date = DateUtils.Companion.databaseDateFormatNoSeconds().parse(data);
                    else
                        date = DateUtils.Companion.databaseDateFormatNoMillis().parse(data);
                } catch (ParseException e) {
                    Timber.e(e);
                }

            data = DateUtils.Companion.dateTimeFormat().format(date);
        } else {
            editText.setText("");
        }
        editText.setText(data);
    }

    public void setWarning(String msg) {
        inputLayout.setErrorTextAppearance(R.style.warning_appearance);
        inputLayout.setError(msg);
    }

    public void setError(String msg) {
        inputLayout.setErrorTextAppearance(R.style.error_appearance);
        inputLayout.setError(msg);
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
        inputLayout = findViewById(R.id.inputLayout);
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
        showCustomCalendar(view);
    }

    private void showCustomCalendar(View view) {

        DatePickerUtils.Companion.getDatePickerDialog(getContext(), label, date, allowFutureDates,
                new OnDatePickerClickListener() {
                    @Override
                    public void onNegativeClick() {
                        editText.setText(null);
                        listener.onDateSelected(null);
                    }

                    @Override
                    public void onPositiveClick(DatePicker datePicker) {
                        selectedCalendar.set(Calendar.YEAR, datePicker.getYear());
                        selectedCalendar.set(Calendar.MONTH, datePicker.getMonth());
                        selectedCalendar.set(Calendar.DAY_OF_MONTH, datePicker.getDayOfMonth());
                        showTimePicker(view);
                    }
                }).show();
    }

    private void showTimePicker(View view) {
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
            nextFocus(view);
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
