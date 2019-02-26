package org.dhis2.utils.custom_views;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.AttributeSet;

import org.dhis2.R;
import org.dhis2.utils.DateUtils;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import androidx.databinding.DataBindingUtil;
import timber.log.Timber;

/**
 * QUADRAM. Created by frodriguez on 1/15/2018.
 */
@SuppressWarnings("squid:MaximumInheritanceDepth")
public class DateView extends GlobalDateView {

    private Calendar selectedCalendar;
    DatePickerDialog dateDialog;
    private boolean allowFutureDates;

    public DateView(Context context) {
        super(context);
        init(context);
    }

    public DateView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public DateView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void setLayout() {
        if (isBgTransparent)
            binding = DataBindingUtil.inflate(inflater, R.layout.date_time_view, this, true);
        else
            binding = DataBindingUtil.inflate(inflater, R.layout.date_time_view_accent, this, true);

        selectedCalendar = Calendar.getInstance();
        setUpEditText();
    }

    public void setAllowFutureDates(boolean allowFutureDates) {
        this.allowFutureDates = allowFutureDates;
    }

    public void initData(String data) {
        if (data != null) {
            date = null;
            data = data.replace("'", ""); //TODO: Check why it is happening
            if (data.length() == 10) //has format yyyy-MM-dd
                try {
                    date = DateUtils.uiDateFormat().parse(data);
                } catch (ParseException e) {
                    Timber.e(e);
                }
            else
                try {
                    date = DateUtils.databaseDateFormat().parse(data);
                    data = DateUtils.uiDateFormat().format(date);
                } catch (ParseException e) {
                    Timber.e(e);
                }


        } else {
            editText.setText("");
        }
        editText.setText(data);
    }

    public void onClick() {
        Calendar c = Calendar.getInstance();
        if (date != null)
            c.setTime(date);
        dateDialog = setUpDatePickerDialog(date, selectedCalendar, allowFutureDates,
                (datePicker, year1, month1, day1) -> {
                    selectedCalendar.set(Calendar.YEAR, year1);
                    selectedCalendar.set(Calendar.MONTH, month1);
                    selectedCalendar.set(Calendar.DAY_OF_MONTH, day1);
                    selectedCalendar.set(Calendar.HOUR_OF_DAY, c.get(Calendar.HOUR_OF_DAY));
                    selectedCalendar.set(Calendar.MINUTE, c.get(Calendar.MINUTE));
                    Date selectedDate = selectedCalendar.getTime();
                    String result = DateUtils.uiDateFormat().format(selectedDate);
                    editText.setText(result);
                    listener.onDateSelected(selectedDate);
                });
        dateDialog.setTitle(label);
        dateDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getContext().getString(R.string.date_dialog_clear), (dialog, which) -> {
            editText.setText(null);
            listener.onDateSelected(null);
        });

        dateDialog.show();
    }
}
