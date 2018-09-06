package org.dhis2.data.forms.section.viewmodels.date;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;

import java.util.Calendar;
import java.util.Date;

public class DatePickerDialogFragment extends DialogFragment {
    private static final String TAG = DatePickerDialogFragment.class.getSimpleName();
    private static final String ARG_ALLOW_DATES_IN_FUTURE = "arg:allowDatesInFuture";

    @Nullable
    private FormattedOnDateSetListener onDateSetListener;

    public static DatePickerDialogFragment create(boolean allowDatesInFuture) {
        Bundle arguments = new Bundle();
        arguments.putBoolean(ARG_ALLOW_DATES_IN_FUTURE, allowDatesInFuture);

        DatePickerDialogFragment fragment = new DatePickerDialogFragment();
        fragment.setArguments(arguments);

        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@NonNull Bundle savedInstanceState) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getContext(), (view, year, month, dayOfMonth) -> {
            Calendar chosenDate = Calendar.getInstance();
            chosenDate.set(year, month, dayOfMonth);
            if (onDateSetListener != null) {
                onDateSetListener.onDateSet(chosenDate.getTime());
            }
        }, calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));

        if (!isAllowDatesInFuture()) {
            datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        }

        return datePickerDialog;
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

    /**
     * The listener used to indicate the user has finished selecting a date.
     */
    public interface FormattedOnDateSetListener {
        /**
         * @param date the date in the correct simple fate format
         */
        void onDateSet(@NonNull Date date);
    }
}