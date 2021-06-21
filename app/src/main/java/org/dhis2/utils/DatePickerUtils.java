package org.dhis2.utils;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import org.dhis2.App;
import org.dhis2.R;
import org.dhis2.data.prefs.Preference;
import org.dhis2.data.prefs.PreferenceProvider;
import org.dhis2.databinding.WidgetDatepickerBinding;

import java.util.Calendar;
import java.util.Date;

public class DatePickerUtils {

    static PreferenceProvider preferences;

    private static boolean showDatePicker;

    public static Dialog getDatePickerDialog(@NonNull Context context,
                                             @NonNull OnDatePickerClickListener buttonListener) {

        Calendar c = Calendar.getInstance();

        return buildDialog(context, c, null, null, null, true, false, buttonListener);
    }

    public static Dialog getDatePickerDialog(@NonNull Context context,
                                             @Nullable String title,
                                             @Nullable Date currentDate,
                                             boolean allowFutureDates,
                                             @NonNull OnDatePickerClickListener buttonListener) {

        Calendar c = Calendar.getInstance();
        if (currentDate != null)
            c.setTime(currentDate);


        return buildDialog(context, c, title, null, null, allowFutureDates, false, buttonListener);
    }

    public static Dialog getDatePickerDialog(@NonNull Context context,
                                             @Nullable String title,
                                             @Nullable Date currentDate,
                                             @Nullable Date minDate,
                                             @Nullable Date maxDate,
                                             @Nullable Integer eventScheduleInterval,
                                             @NonNull OnDatePickerClickListener buttonListener) {


        Calendar c = Calendar.getInstance();
        if (currentDate != null)
            c.setTime(currentDate);
        if (eventScheduleInterval != null && eventScheduleInterval >= 0) {
            c.add(Calendar.DAY_OF_YEAR, eventScheduleInterval);
        }

        return buildDialog(context, c, title, minDate, maxDate, true, false, buttonListener);
    }

    public static Dialog getDatePickerDialog(@NonNull Context context,
                                             @Nullable String title,
                                             @Nullable Date currentDate,
                                             @Nullable Date minDate,
                                             @Nullable Date maxDate,
                                             boolean allowFutureDates,
                                             @NonNull OnDatePickerClickListener buttonListener) {


        Calendar c = Calendar.getInstance();
        if (currentDate != null)
            c.setTime(currentDate);


        return buildDialog(context, c, title, minDate, maxDate, allowFutureDates, false, buttonListener);
    }

    public static Dialog getDatePickerDialog(@NonNull Context context,
                                             @Nullable String title,
                                             @Nullable Date currentDate,
                                             @Nullable Date minDate,
                                             @Nullable Date maxDate,
                                             boolean allowFutureDates,
                                             boolean fromOtherPeriods,
                                             @NonNull OnDatePickerClickListener buttonListener) {


        Calendar c = Calendar.getInstance();
        if (currentDate != null)
            c.setTime(currentDate);


        return buildDialog(context, c, title, minDate, maxDate, allowFutureDates, fromOtherPeriods, buttonListener);
    }

    private static Dialog buildDialog(Context context,
                                      Calendar c,
                                      String title,
                                      @Nullable Date minDate,
                                      @Nullable Date maxDate,
                                      boolean allowFutureDates,
                                      boolean fromOtherPeriods,
                                      OnDatePickerClickListener buttonListener) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        WidgetDatepickerBinding widgetBinding = WidgetDatepickerBinding.inflate(layoutInflater);
        final DatePicker datePicker = widgetBinding.widgetDatepicker;
        final DatePicker calendarPicker = widgetBinding.widgetDatepickerCalendar;

        preferences = ((App) datePicker.getContext().getApplicationContext()).appComponent().preferenceProvider();

        if (!preferences.contains(Preference.DATE_PICKER)) {
            preferences.setValue(Preference.DATE_PICKER, true);
        }

        showDatePicker = preferences.getBoolean(Preference.DATE_PICKER, false);

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
        if (minDate != null) {
            datePicker.setMinDate(minDate.getTime());
            calendarPicker.setMinDate(minDate.getTime());
        }
        if (maxDate != null ) {
            datePicker.setMaxDate(maxDate.getTime());
            calendarPicker.setMaxDate(maxDate.getTime());
        }

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context, R.style.DatePickerTheme);
        if (title != null)
            alertDialog.setTitle(title);

        if (fromOtherPeriods) {
            widgetBinding.clearButton.setText(context.getString(R.string.sectionSelectorNext));
        }

        alertDialog.setView(widgetBinding.getRoot());
        if (showDatePicker) {
            calendarPicker.setVisibility(View.GONE);
            datePicker.setVisibility(View.VISIBLE);
        } else {
            datePicker.setVisibility(View.GONE);
            calendarPicker.setVisibility(View.VISIBLE);
        }
        Dialog dialog = alertDialog.create();

        widgetBinding.changeCalendarButton.setOnClickListener(calendarButton -> {
            if (preferences.getBoolean(Preference.DATE_PICKER, false)) {
                preferences.setValue(Preference.DATE_PICKER, false);
                calendarPicker.setVisibility(View.VISIBLE);
                datePicker.setVisibility(View.GONE);
            } else {
                preferences.setValue(Preference.DATE_PICKER, true);
                calendarPicker.setVisibility(View.GONE);
                datePicker.setVisibility(View.VISIBLE);
            }
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
