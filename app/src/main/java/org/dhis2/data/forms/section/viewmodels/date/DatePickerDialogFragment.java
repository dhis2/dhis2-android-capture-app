package org.dhis2.data.forms.section.viewmodels.date;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import org.dhis2.R;

import java.util.Calendar;
import java.util.Date;

public class DatePickerDialogFragment extends DialogFragment {
    private static final String TAG = DatePickerDialogFragment.class.getSimpleName();
    private static final String ARG_ALLOW_DATES_IN_FUTURE = "arg:allowDatesInFuture";
    private static final String ARG_TITLE = "arg:title";
    private static final String ARG_FROM_OTHER_PERIOD = "arg:fromOtherPeriod";

    @Nullable
    private FormattedOnDateSetListener onDateSetListener;
    private Date openingDate;
    private Date closingDate;
    private Date initialDate;
    private Context context;

    Dialog dialog;

    public static DatePickerDialogFragment create(boolean allowDatesInFuture) {
        Bundle arguments = new Bundle();
        arguments.putBoolean(ARG_ALLOW_DATES_IN_FUTURE, allowDatesInFuture);
        arguments.putString(ARG_TITLE, null);

        DatePickerDialogFragment fragment = new DatePickerDialogFragment();
        fragment.setArguments(arguments);

        return fragment;
    }

    public static DatePickerDialogFragment create(boolean allowDatesInFuture, String title, boolean fromOtherPeriod) {
        Bundle arguments = new Bundle();
        arguments.putBoolean(ARG_ALLOW_DATES_IN_FUTURE, allowDatesInFuture);
        arguments.putString(ARG_TITLE, title);
        arguments.putBoolean(ARG_FROM_OTHER_PERIOD, fromOtherPeriod);
        DatePickerDialogFragment fragment = new DatePickerDialogFragment();
        fragment.setArguments(arguments);

        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@NonNull Bundle savedInstanceState) {
        return showCustomCalendar();
    }

    private DatePickerDialog showNativeCalendar() {
        Calendar calendar = Calendar.getInstance();

        if (initialDate != null) calendar.setTime(initialDate);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                context, (view, year, month, dayOfMonth) -> {
            Calendar chosenDate = Calendar.getInstance();
            chosenDate.set(year, month, dayOfMonth);
            if (onDateSetListener != null) {
                onDateSetListener.onDateSet(chosenDate.getTime());
            }
        }, calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));

        if (openingDate != null)
            datePickerDialog.getDatePicker().setMinDate(openingDate.getTime());

        if (closingDate == null && !isAllowDatesInFuture()) {
            datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        } else if (closingDate != null && !isAllowDatesInFuture()) {
            if (closingDate.before(new Date(System.currentTimeMillis()))) {
                datePickerDialog.getDatePicker().setMaxDate(closingDate.getTime());
            } else {
                datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
            }
        } else if (closingDate != null && isAllowDatesInFuture()) {
            datePickerDialog.getDatePicker().setMaxDate(closingDate.getTime());
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            datePickerDialog.setButton(DialogInterface.BUTTON_NEUTRAL,
                    context.getResources().getString(R.string.change_calendar), (dialog, which) -> {
                        datePickerDialog.dismiss();
                        showCustomCalendar().show();
                    });
        }
        return datePickerDialog;
    }

    private Dialog showCustomCalendar() {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View datePickerView = layoutInflater.inflate(R.layout.widget_datepicker, null);
        final DatePicker datePicker = datePickerView.findViewById(R.id.widget_datepicker);
        final ImageButton changeCalendarButton = datePickerView.findViewById(R.id.changeCalendarButton);
        final Button clearButton = datePickerView.findViewById(R.id.clearButton);
        final Button acceptButton = datePickerView.findViewById(R.id.acceptButton);
        if (fromOtherPeriod())
            clearButton.setText(context.getString(R.string.sectionSelectorNext));

        Calendar c = Calendar.getInstance();
        if (initialDate != null) c.setTime(initialDate);

        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        datePicker.updateDate(year, month, day);

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context, R.style.DatePickerTheme);

        changeCalendarButton.setOnClickListener(view -> {
            showNativeCalendar().show();
            dialog.dismiss();
        });
        clearButton.setOnClickListener(view -> {
            if (onDateSetListener != null)
                onDateSetListener.onClearDate();
            dialog.dismiss();
        });

        acceptButton.setOnClickListener(view -> {
            Calendar chosenDate = Calendar.getInstance();
            chosenDate.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());
            if (onDateSetListener != null) {
                onDateSetListener.onDateSet(chosenDate.getTime());
            }
            dialog.dismiss();
        });

        if (openingDate != null)
            datePicker.setMinDate(openingDate.getTime());

        if (closingDate == null && !isAllowDatesInFuture()) {
            datePicker.setMaxDate(System.currentTimeMillis());
        } else if (closingDate != null && !isAllowDatesInFuture()) {
            if (closingDate.before(new Date(System.currentTimeMillis()))) {
                datePicker.setMaxDate(closingDate.getTime());
            } else {
                datePicker.setMaxDate(System.currentTimeMillis());
            }
        } else if (closingDate != null && isAllowDatesInFuture()) {
            datePicker.setMaxDate(closingDate.getTime());
        }

        alertDialog.setView(datePickerView);
        if (getArguments().getString(ARG_TITLE) != null)
            alertDialog.setTitle(getArguments().getString(ARG_TITLE));

        dialog = alertDialog.create();
        return dialog;
    }

    public void show(@NonNull FragmentManager fragmentManager) {
        show(fragmentManager, TAG);
    }

    public void setFormattedOnDateSetListener(@Nullable FormattedOnDateSetListener listener) {
        this.onDateSetListener = listener;
    }

    private boolean isAllowDatesInFuture() {
        return getArguments().getBoolean(ARG_ALLOW_DATES_IN_FUTURE, false);
    }

    private boolean fromOtherPeriod() {
        return getArguments().getBoolean(ARG_FROM_OTHER_PERIOD, false);
    }

    public void setOpeningClosingDates(Date openingDate, Date closingDate) {
        this.openingDate = openingDate;
        this.closingDate = closingDate;
    }

    public void setInitialDate(Date initialDate) {
        this.initialDate = initialDate;
    }

    /**
     * The listener used to indicate the user has finished selecting a date.
     */
    public interface FormattedOnDateSetListener {
        //TODO Should change names of methods cause it make no sense with the new filter

        /**
         * @param date the date in the correct simple fate format
         */
        void onDateSet(@NonNull Date date);

        void onClearDate();
    }
}