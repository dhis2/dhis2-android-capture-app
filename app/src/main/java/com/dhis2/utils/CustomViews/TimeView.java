package com.dhis2.utils.CustomViews;

import android.app.TimePickerDialog;
import android.content.Context;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.dhis2.R;
import com.dhis2.databinding.DateTimeViewBinding;

import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeModel;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by frodriguez on 1/15/2018.
 */

public class TimeView extends RelativeLayout implements View.OnClickListener {

    private EditText time;
    private DateTimeViewBinding binding;

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

    private void init(Context context){
        LayoutInflater inflater = LayoutInflater.from(context);
        binding = DateTimeViewBinding.inflate(inflater, this, true);
        time = findViewById(R.id.button);
        time.setOnFocusChangeListener(this::onFocusChanged);
        time.setOnClickListener(this::onClick);

    }

    public void setAttribute(TrackedEntityAttributeModel attribute){
        binding.setAttribute(attribute);
    }

    public void setLabel(String label){
        binding.setLabel(label);
    }

    private void onFocusChanged(View view, boolean b) {
        if(b)
            onClick(view);
    }

    @Override
    public void onClick(View view) {
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        boolean is24HourFormat = DateFormat.is24HourFormat(getContext());
        SimpleDateFormat twentyFourHourFormat = new SimpleDateFormat("HH:mm");
        SimpleDateFormat twelveHourFormat = new SimpleDateFormat("hh:mm a");

        TimePickerDialog dialog = new TimePickerDialog(getContext(), (timePicker, hourOfDay, minutes) -> {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            calendar.set(Calendar.MINUTE, minutes);
            String calendarTime;

            if(is24HourFormat) {
                calendarTime = twentyFourHourFormat.format(calendar.getTime());
                time.setText(calendarTime);
            }
            else {
                calendarTime = twelveHourFormat.format(calendar.getTime());
                time.setText(calendarTime);
            }
        }, hour, minute, is24HourFormat);
        dialog.show();
    }
}
