package org.dhis2.utils.custom_views;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ObservableField;
import androidx.databinding.ViewDataBinding;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.dhis2.BR;
import org.dhis2.R;
import org.dhis2.data.forms.dataentry.fields.datetime.OnDateSelected;
import org.dhis2.databinding.CustomCellViewBinding;
import org.dhis2.usescases.datasets.dataSetTable.dataSetSection.DataSetTableAdapter;
import org.dhis2.utils.DateUtils;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import timber.log.Timber;

/**
 * QUADRAM. Created by frodriguez on 1/15/2018.
 */

public class DateTableView extends FieldLayout implements View.OnClickListener {

    private TextView editText;
    private CustomCellViewBinding binding;

    private Calendar selectedCalendar;
    DatePickerDialog dateDialog;

    private OnDateSelected listener;

    private String label;
    private boolean allowFutureDates;
    private Date date;

    public DateTableView(Context context) {
        super(context);
        init(context);
    }

    public DateTableView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public DateTableView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void init(Context context) {
        super.init(context);
    }

    @Override
    public void performOnFocusAction() {
        editText.performClick();
    }

    public void setCellLayout(ObservableField<DataSetTableAdapter.TableScale> tableScale){
        binding = DataBindingUtil.inflate(inflater, R.layout.custom_cell_view, this, true);
        ((CustomCellViewBinding)binding).setTableScale(tableScale);
        editText = findViewById(R.id.inputEditText);
        selectedCalendar = Calendar.getInstance();
        editText.setFocusable(false); //Makes editText not editable
        editText.setClickable(true);//  but clickable
        editText.setOnFocusChangeListener(this::onFocusChanged);
        editText.setOnClickListener(this);
    }

    public void setLabel(String label) {
        this.label = label;
        binding.setVariable(BR.label, label);
        binding.executePendingBindings();
    }

    public void setMandatory(){
        ImageView mandatory = binding.getRoot().findViewById(R.id.ic_mandatory);
        mandatory.setVisibility(View.VISIBLE);
    }

    public void setDescription(String description) {
      /*  this.description = description;
        binding.setVariable(BR.description, description);
        binding.executePendingBindings();*/
    }

    public void setAllowFutureDates(boolean allowFutureDates) {
        this.allowFutureDates = allowFutureDates;
    }

    public void initData(String data) {
        if (data != null) {
            date = null;
            data = data.replace("'", ""); //TODO: Check why it is happening
            if (data.length() == 10) //has format yyyy-MM-dd
                try {
                    date = DateUtils.uiDateFormat().parse(data);
                } catch (ParseException e) {
                    Timber.e(e);
                }
            else
                try {
                    date = DateUtils.databaseDateFormat().parse(data);
                    data = DateUtils.uiDateFormat().format(date);
                } catch (ParseException e) {
                    Timber.e(e);
                }


        } else {
            editText.setText("");
        }
        editText.setText(data);
    }

    public void setWarning(String msg) {

    }

    public void setError(String msg) {

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
        showCustomCalendar();
    }

    private void showNativeCalendar() {
        Calendar c = Calendar.getInstance();
        if (date != null)
            c.setTime(date);
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        dateDialog = new DatePickerDialog(getContext(), (
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
                    nextFocus(this);
                }),
                year,
                month,
                day);

        dateDialog.setTitle(label);

        if (!allowFutureDates) {
            dateDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        }

        dateDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getContext().getString(R.string.date_dialog_clear), (dialog, which) -> {
            editText.setText(null);
            listener.onDateSelected(null);
        });

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP){
            dateDialog.setButton(DialogInterface.BUTTON_NEUTRAL, getContext().getResources().getString(R.string.change_calendar), (dialog, which) -> {
                dateDialog.dismiss();
                showCustomCalendar();
            });
        }

        dateDialog.show();
    }

    private void showCustomCalendar() {
        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        View datePickerView = layoutInflater.inflate(R.layout.widget_datepicker, null);
        final DatePicker datePicker = datePickerView.findViewById(R.id.widget_datepicker);

        Calendar c = Calendar.getInstance();
        if (date != null)
            c.setTime(date);
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        datePicker.updateDate(year, month, day);

        if (!allowFutureDates) {
            datePicker.setMaxDate(System.currentTimeMillis());
        }

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext(), R.style.DatePickerTheme)
                .setTitle(label)
                .setPositiveButton(R.string.action_accept, (dialog, which) -> {
                    selectedCalendar.set(Calendar.YEAR, datePicker.getYear());
                    selectedCalendar.set(Calendar.MONTH, datePicker.getMonth());
                    selectedCalendar.set(Calendar.DAY_OF_MONTH, datePicker.getDayOfMonth());
                    selectedCalendar.set(Calendar.HOUR_OF_DAY, c.get(Calendar.HOUR_OF_DAY));
                    selectedCalendar.set(Calendar.MINUTE, c.get(Calendar.MINUTE));
                    Date selectedDate = selectedCalendar.getTime();
                    String result = DateUtils.uiDateFormat().format(selectedDate);
                    editText.setText(result);
                    listener.onDateSelected(selectedDate);
                    nextFocus(this);
                })
                .setNegativeButton(getContext().getString(R.string.date_dialog_clear), (dialog, which) -> {
                    editText.setText(null);
                    listener.onDateSelected(null);
                })
                .setNeutralButton(getContext().getResources().getString(R.string.change_calendar), (dialog, which) -> {
                    showNativeCalendar();
                });

        alertDialog.setView(datePickerView);
        Dialog dialog = alertDialog.create();
        dialog.show();
    }

    public TextView getEditText() {
        return editText;
    }

    public void setEditable(Boolean editable) {
        editText.setEnabled(editable);
    }
}
