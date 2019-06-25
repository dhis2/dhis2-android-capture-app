package org.dhis2.utils;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import org.dhis2.R;
import org.dhis2.databinding.WidgetDatepickerBinding;

import java.util.Calendar;
import java.util.Date;

public class DatePickerUtils {

    public static Dialog getDatePickerDialog(@NonNull Context context,
                                             @NonNull OnDatePickerClickListener buttonListener) {

        Calendar c = Calendar.getInstance();

        return buildDialog(context, c, null, true, buttonListener);
    }

    public static Dialog getDatePickerDialog(@NonNull Context context,
                                             @Nullable String title,
                                             @Nullable Date currentDate,
                                             boolean allowFutureDates,
                                             @NonNull OnDatePickerClickListener buttonListener) {


        Calendar c = Calendar.getInstance();
        if (currentDate != null)
            c.setTime(currentDate);


        return buildDialog(context, c, title, allowFutureDates, buttonListener);
    }

    public static Dialog getDatePickerDialog(@NonNull Context context,
                                             @Nullable String title,
                                             @Nullable Date currentDate,
                                             boolean futureOnly,
                                             Date maxDate,
                                             Date minDate,
                                             @NonNull OnDatePickerClickListener buttonListener) {


        Calendar c = Calendar.getInstance();
        if (currentDate != null)
            c.setTime(currentDate);
        if(futureOnly)
            c.add(Calendar.DAY_OF_YEAR, 1);

        return buildDialog(context, c, title, true, buttonListener);
    }

    private static Dialog buildDialog(Context context, Calendar c, String title,
                                      boolean allowFutureDates,
                                      OnDatePickerClickListener buttonListener) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        WidgetDatepickerBinding widgetBinding = WidgetDatepickerBinding.inflate(layoutInflater);
        final DatePicker datePicker = widgetBinding.widgetDatepicker;
        final DatePicker calendarPicker = widgetBinding.widgetDatepickerCalendar;

        datePicker.updateDate(
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH));

        calendarPicker.updateDate(
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH));

        if (!allowFutureDates) {
            datePicker.setMaxDate(System.currentTimeMillis());
            calendarPicker.setMaxDate(System.currentTimeMillis());
        }

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context, R.style.DatePickerTheme);
        if (title != null)
            alertDialog.setTitle(title);

        alertDialog.setView(widgetBinding.getRoot());
        Dialog dialog = alertDialog.create();

        widgetBinding.changeCalendarButton.setOnClickListener(calendarButton -> {
            datePicker.setVisibility(datePicker.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
            calendarPicker.setVisibility(datePicker.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
        });
        widgetBinding.clearButton.setOnClickListener(clearButton -> {
            buttonListener.onNegativeClick();
            dialog.dismiss();
        });
        widgetBinding.acceptButton.setOnClickListener(acceptButton -> {
            buttonListener.onPositiveClick(datePicker.getVisibility() == View.VISIBLE ? datePicker : calendarPicker);
            dialog.dismiss();
        });

        return dialog;
    }


    public interface OnDatePickerClickListener {
        void onNegativeClick();

        void onPositiveClick(DatePicker datePicker);
    }

}
