package com.dhis2.utils.CustomViews;

import android.app.DatePickerDialog;
import android.content.Context;
import android.support.design.widget.TextInputEditText;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.dhis2.R;
import com.dhis2.databinding.AgeCustomViewBinding;
import com.dhis2.utils.TextChangedListener;

import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeModel;

import java.text.DateFormat;
import java.util.Calendar;

/**
 * Created by frodriguez on 1/15/2018.
 */

public class AgeView extends RelativeLayout implements View.OnClickListener, TextWatcher {

    private EditText date;
    private TextInputEditText day;
    private TextInputEditText month;
    private TextInputEditText year;
    private AgeCustomViewBinding binding;

    private Calendar selectedCalendar;
    private DateFormat dateFormat;

    private TextChangedListener listener;

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
        LayoutInflater inflater = LayoutInflater.from(context);
        binding = AgeCustomViewBinding.inflate(inflater, this, true);
        date = findViewById(R.id.date_picker);
        day = findViewById(R.id.input_days);
        month = findViewById(R.id.input_month);
        year = findViewById(R.id.input_year);
        selectedCalendar = Calendar.getInstance();
        dateFormat = DateFormat.getDateInstance(DateFormat.SHORT);
        date.setOnFocusChangeListener(this::onFocusChanged);
        date.setOnClickListener(this::onClick);

        day.setFilters(new InputFilter[]{new InputFilter.LengthFilter(2)});
        month.setFilters(new InputFilter[]{new InputFilter.LengthFilter(2)});
        year.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});

        date.addTextChangedListener(this);
        month.addTextChangedListener(this);
        year.addTextChangedListener(this);

    }

    public void setAttribute(TrackedEntityAttributeModel attribute) {
        binding.setAttribute(attribute);
    }

    public void setLabel(String label) {
        binding.setLabel(label);
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
                (datePicker, year1, month1, day1) -> {
                    selectedCalendar.set(Calendar.YEAR, year1);
                    selectedCalendar.set(Calendar.MONTH, month1);
                    selectedCalendar.set(Calendar.DAY_OF_MONTH, day1);
                    String result = dateFormat.format(selectedCalendar.getTime());
                    date.setText(result);
                }),
                year,
                month,
                day);
        dateDialog.getDatePicker().setMinDate(c.getTimeInMillis());
        dateDialog.show();
    }

    public void setTextChangedListener(TextChangedListener listener) {
        this.listener = listener;
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
        if (listener != null)
            listener.beforeTextChanged(charSequence, start, count, after);
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
        if (listener != null)
            listener.onTextChanged(charSequence, start, before, count);
    }

    @Override
    public void afterTextChanged(Editable editable) {
        if (listener != null)
            listener.afterTextChanged(editable);
    }
}
