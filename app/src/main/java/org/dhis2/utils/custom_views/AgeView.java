package org.dhis2.utils.custom_views;

import android.content.Context;
import android.text.InputFilter;
import android.util.AttributeSet;
import android.view.View;
import android.widget.DatePicker;

import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.MutableLiveData;

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
    private TextInputLayout inputLayout;
    private MutableLiveData<String> currentUidListener;

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
        showCustomCalendar(view);
    }

    private void showCustomCalendar(View view) {

        DatePickerUtils.getDatePickerDialog(getContext(), label, null, true, new DatePickerUtils.OnDatePickerClickListener() {
            @Override
            public void onNegativeClick() {
                listener.onAgeSet(null);
                date.setText(null);
                AgeView.this.day.setText(null);
                AgeView.this.month.setText(null);
                AgeView.this.year.setText(null);
            }

            @Override
            public void onPositiveClick(DatePicker datePicker) {
                handleDateInput(view, datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());
            }
        }).show();
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
                    handleSingleInputs(false);
                    break;
            }
        else
            activate();
    }

    protected void handleSingleInputs(boolean finish) {

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

        if(initialDate != null) {
            String result = dateFormat.format(initialDate);

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

        day.setFocusable(true);
        day.setClickable(true);
        month.setFocusable(true);
        month.setClickable(true);
        year.setFocusable(true);
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
