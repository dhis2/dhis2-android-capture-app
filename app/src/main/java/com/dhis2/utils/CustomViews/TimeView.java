package com.dhis2.utils.CustomViews;

import android.app.TimePickerDialog;
import android.content.Context;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import com.dhis2.R;
import com.dhis2.data.forms.dataentry.fields.datetime.OnDateSelected;
import com.dhis2.databinding.DateTimeViewBinding;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by frodriguez on 1/15/2018.
 */

public class TimeView extends RelativeLayout implements View.OnClickListener {

    private TextInputLayout time;
    private TextInputEditText editText;
    private DateTimeViewBinding binding;
    private LayoutInflater inflater;
    private boolean isBgTransparent;
    private OnDateSelected listener;

    public TimeView(Context context) {
        super(context);
        init(context);
    }

    public TimeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public TimeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        inflater = LayoutInflater.from(context);


    }

    private void setLayout() {
        binding = DateTimeViewBinding.inflate(inflater, this, true);
        time = findViewById(R.id.button);
        editText = findViewById(R.id.inputEditText);
        time.setOnFocusChangeListener(this::onFocusChanged);
        time.setOnClickListener(this::onClick);
    }

    public void setIsBgTransparent(boolean isBgTransparent) {
        this.isBgTransparent = isBgTransparent;
        setLayout();
    }

    public void setLabel(String label) {
        binding.setLabel(label);
        binding.executePendingBindings();
    }

    public void initData(String data) {
        editText.setText(data);
    }

    public void setDateListener(OnDateSelected listener) {
        this.listener = listener;
    }

    private void onFocusChanged(View view, boolean b) {
        if (b)
            onClick(view);
    }

    @Override
    public void onClick(View view) {
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        boolean is24HourFormat = DateFormat.is24HourFormat(getContext());
        SimpleDateFormat twentyFourHourFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        SimpleDateFormat twelveHourFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());

        TimePickerDialog dialog = new TimePickerDialog(getContext(), (timePicker, hourOfDay, minutes) -> {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            calendar.set(Calendar.MINUTE, minutes);
            Date selectedDate = calendar.getTime();
            String calendarTime;

            if (is24HourFormat) {
                calendarTime = twentyFourHourFormat.format(selectedDate);
                editText.setText(calendarTime);
            } else {
                calendarTime = twelveHourFormat.format(selectedDate);
                editText.setText(calendarTime);
            }
            listener.onDateSelected(selectedDate);
        }, hour, minute, is24HourFormat);
        dialog.setTitle(binding.getLabel());
        dialog.show();
    }
}
