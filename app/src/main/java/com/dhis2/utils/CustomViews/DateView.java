package com.dhis2.utils.CustomViews;

import android.app.DatePickerDialog;
import android.content.Context;
import android.databinding.BindingAdapter;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.dhis2.R;
import com.dhis2.databinding.DateTimeViewBinding;

import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeModel;

import java.text.DateFormat;
import java.util.Calendar;

/**
 * Created by frodriguez on 1/15/2018.
 */

public class DateView extends RelativeLayout implements View.OnClickListener {

    private EditText dateTime;
    private DateTimeViewBinding binding;

    private Calendar selectedCalendar;
    private DateFormat dateFormat;

    public DateView(Context context) {
        super(context);
        init(context);
    }

    public DateView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public DateView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context){
        LayoutInflater inflater = LayoutInflater.from(context);
        binding = DateTimeViewBinding.inflate(inflater, this, true);
        dateTime = findViewById(R.id.button);
        selectedCalendar = Calendar.getInstance();
        dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
        dateTime.setOnFocusChangeListener(this::onFocusChanged);
        dateTime.setOnClickListener(this::onClick);

    }

    public void setAttribute(TrackedEntityAttributeModel attribute){
        binding.setAttribute(attribute);
    }

    public void setLabel(String label){
        binding.setLabel(label);
    }

    private void onFocusChanged(View view, boolean b) {
        if(b)
            onClick(view);
    }

    @Override
    public void onClick(View view) {
        Calendar c = Calendar.getInstance();
        int year  = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day   = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dateDialog = new DatePickerDialog(getContext(), (
                (datePicker, year1, month1, day1) -> {
                    selectedCalendar.set(Calendar.YEAR, year1);
                    selectedCalendar.set(Calendar.MONTH, month1);
                    selectedCalendar.set(Calendar.DAY_OF_MONTH, day1);
                    selectedCalendar.set(Calendar.HOUR_OF_DAY, c.get(Calendar.HOUR_OF_DAY));
                    selectedCalendar.set(Calendar.MINUTE, c.get(Calendar.MINUTE));
                    String result = dateFormat.format(selectedCalendar.getTime());
                    dateTime.setText(result);
                }),
                year,
                month,
                day);
        dateDialog.show();
    }
}
