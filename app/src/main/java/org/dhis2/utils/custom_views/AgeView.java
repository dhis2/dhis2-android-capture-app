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

    private TextInputEditText date;
    private TextInputEditText day;
    private TextInputEditText month;
    private TextInputEditText year;
    private ViewDataBinding binding;

    private Calendar selectedCalendar;
    private DateFormat dateFormat;

    private OnAgeSet listener;
    private String label;
    private String description;

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
        this.description = description;
        if (binding instanceof AgeCustomViewAccentBinding) {
            ((AgeCustomViewAccentBinding) binding).setLabel(label);
            ((AgeCustomViewAccentBinding) binding).setDescription(description);
        } else {
            ((AgeCustomViewBinding) binding).setLabel(label);
            ((AgeCustomViewBinding) binding).setDescription(description);
        }
    }

    public void setWarningOrError(String warningOrError) {
        date.setError(warningOrError);
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
                (datePicker, year1, month1, day1) -> handleDateInput(view, year1, month1, day1)),
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
            }
    }

    protected void handleSingleInputs() {

        Calendar calendar = Calendar.getInstance();

        calendar.add(Calendar.DAY_OF_MONTH, isEmpty(day.getText().toString()) ? 0 : -Integer.valueOf(day.getText().toString()));
        calendar.add(Calendar.MONTH, isEmpty(month.getText().toString()) ? 0 : -Integer.valueOf(month.getText().toString()));
        calendar.add(Calendar.YEAR, isEmpty(year.getText().toString()) ? 0 : -Integer.valueOf(year.getText().toString()));
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
        day.setText(String.valueOf(dateDifference[2]));
        month.setText(String.valueOf(dateDifference[1]));
        year.setText(String.valueOf(dateDifference[0]));

        if (!result.equals(date.getText().toString())) {
            date.setText(result);
            listener.onAgeSet(selectedCalendar.getTime());
//            nextFocus(view);
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
        day.setText(String.valueOf(dateDifference[2]));
        month.setText(String.valueOf(dateDifference[1]));
        year.setText(String.valueOf(dateDifference[0]));

        date.setText(result);
    }


    public void setIsBgTransparent(Boolean isBgTransparent) {

        if (!isBgTransparent)
            binding = AgeCustomViewAccentBinding.inflate(inflater, this, true);
        else
            binding = AgeCustomViewBinding.inflate(inflater, this, true);

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

        day.setFocusable(true);
        day.setClickable(true);
        month.setFocusable(true);
        month.setClickable(true);
        year.setFocusable(true);
        year.setClickable(true);

        day.setFilters(new InputFilter[]{new InputFilter.LengthFilter(2)});
        month.setFilters(new InputFilter[]{new InputFilter.LengthFilter(2)});
        year.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});

        day.setOnFocusChangeListener(this);
        month.setOnFocusChangeListener(this);
        year.setOnFocusChangeListener(this);
    }

    public void setEditable(Boolean editable) {
        date.setEnabled(editable);
        day.setEnabled(editable);
        month.setEnabled(editable);
        year.setEnabled(editable);
    }

    public interface OnAgeSet {
        void onAgeSet(Date ageDate);
    }
}
