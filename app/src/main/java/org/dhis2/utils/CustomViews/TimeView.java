package org.dhis2.utils.CustomViews;

import android.app.TimePickerDialog;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.design.widget.TextInputEditText;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import org.dhis2.BR;
import org.dhis2.R;
import org.dhis2.data.forms.dataentry.fields.datetime.OnDateSelected;
import org.dhis2.utils.DateUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import timber.log.Timber;

/**
 * QUADRAM. Created by frodriguez on 1/15/2018.
 */

public class TimeView extends RelativeLayout implements View.OnClickListener {

    private TextInputEditText editText;
    private ViewDataBinding binding;

    private LayoutInflater inflater;
    private boolean isBgTransparent;
    private OnDateSelected listener;

    private String label;
    private String description;
    private Date date;

    public TimeView(Context context) {
        super(context);
        init(context);
    }

    public TimeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public TimeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        inflater = LayoutInflater.from(context);


    }

    private void setLayout() {
        binding = DataBindingUtil.inflate(inflater, R.layout.time_view, this, true);
        editText = findViewById(R.id.inputEditText);
        editText.setFocusable(false); //Makes editText not editable
        editText.setClickable(true);//  but clickable
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

    public void setDescription(String description) {
        this.description = description;
        binding.setVariable(BR.description, description);
        binding.executePendingBindings();
    }

    public void initData(String data) {
        if (data != null) {
            date = null;
            try {
                date = DateUtils.timeFormat().parse(data);
            } catch (ParseException e) {
                Timber.e(e);
            }


            data = DateUtils.timeFormat().format(date);
        }
        editText.setText(data);
    }

    public void setWarningOrError(String msg) {
        editText.setError(msg);
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
        final Calendar c = Calendar.getInstance();
        if(date!=null)
            c.setTime(date);

        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        boolean is24HourFormat = DateFormat.is24HourFormat(getContext());
        SimpleDateFormat twentyFourHourFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        SimpleDateFormat twelveHourFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        TimePickerDialog dialog = new TimePickerDialog(getContext(), (timePicker, hourOfDay, minutes) -> {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            calendar.set(Calendar.MINUTE, minutes);
            Date selectedDate = calendar.getTime();
            String calendarTime;

            if (is24HourFormat) {
                calendarTime = twentyFourHourFormat.format(selectedDate);
                editText.setText(calendarTime);
            } else {
                calendarTime = twelveHourFormat.format(selectedDate);
                editText.setText(calendarTime);
            }
            listener.onDateSelected(selectedDate);
        }, hour, minute, is24HourFormat);
        dialog.setTitle(label);
        dialog.show();
    }

    public TextInputEditText getEditText() {
        return editText;
    }

    public void setEditable(Boolean editable) {
        editText.setEnabled(editable);
    }
}
