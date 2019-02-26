package org.dhis2.utils.custom_views;

import android.app.TimePickerDialog;
import android.content.Context;
import android.text.format.DateFormat;
import android.util.AttributeSet;

import org.dhis2.R;
import org.dhis2.utils.DateUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import androidx.databinding.DataBindingUtil;
import timber.log.Timber;

/**
 * QUADRAM. Created by frodriguez on 1/15/2018.
 */
@SuppressWarnings("squid:MaximumInheritanceDepth")
public class TimeView extends GlobalDateView {

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

    public void setLayout() {
        binding = DataBindingUtil.inflate(inflater, R.layout.time_view, this, true);
        setUpEditText();
    }

    public void initData(String data) {
        if (data != null) {
            date = null;
            try {
                date = DateUtils.timeFormat().parse(data);
            } catch (ParseException e) {
                Timber.e(e);
            }


            data = DateUtils.timeFormat().format(date);
        }
        editText.setText(data);
    }

    public void onClick() {
        final Calendar c = Calendar.getInstance();
        if (date != null)
            c.setTime(date);

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
        dialog.setTitle(label);
        dialog.show();
    }
}
