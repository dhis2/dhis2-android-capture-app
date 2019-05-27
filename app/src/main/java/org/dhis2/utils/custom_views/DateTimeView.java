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
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.dhis2.BR;
import org.dhis2.R;
import org.dhis2.data.forms.dataentry.fields.datetime.OnDateSelected;
import org.dhis2.databinding.CustomCellViewBinding;
import org.dhis2.databinding.DateTimeViewBinding;
import org.dhis2.usescases.datasets.dataSetTable.dataSetSection.DataSetTableAdapter;
import org.dhis2.utils.DateUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import androidx.databinding.DataBindingUtil;
import androidx.databinding.ObservableField;
import androidx.databinding.ViewDataBinding;
import timber.log.Timber;

/**
 * QUADRAM. Created by frodriguez on 1/15/2018.
 */

public class DateTimeView extends FieldLayout implements View.OnClickListener, View.OnFocusChangeListener {

    private TextInputEditText editText;
    private TextInputLayout inputLayout;
    private ViewDataBinding binding;

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
        this.label = label;
        binding.setVariable(BR.label, label);
        binding.executePendingBindings();
    }

    public void setDescription(String description) {
        binding.setVariable(BR.description, description);
        binding.executePendingBindings();
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

    public void setCellLayout(ObservableField<DataSetTableAdapter.TableScale> tableScale) {
        binding = DataBindingUtil.inflate(inflater, R.layout.custom_cell_view, this, true);
        ((CustomCellViewBinding) binding).setTableScale(tableScale);
        editText = findViewById(R.id.inputEditText);
        selectedCalendar = Calendar.getInstance();
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
            editText.setText(null);
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
                    editText.setText(null);
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
            String result = dateFormat.format(selectedDate);
            editText.setText(result);
            listener.onDateSelected(selectedDate);
            nextFocus(view);
        },
                hour,
                minute,
                is24HourFormat);
        dialog.setTitle(label);
        dialog.show();
    }

    public TextInputEditText getEditText() {
        return editText;
    }

    public void setEditable(Boolean editable) {
        editText.setEnabled(editable);
    }
}
