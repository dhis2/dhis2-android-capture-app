package org.dhis2.utils.custom_views;

import android.app.DatePickerDialog;
import android.content.Context;
import android.text.InputFilter;
import android.util.AttributeSet;
import android.view.View;

import com.google.android.material.textfield.TextInputEditText;

import org.dhis2.R;
import org.dhis2.databinding.AgeCustomViewAccentBinding;
import org.dhis2.databinding.AgeCustomViewBinding;
import org.dhis2.utils.DateUtils;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

import androidx.databinding.ViewDataBinding;
import timber.log.Timber;

import static android.text.TextUtils.isEmpty;

/**
 * QUADRAM. Created by frodriguez on 1/15/2018.
 */


public class AgeView extends FieldLayout implements View.OnClickListener, View.OnFocusChangeListener {

    private TextInputEditText dateTIET;
    private TextInputEditText dayTIET;
    private TextInputEditText monthTIET;
    private TextInputEditText yearTIET;
    private ViewDataBinding binding;

    private Calendar selectedCalendar;
    private DateFormat dateFormat;

    private OnAgeSet listener;
    private String label;

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
        dateTIET.performClick();
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

    public void setWarningOrError(String warningOrError) {
        dateTIET.setError(warningOrError);
    }

    private void onFocusChanged(View view, boolean b) {
        if (b)
            onClick(view);
    }

    @Override
    public void onClick(View view) {
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dateDialog = new DatePickerDialog(getContext(), (
                (datePicker, year1, month1, day1) -> handleDateInput(year1, month1, day1)),
                year,
                month,
                day);
        dateDialog.getDatePicker().setMaxDate(c.getTimeInMillis());
        dateDialog.setTitle(label);
        dateDialog.show();
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

    private void handleSingleInputs() {

        Calendar calendar = Calendar.getInstance();

        calendar.add(Calendar.DAY_OF_MONTH, isEmpty(dayTIET.getText().toString()) ? 0 : -Integer.valueOf(dayTIET.getText().toString()));
        calendar.add(Calendar.MONTH, isEmpty(monthTIET.getText().toString()) ? 0 : -Integer.valueOf(monthTIET.getText().toString()));
        calendar.add(Calendar.YEAR, isEmpty(yearTIET.getText().toString()) ? 0 : -Integer.valueOf(yearTIET.getText().toString()));
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        String birthDate = DateUtils.uiDateFormat().format(calendar.getTime());
        if (!dateTIET.getText().toString().equals(birthDate)) {
            dateTIET.setText(birthDate);
            listener.onAgeSet(calendar.getTime());
        }
    }

    private void handleDateInput(int year1, int month1, int day1) {
        selectedCalendar.set(Calendar.YEAR, year1);
        selectedCalendar.set(Calendar.MONTH, month1);
        selectedCalendar.set(Calendar.DAY_OF_MONTH, day1);
        selectedCalendar.set(Calendar.HOUR_OF_DAY, 0);
        selectedCalendar.set(Calendar.MINUTE, 0);
        selectedCalendar.set(Calendar.SECOND, 0);
        selectedCalendar.set(Calendar.MILLISECOND, 0);

        String result = dateFormat.format(selectedCalendar.getTime());

        int[] dateDifference = DateUtils.getDifference(selectedCalendar.getTime(), Calendar.getInstance().getTime());
        dayTIET.setText(String.valueOf(dateDifference[2]));
        monthTIET.setText(String.valueOf(dateDifference[1]));
        yearTIET.setText(String.valueOf(dateDifference[0]));

        if (!result.equals(dateTIET.getText().toString())) {
            dateTIET.setText(result);
            listener.onAgeSet(selectedCalendar.getTime());
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
        dayTIET.setText(String.valueOf(dateDifference[2]));
        monthTIET.setText(String.valueOf(dateDifference[1]));
        yearTIET.setText(String.valueOf(dateDifference[0]));

        dateTIET.setText(result);
    }


    public void setIsBgTransparent(Boolean isBgTransparent) {

        if (!isBgTransparent)
            binding = AgeCustomViewAccentBinding.inflate(inflater, this, true);
        else
            binding = AgeCustomViewBinding.inflate(inflater, this, true);

        dateTIET = findViewById(R.id.date_picker);
        dayTIET = findViewById(R.id.input_days);
        monthTIET = findViewById(R.id.input_month);
        yearTIET = findViewById(R.id.input_year);
        selectedCalendar = Calendar.getInstance();
        dateFormat = DateUtils.uiDateFormat();

        dateTIET.setFocusable(false); //Makes editText not editable
        dateTIET.setClickable(true);//  but clickable
        dateTIET.setOnFocusChangeListener(this::onFocusChanged);
        dateTIET.setOnClickListener(this);

        dayTIET.setFocusable(false);
        dayTIET.setClickable(true);
        monthTIET.setFocusable(false);
        monthTIET.setClickable(true);
        yearTIET.setFocusable(false);
        yearTIET.setClickable(true);

        dayTIET.setFilters(new InputFilter[]{new InputFilter.LengthFilter(2)});
        monthTIET.setFilters(new InputFilter[]{new InputFilter.LengthFilter(2)});
        yearTIET.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});

        dayTIET.setOnFocusChangeListener(this);
        monthTIET.setOnFocusChangeListener(this);
        yearTIET.setOnFocusChangeListener(this);
    }

    public void setEditable(Boolean editable) {
        dateTIET.setEnabled(editable);
        dayTIET.setEnabled(editable);
        monthTIET.setEnabled(editable);
        yearTIET.setEnabled(editable);
    }

    public interface OnAgeSet {
        void onAgeSet(Date ageDate);
    }
}
