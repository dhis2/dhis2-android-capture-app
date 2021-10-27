package org.dhis2.utils.customviews;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.databinding.DataBindingUtil;
import androidx.databinding.ObservableField;

import org.dhis2.BR;
import org.dhis2.Bindings.StringExtensionsKt;
import org.dhis2.R;
import org.dhis2.commons.dialogs.calendarpicker.CalendarPicker;
import org.dhis2.commons.dialogs.calendarpicker.OnDatePickerListener;
import org.dhis2.data.forms.dataentry.tablefields.datetime.OnDateSelected;
import org.dhis2.databinding.CustomCellViewBinding;
import org.dhis2.usescases.datasets.dataSetTable.dataSetSection.DataSetTableAdapter;
import org.dhis2.utils.DateUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Calendar;
import java.util.Date;


public class DateTableView extends FieldLayout implements View.OnClickListener {

    private TextView editText;
    private CustomCellViewBinding binding;

    private Calendar selectedCalendar;

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

    public void setCellLayout(ObservableField<DataSetTableAdapter.TableScale> tableScale) {
        binding = DataBindingUtil.inflate(inflater, R.layout.custom_cell_view, this, true);
        ((CustomCellViewBinding) binding).setTableScale(tableScale);
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

    public void setMandatory() {
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
            date = StringExtensionsKt.toDate(data.replace("'", ""));
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

    private void showCustomCalendar() {
        CalendarPicker dialog = new CalendarPicker(binding.getRoot().getContext());
        dialog.setTitle(label);
        dialog.setInitialDate(date);
        dialog.isFutureDatesAllowed(allowFutureDates);
        dialog.setListener(new OnDatePickerListener() {
            @Override
            public void onNegativeClick() {
                editText.setText(null);
                listener.onDateSelected(null);
            }

            @Override
            public void onPositiveClick(@NotNull DatePicker datePicker) {
                selectedCalendar.set(Calendar.YEAR, datePicker.getYear());
                selectedCalendar.set(Calendar.MONTH, datePicker.getMonth());
                selectedCalendar.set(Calendar.DAY_OF_MONTH, datePicker.getDayOfMonth());
                selectedCalendar.set(Calendar.HOUR_OF_DAY, 0);
                selectedCalendar.set(Calendar.MINUTE, 0);
                Date selectedDate = selectedCalendar.getTime();
                String result = DateUtils.uiDateFormat().format(selectedDate);
                editText.setText(result);
                listener.onDateSelected(selectedDate);
                nextFocus(DateTableView.this);
            }
        });
        dialog.show();
    }

    public TextView getEditText() {
        return editText;
    }

    public void setEditable(Boolean editable) {
        editText.setEnabled(editable);
    }
}
