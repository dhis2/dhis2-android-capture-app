package org.dhis2.utils.customviews;

import android.content.Context;
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
import org.dhis2.utils.DatePickerUtils;
import org.dhis2.utils.DateUtils;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

import timber.log.Timber;

import static android.text.TextUtils.isEmpty;

/**
 * QUADRAM. Created by frodriguez on 1/15/2018.
 */


public class AgeView extends FieldLayout implements View.OnClickListener {

    private TextInputEditText date;
    private TextInputEditText day;
    private TextInputEditText month;
    private TextInputEditText year;
    private ViewDataBinding binding;

    private Calendar selectedCalendar;
    private DateFormat dateFormat;

    private OnAgeSet listener;
    private String label;
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
        clearValues();
        date.requestFocus();
    }

    private void onFocusChanged(View view, boolean b) {
        if (b)
            onClick(view);
    }

    @Override
    public void onClick(View view) {
        activate();
        switch (view.getId()) {
            case R.id.date_picker:
                showCustomCalendar(view);
                break;
            case R.id.input_days:
            case R.id.input_month:
            case R.id.input_year:
                getYearsDialog().show();
                break;
        }

    }

    private void showCustomCalendar(View view) {

        DatePickerUtils.getDatePickerDialog(getContext(), label, selectedCalendar.getTime(), true, new DatePickerUtils.OnDatePickerClickListener() {
            @Override
            public void onNegativeClick() {
                listener.onAgeSet(null);
                clearValues();
            }

            @Override
            public void onPositiveClick(DatePicker datePicker) {
                handleDateInput(view, datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());
            }
        }).show();
    }

    private AlertDialog getYearsDialog() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_age, null);
        TextInputEditText yearPicker = view.findViewById(R.id.input_year);
        TextInputEditText monthPicker = view.findViewById(R.id.input_month);
        TextInputEditText dayPicker = view.findViewById(R.id.input_days);
        yearPicker.setText(year.getText());
        monthPicker.setText(month.getText());
        dayPicker.setText(day.getText());

        return new AlertDialog.Builder(getContext(), R.style.CustomDialog)
                .setView(view)
                .setPositiveButton(R.string.action_accept, (dialog, which) -> handleSingleInputs(
                        isEmpty(yearPicker.getText().toString()) ? 0 : -Integer.valueOf(yearPicker.getText().toString()),
                        isEmpty(monthPicker.getText().toString()) ? 0 : -Integer.valueOf(monthPicker.getText().toString()),
                        isEmpty(dayPicker.getText().toString()) ? 0 : -Integer.valueOf(dayPicker.getText().toString())))
                .setNegativeButton(R.string.clear, (dialog, which) -> {
                    clearValues();
                    listener.onAgeSet(null);
                })
                .create();
    }

    public void setAgeChangedListener(OnAgeSet listener) {
        this.listener = listener;
    }

    protected void handleSingleInputs(int year, int month, int day) {

        Calendar calendar = Calendar.getInstance();

        calendar.add(Calendar.DAY_OF_MONTH, day);
        calendar.add(Calendar.MONTH, month);
        calendar.add(Calendar.YEAR, year);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        String birthDate = DateUtils.uiDateFormat().format(calendar.getTime());

        int[] dateDifference = DateUtils.getDifference(calendar.getTime(), Calendar.getInstance().getTime());
        this.day.setText(String.valueOf(dateDifference[2]));
        this.month.setText(String.valueOf(dateDifference[1]));
        this.year.setText(String.valueOf(dateDifference[0]));

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
        day.setText(String.valueOf(dateDifference[2]));
        month.setText(String.valueOf(dateDifference[1]));
        year.setText(String.valueOf(dateDifference[0]));

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

        if (initialDate != null) {
            String result = dateFormat.format(initialDate);
            selectedCalendar.setTime(initialDate);

            int[] dateDifference = DateUtils.getDifference(initialDate, Calendar.getInstance().getTime());
            day.setText(String.valueOf(dateDifference[2]));
            month.setText(String.valueOf(dateDifference[1]));
            year.setText(String.valueOf(dateDifference[0]));

            date.setText(result);
        }
    }


    public void setIsBgTransparent(Boolean isBgTransparent) {

        if (!isBgTransparent)
            binding = AgeCustomViewAccentBinding.inflate(inflater, this, true);
        else
            binding = AgeCustomViewBinding.inflate(inflater, this, true);

        inputLayout = findViewById(R.id.inputLayout);
        date = findViewById(R.id.date_picker);
        day = findViewById(R.id.input_days);
        month = findViewById(R.id.input_month);
        year = findViewById(R.id.input_year);
        selectedCalendar = Calendar.getInstance();
        dateFormat = DateUtils.uiDateFormat();

        date.setFocusable(false); //Makes editText not editable
        date.setClickable(true);//  but clickable
        date.setOnFocusChangeListener(this::onFocusChanged);
        date.setOnClickListener(this);

        day.setFocusable(false);
        day.setClickable(true);
        month.setFocusable(false);
        month.setClickable(true);
        year.setFocusable(false);
        year.setClickable(true);

        day.setFilters(new InputFilter[]{new InputFilter.LengthFilter(2)});
        month.setFilters(new InputFilter[]{new InputFilter.LengthFilter(2)});
        year.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});

        day.setOnEditorActionListener((v, actionId, event) -> {
            nextFocus(v);
            return true;
        });

        month.setOnEditorActionListener((v, actionId, event) -> {
            day.requestFocus();
            return true;
        });

        year.setOnEditorActionListener((v, actionId, event) -> {
            month.requestFocus();
            return true;
        });
        day.setOnClickListener(this);
        month.setOnClickListener(this);
        year.setOnClickListener(this);
    }

    public void setEditable(Boolean editable) {
        date.setEnabled(editable);
        day.setEnabled(editable);
        month.setEnabled(editable);
        year.setEnabled(editable);
    }

    public void clearValues() {
        date.setText(null);
        day.setText(null);
        month.setText(null);
        year.setText(null);
    }

    public OnClickListener getClickListener() {
        return this;
    }

    public interface OnAgeSet {
        void onAgeSet(Date ageDate);
    }
}
