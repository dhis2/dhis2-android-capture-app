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

    private TextInputEditText date;
    private TextInputEditText dayEditText;
    private TextInputEditText monthEditText;
    private TextInputEditText yearEditText;
    private ViewDataBinding viewDataBinding;

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
        date.performClick();
    }

    public void setLabel(String label, String description) {
        this.label = label;
        if (viewDataBinding instanceof AgeCustomViewAccentBinding) {
            ((AgeCustomViewAccentBinding) viewDataBinding).setLabel(label);
            ((AgeCustomViewAccentBinding) viewDataBinding).setDescription(description);
        } else {
            ((AgeCustomViewBinding) viewDataBinding).setLabel(label);
            ((AgeCustomViewBinding) viewDataBinding).setDescription(description);
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
        showNativeCalendar(view);
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
        View datePickerView = layoutInflater.inflate(R.layout.widget_datepicker, null);
        final DatePicker datePicker = datePickerView.findViewById(R.id.widget_datepicker);

        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        datePicker.updateDate(year, month, day);
        datePicker.setMaxDate(c.getTimeInMillis());

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext(), R.style.DatePickerTheme)
                .setTitle(label)
                .setPositiveButton(R.string.action_accept, (dialog, which) -> handleDateInput(view, datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth()))
                .setNeutralButton(getContext().getResources().getString(R.string.change_calendar), (dialog, which) -> showNativeCalendar(view));

        alertDialog.setView(datePickerView);
        Dialog dialog = alertDialog.create();
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
                case R.id.input_month:
                case R.id.input_year:
                    handleSingleInputs();
                    break;
                default:
                    break;
            }
    }

    protected void handleSingleInputs() {

        Calendar calendar = Calendar.getInstance();

        calendar.add(Calendar.DAY_OF_MONTH, isEmpty(dayEditText.getText().toString()) ? 0 : -Integer.valueOf(dayEditText.getText().toString()));
        calendar.add(Calendar.MONTH, isEmpty(monthEditText.getText().toString()) ? 0 : -Integer.valueOf(monthEditText.getText().toString()));
        calendar.add(Calendar.YEAR, isEmpty(yearEditText.getText().toString()) ? 0 : -Integer.valueOf(yearEditText.getText().toString()));
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        String birthDate = DateUtils.uiDateFormat().format(calendar.getTime());
        if (!date.getText().toString().equals(birthDate)) {
            date.setText(birthDate);
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
        dayEditText.setText(String.valueOf(dateDifference[2]));
        monthEditText.setText(String.valueOf(dateDifference[1]));
        yearEditText.setText(String.valueOf(dateDifference[0]));

        if (!result.equals(date.getText().toString())) {
            date.setText(result);
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
        dayEditText.setText(String.valueOf(dateDifference[2]));
        monthEditText.setText(String.valueOf(dateDifference[1]));
        yearEditText.setText(String.valueOf(dateDifference[0]));

        date.setText(result);
    }


    public void setIsBgTransparent(Boolean isBgTransparent) {

        if (!isBgTransparent)
            viewDataBinding = AgeCustomViewAccentBinding.inflate(inflater, this, true);
        else
            viewDataBinding = AgeCustomViewBinding.inflate(inflater, this, true);

        inputLayout = findViewById(R.id.inputLayout);
        date = findViewById(R.id.date_picker);
        dayEditText = findViewById(R.id.input_days);
        monthEditText = findViewById(R.id.input_month);
        yearEditText = findViewById(R.id.input_year);
        selectedCalendar = Calendar.getInstance();
        dateFormat = DateUtils.uiDateFormat();

        date.setFocusable(false); //Makes editText not editable
        date.setClickable(true);//  but clickable
        date.setOnFocusChangeListener(this::onFocusChanged);
        date.setOnClickListener(this);

        dayEditText.setFocusable(true);
        dayEditText.setClickable(true);
        monthEditText.setFocusable(true);
        monthEditText.setClickable(true);
        yearEditText.setFocusable(true);
        yearEditText.setClickable(true);

        dayEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(2)});
        monthEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(2)});
        yearEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});

        dayEditText.setOnEditorActionListener((v, actionId, event) -> {
            nextFocus(v);
            return true;
        });

        monthEditText.setOnEditorActionListener((v, actionId, event) -> {
            dayEditText.requestFocus();
            return true;
        });

        yearEditText.setOnEditorActionListener((v, actionId, event) -> {
            monthEditText.requestFocus();
            return true;
        });
        dayEditText.setOnFocusChangeListener(this);
        monthEditText.setOnFocusChangeListener(this);
        yearEditText.setOnFocusChangeListener(this);
    }

    public void setEditable(Boolean editable) {
        date.setEnabled(editable);
        dayEditText.setEnabled(editable);
        monthEditText.setEnabled(editable);
        yearEditText.setEnabled(editable);
    }

    public interface OnAgeSet {
        void onAgeSet(Date ageDate);
    }
}
