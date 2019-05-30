package org.dhis2.utils.custom_views;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.InputFilter;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;

import androidx.appcompat.app.AlertDialog;
import androidx.databinding.ViewDataBinding;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.dhis2.R;
import org.dhis2.databinding.AgeCustomViewAccentBinding;
import org.dhis2.databinding.AgeCustomViewBinding;
import org.dhis2.databinding.WidgetDatepickerBinding;
import org.dhis2.utils.DateUtils;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

import timber.log.Timber;

import static android.text.TextUtils.isEmpty;

/**
 * QUADRAM. Created by frodriguez on 1/15/2018.
 */


public class AgeView extends FieldLayout implements View.OnClickListener, View.OnFocusChangeListener {

    private TextInputEditText dateET;
    private TextInputEditText dayET;
    private TextInputEditText monthET;
    private TextInputEditText yearET;
    private ViewDataBinding binding;

    private Calendar selectedCalendar;
    private DateFormat dateFormat;

    private OnAgeSet listener;
    private TextInputLayout inputLayout;

    public AgeView(Context context) {
        super(context);
        init(context);
    }

    public AgeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public AgeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @Override
    public void performOnFocusAction() {
        dateET.performClick();
    }

    public void setLabel(String label, String description) {
        this.label = label;
        if (binding instanceof AgeCustomViewAccentBinding) {
            ((AgeCustomViewAccentBinding) binding).setLabel(label);
            ((AgeCustomViewAccentBinding) binding).setDescription(description);
        } else {
            ((AgeCustomViewBinding) binding).setLabel(label);
            ((AgeCustomViewBinding) binding).setDescription(description);
        }
    }

    public void setWarning(String msg) {
        inputLayout.setErrorTextAppearance(R.style.warning_appearance);
        inputLayout.setError(msg);
    }

    public void setError(String msg) {
        inputLayout.setErrorTextAppearance(R.style.error_appearance);
        inputLayout.setError(msg);
    }

    private void onFocusChanged(View view, boolean b) {
        if (b)
            onClick(view);
    }

    @Override
    public void onClick(View view) {
        showCustomCalendar(view);
    }

    private void showNativeCalendar(View view) {
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dateDialog = new DatePickerDialog(getContext(), (
                (datePicker, year1, month1, day1) -> handleDateInput(view, year1, month1, day1)),
                year,
                month,
                day);
        dateDialog.getDatePicker().setMaxDate(c.getTimeInMillis());
        dateDialog.setTitle(label);
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
        WidgetDatepickerBinding binding = WidgetDatepickerBinding.inflate(layoutInflater);
        final DatePicker datePicker = binding.widgetDatepicker;

        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        datePicker.updateDate(year, month, day);
        datePicker.setMaxDate(c.getTimeInMillis());

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext(), R.style.DatePickerTheme)
                .setTitle(label);
                /*.setPositiveButton(R.string.action_accept, (dialog, which) -> handleDateInput(view, datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth()))
                .setNeutralButton(getContext().getResources().getString(R.string.change_calendar), (dialog, which) -> showNativeCalendar(view));*/

        alertDialog.setView(binding.getRoot());
        Dialog dialog = alertDialog.create();

        binding.changeCalendarButton.setOnClickListener(calendarButton -> {
            showNativeCalendar(view);
            dialog.dismiss();
        });
        binding.clearButton.setOnClickListener(clearButton -> {
            listener.onAgeSet(null);
            dateET.setText(null);
            this.dayET.setText(null);
            this.monthET.setText(null);
            this.yearET.setText(null);
            dialog.dismiss();
        });

        binding.acceptButton.setOnClickListener(acceptButton -> {
            handleDateInput(view, datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());
            dialog.dismiss();
        });

        dialog.show();
    }

    public void setAgeChangedListener(OnAgeSet listener) {
        this.listener = listener;
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (!hasFocus)
            switch (v.getId()) {
                case R.id.input_days:
                    handleSingleInputs(true);
                case R.id.input_month:
                case R.id.input_year:
                default:
                    handleSingleInputs(false);
                    break;
            }
    }

    protected void handleSingleInputs(boolean finish) {

        Calendar calendar = Calendar.getInstance();

        calendar.add(Calendar.DAY_OF_MONTH, isEmpty(dayET.getText().toString()) ? 0 : -Integer.valueOf(dayET.getText().toString()));
        calendar.add(Calendar.MONTH, isEmpty(monthET.getText().toString()) ? 0 : -Integer.valueOf(monthET.getText().toString()));
        calendar.add(Calendar.YEAR, isEmpty(yearET.getText().toString()) ? 0 : -Integer.valueOf(yearET.getText().toString()));
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        String birthDate = DateUtils.uiDateFormat().format(calendar.getTime());
        if (!dateET.getText().toString().equals(birthDate)) {
            dateET.setText(birthDate);
            if (finish)
                listener.onAgeSet(calendar.getTime());
        }
    }

    protected void handleDateInput(View view, int year1, int month1, int day1) {
        selectedCalendar.set(Calendar.YEAR, year1);
        selectedCalendar.set(Calendar.MONTH, month1);
        selectedCalendar.set(Calendar.DAY_OF_MONTH, day1);
        selectedCalendar.set(Calendar.HOUR_OF_DAY, 0);
        selectedCalendar.set(Calendar.MINUTE, 0);
        selectedCalendar.set(Calendar.SECOND, 0);
        selectedCalendar.set(Calendar.MILLISECOND, 0);

        String result = dateFormat.format(selectedCalendar.getTime());

        int[] dateDifference = DateUtils.getDifference(selectedCalendar.getTime(), Calendar.getInstance().getTime());
        dayET.setText(String.valueOf(dateDifference[2]));
        monthET.setText(String.valueOf(dateDifference[1]));
        yearET.setText(String.valueOf(dateDifference[0]));

        if (!result.equals(dateET.getText().toString())) {
            dateET.setText(result);
            listener.onAgeSet(selectedCalendar.getTime());
            nextFocus(view);
        }
    }

    public void setInitialValue(String initialValue) {
        Date initialDate = null;
        try {
            initialDate = DateUtils.databaseDateFormat().parse(initialValue);

        } catch (Exception e) {
            Timber.e(e);
        }

        if (initialDate == null)
            try {
                initialDate = DateUtils.uiDateFormat().parse(initialValue);

            } catch (Exception e) {
                Timber.e(e);
            }

        String result = dateFormat.format(initialDate);

        int[] dateDifference = DateUtils.getDifference(initialDate, Calendar.getInstance().getTime());
        dayET.setText(String.valueOf(dateDifference[2]));
        monthET.setText(String.valueOf(dateDifference[1]));
        yearET.setText(String.valueOf(dateDifference[0]));

        dateET.setText(result);
    }


    public void setIsBgTransparent(Boolean isBgTransparent) {

        if (!isBgTransparent)
            binding = AgeCustomViewAccentBinding.inflate(inflater, this, true);
        else
            binding = AgeCustomViewBinding.inflate(inflater, this, true);

        inputLayout = findViewById(R.id.inputLayout);
        dateET = findViewById(R.id.date_picker);
        dayET = findViewById(R.id.input_days);
        monthET = findViewById(R.id.input_month);
        yearET = findViewById(R.id.input_year);
        selectedCalendar = Calendar.getInstance();
        dateFormat = DateUtils.uiDateFormat();

        dateET.setFocusable(false); //Makes editText not editable
        dateET.setClickable(true);//  but clickable
        dateET.setOnFocusChangeListener(this::onFocusChanged);
        dateET.setOnClickListener(this);

        dayET.setFocusable(true);
        dayET.setClickable(true);
        monthET.setFocusable(true);
        monthET.setClickable(true);
        yearET.setFocusable(true);
        yearET.setClickable(true);

        dayET.setFilters(new InputFilter[]{new InputFilter.LengthFilter(2)});
        monthET.setFilters(new InputFilter[]{new InputFilter.LengthFilter(2)});
        yearET.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});

        dayET.setOnEditorActionListener((v, actionId, event) -> {
            nextFocus(v);
            return true;
        });

        monthET.setOnEditorActionListener((v, actionId, event) -> {
            dayET.requestFocus();
            return true;
        });

        yearET.setOnEditorActionListener((v, actionId, event) -> {
            monthET.requestFocus();
            return true;
        });
        dayET.setOnFocusChangeListener(this);
        monthET.setOnFocusChangeListener(this);
        yearET.setOnFocusChangeListener(this);
    }

    public void setEditable(Boolean editable) {
        dateET.setEnabled(editable);
        dayET.setEnabled(editable);
        monthET.setEnabled(editable);
        yearET.setEnabled(editable);
    }

    public interface OnAgeSet {
        void onAgeSet(Date ageDate);
    }
}