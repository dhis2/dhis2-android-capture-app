package com.dhis2.utils.CustomViews;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.design.widget.TextInputEditText;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import com.dhis2.BR;
import com.dhis2.R;
import com.dhis2.data.forms.dataentry.fields.datetime.OnDateSelected;
import com.dhis2.utils.DateUtils;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by frodriguez on 1/15/2018.
 */

public class DateView extends RelativeLayout implements View.OnClickListener {

    private TextInputEditText editText;
    private ViewDataBinding binding;

    private Calendar selectedCalendar;
    private boolean isBgTransparent;
    private LayoutInflater inflater;

    private OnDateSelected listener;

    private String label;

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

    private void init(Context context) {
        inflater = LayoutInflater.from(context);


    }

    private void setLayout() {
        if (isBgTransparent)
            binding = DataBindingUtil.inflate(inflater, R.layout.date_time_view, this, true);
        else
            binding = DataBindingUtil.inflate(inflater, R.layout.date_time_view_accent, this, true);

        editText = findViewById(R.id.inputEditText);
        selectedCalendar = Calendar.getInstance();
        editText.setOnFocusChangeListener(this::onFocusChanged);
        editText.setOnClickListener(this);
    }

    public void setIsBgTransparent(boolean isBgTransparent) {
        this.isBgTransparent = isBgTransparent;
        setLayout();
    }

    public void setLabel(String label) {
        this.label = label;
        binding.setVariable(BR.label, label);
        binding.executePendingBindings();
    }

    public void initData(String data) {
        editText.setText(data);
    }

    public void setDateListener(OnDateSelected listener) {
        this.listener = listener;
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
                    selectedCalendar.set(Calendar.HOUR_OF_DAY, c.get(Calendar.HOUR_OF_DAY));
                    selectedCalendar.set(Calendar.MINUTE, c.get(Calendar.MINUTE));
                    Date selectedDate = selectedCalendar.getTime();
                    String result = DateUtils.uiDateFormat().format(selectedDate);
                    editText.setText(result);
                    listener.onDateSelected(selectedDate);
                }),
                year,
                month,
                day);
        dateDialog.setTitle(label);
        dateDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getContext().getString(R.string.date_dialog_clear), (dialog, which) -> {
            editText.setText(null);
            listener.onDateSelected(null);
        });
        dateDialog.show();
    }

}
