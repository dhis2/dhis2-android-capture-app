package org.dhis2.utils.custom_views;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ObservableField;

import org.dhis2.R;
import org.dhis2.data.forms.dataentry.fields.datetime.OnDateSelected;
import org.dhis2.databinding.CustomCellViewBinding;
import org.dhis2.usescases.datasets.dataSetTable.dataSetSection.DataSetTableAdapter;
import org.dhis2.utils.DateUtils;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import timber.log.Timber;

/**
 * QUADRAM. Created by frodriguez on 1/15/2018.
 */

public class DateTimeTableView extends FieldLayout implements View.OnClickListener, View.OnFocusChangeListener {

    private TextView textView;
    private CustomCellViewBinding binding;

    private Calendar selectedCalendar;
    private OnDateSelected listener;
    private boolean allowFutureDates;
    private Date date;
    DatePickerDialog dateDialog;

    public DateTimeTableView(Context context) {
        super(context);
        init(context);
    }

    public DateTimeTableView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public DateTimeTableView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }


    @Override
    public void performOnFocusAction() {
        textView.performClick();
    }


    public void setLabel(String label) {
        this.label = label;
        binding.executePendingBindings();
    }

    public void setDescription(String description) {
       /* binding.setDescription(description);
        binding.executePendingBindings();*/
    }

    public void initData(String data) {
        if (data != null) {
            date = null;
            try {
                date = DateUtils.databaseDateFormat().parse(data);
            } catch (ParseException e) {
                Timber.w(e);
            }

            if (date == null)
                try {
                    if (DateUtils.dateHasNoSeconds(data))
                        date = DateUtils.databaseDateFormatNoSeconds().parse(data);
                    else
                        date = DateUtils.databaseDateFormatNoMillis().parse(data);
                } catch (ParseException e) {
                    Timber.e(e);
                }

            data = DateUtils.dateTimeFormat().format(date);
        } else {
            textView.setText("");
        }
        textView.setText(data);
    }

    public void setWarning(String msg) {


    }

    public void setError(String msg) {


    }

    public void setAllowFutureDates(boolean allowFutureDates) {
        this.allowFutureDates = allowFutureDates;
    }

    public void setCellLayout(ObservableField<DataSetTableAdapter.TableScale> tableScale) {
        binding = DataBindingUtil.inflate(inflater, R.layout.custom_cell_view, this, true);
        binding.setTableScale(tableScale);
        textView = findViewById(R.id.inputEditText);
        selectedCalendar = Calendar.getInstance();
        textView.setFocusable(false); //Makes editText not editable
        textView.setClickable(true);//  but clickable
        textView.setOnFocusChangeListener(this);
        textView.setOnClickListener(this);
    }

    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        if (hasFocus)
            onClick(view);
    }

    public void setDateListener(OnDateSelected listener) {
        this.listener = listener;
    }

    public void setMandatory() {
        ImageView mandatory = binding.getRoot().findViewById(R.id.ic_mandatory);
        mandatory.setVisibility(View.VISIBLE);
    }


    @Override
    public void onClick(View view) {
        showCustomCalendar(view);
    }

    private void showNativeCalendar(View view) {
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
                    showTimePicker(view);
                }),
                year,
                month,
                day);
        dateDialog.setTitle(label);
        if (!allowFutureDates) {
            dateDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        }

        dateDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getContext().getString(R.string.date_dialog_clear), (dialog, which) -> {
            textView.setText(null);
            listener.onDateSelected(null);
        });

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            dateDialog.setButton(DialogInterface.BUTTON_NEUTRAL, getContext().getResources().getString(R.string.change_calendar), (dialog, which) -> {
                dateDialog.dismiss();
                showCustomCalendar(view);
            });
        }
        dateDialog.show();
    }

    private void showCustomCalendar(View view) {
        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        View datePickerView = layoutInflater.inflate(R.layout.widget_datepicker, null);
        final DatePicker datePicker = datePickerView.findViewById(R.id.widget_datepicker);

        Calendar c = Calendar.getInstance();
        if (date != null)
            c.setTime(date);
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        datePicker.updateDate(year, month, day);

        if (!allowFutureDates) {
            datePicker.setMaxDate(System.currentTimeMillis());
        }

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext(), R.style.DatePickerTheme)
                .setTitle(label)
                .setPositiveButton(R.string.action_accept, (dialog, which) -> {
                    selectedCalendar.set(Calendar.YEAR, datePicker.getYear());
                    selectedCalendar.set(Calendar.MONTH, datePicker.getMonth());
                    selectedCalendar.set(Calendar.DAY_OF_MONTH, datePicker.getDayOfMonth());
                    showTimePicker(view);
                })
                .setNegativeButton(getContext().getString(R.string.date_dialog_clear), (dialog, which) -> {
                    textView.setText(null);
                    listener.onDateSelected(null);
                })
                .setNeutralButton(getContext().getResources().getString(R.string.change_calendar), (dialog, which) -> {
                    showNativeCalendar(view);
                });

        alertDialog.setView(datePickerView);
        Dialog dialog = alertDialog.create();
        dialog.show();
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
            String result = DateUtils.timeFormat().format(selectedDate);
            textView.setText(result);
            listener.onDateSelected(selectedDate);
            nextFocus(view);
        },
                hour,
                minute,
                is24HourFormat);
        dialog.setTitle(label);
        dialog.show();
    }

    public TextView getEditText() {
        return textView;
    }

    public void setEditable(Boolean editable) {
        textView.setEnabled(editable);
    }
}
