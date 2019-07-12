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
import org.dhis2.utils.DatePickerUtils;
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

    private void showCustomCalendar(View view) {

        DatePickerUtils.getDatePickerDialog(getContext(), label, date, allowFutureDates,
                new DatePickerUtils.OnDatePickerClickListener() {
                    @Override
                    public void onNegativeClick() {
                        textView.setText(null);
                        listener.onDateSelected(null);
                    }

                    @Override
                    public void onPositiveClick(DatePicker datePicker) {
                        selectedCalendar.set(Calendar.YEAR, datePicker.getYear());
                        selectedCalendar.set(Calendar.MONTH, datePicker.getMonth());
                        selectedCalendar.set(Calendar.DAY_OF_MONTH, datePicker.getDayOfMonth());
                        showTimePicker(view);
                    }
                }).show();
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
