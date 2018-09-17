package org.dhis2.utils.CustomViews;

import android.app.DatePickerDialog;
import android.content.Context;
import android.databinding.ViewDataBinding;
import android.support.design.widget.TextInputEditText;
import android.text.InputFilter;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

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


public class AgeView extends RelativeLayout implements View.OnClickListener, View.OnFocusChangeListener {

    private TextInputEditText date;
    private TextInputEditText day;
    private TextInputEditText month;
    private TextInputEditText year;
    private ViewDataBinding binding;

    private Calendar selectedCalendar;
    private DateFormat dateFormat;

    private OnAgeSet listener;
    private LayoutInflater inflater;
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

    private void init(Context context) {
        inflater = LayoutInflater.from(context);


    }

    public void setLabel(String label) {
        this.label = label;
        if (binding instanceof AgeCustomViewAccentBinding)
            ((AgeCustomViewAccentBinding) binding).setLabel(label);
        else
            ((AgeCustomViewBinding) binding).setLabel(label);
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
            }
    }

    private void handleSingleInputs() {

        Calendar calendar = Calendar.getInstance();

        calendar.add(Calendar.DAY_OF_MONTH, isEmpty(day.getText().toString()) ? 0 : -Integer.valueOf(day.getText().toString()));
        calendar.add(Calendar.MONTH, isEmpty(month.getText().toString()) ? 0 : -Integer.valueOf(month.getText().toString()));
        calendar.add(Calendar.YEAR, isEmpty(year.getText().toString()) ? 0 : -Integer.valueOf(year.getText().toString()));

        String birthDate = DateUtils.uiDateFormat().format(calendar.getTime());
        date.setText(birthDate);
        listener.onAgeSet(calendar.getTime());
    }

    private void handleDateInput(int year1, int month1, int day1) {
        selectedCalendar.set(Calendar.YEAR, year1);
        selectedCalendar.set(Calendar.MONTH, month1);
        selectedCalendar.set(Calendar.DAY_OF_MONTH, day1);
        String result = dateFormat.format(selectedCalendar.getTime());

        int[] dateDifference = DateUtils.getDifference(selectedCalendar.getTime(), Calendar.getInstance().getTime());
        day.setText(String.valueOf(dateDifference[2]));
        month.setText(String.valueOf(dateDifference[1]));
        year.setText(String.valueOf(dateDifference[0]));

        date.setText(result);
        listener.onAgeSet(selectedCalendar.getTime());
    }

    public void setInitialValue(String initialValue) {
        try {
            Date initialDate = DateUtils.databaseDateFormat().parse(initialValue);
            String result = dateFormat.format(initialDate);

            int[] dateDifference = DateUtils.getDifference(initialDate, Calendar.getInstance().getTime());
            day.setText(String.valueOf(dateDifference[2]));
            month.setText(String.valueOf(dateDifference[1]));
            year.setText(String.valueOf(dateDifference[0]));

            date.setText(result);
        } catch (Exception e) {
            Timber.e(e);
        }
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
