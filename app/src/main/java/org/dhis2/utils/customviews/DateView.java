package org.dhis2.utils.customviews;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
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
import org.dhis2.utils.ColorUtils;
import org.dhis2.utils.DatePickerUtils;
import org.dhis2.utils.DateUtils;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import timber.log.Timber;

/**
 * QUADRAM. Created by frodriguez on 1/15/2018.
 */

public class DateView extends FieldLayout implements View.OnClickListener {

    private TextInputEditText editText;
    private ViewDataBinding binding;

    private Calendar selectedCalendar;

    private OnDateSelected listener;

    private String label;
    private boolean allowFutureDates;
    private String description;
    private Date date;
    private TextInputLayout inputLayout;
    private ImageView clearButton;
    private TextView labelText;
    private View descriptionLabel;
    private ImageView descriptionIcon;

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

    public void init(Context context) {
        super.init(context);
    }

    private void setLayout() {
        if (isBgTransparent)
            binding = DataBindingUtil.inflate(inflater, R.layout.date_time_view, this, true);
        else
            binding = DataBindingUtil.inflate(inflater, R.layout.date_time_view_accent, this, true);

        inputLayout = findViewById(R.id.inputLayout);
        editText = findViewById(R.id.inputEditText);
        clearButton = findViewById(R.id.clear_button);
        labelText = findViewById(R.id.label);
        descriptionIcon = findViewById(R.id.descIcon);
        descriptionLabel = findViewById(R.id.descriptionLabel);
        inputLayout.setHint(getContext().getString(R.string.choose_date));
        descriptionIcon.setImageResource(R.drawable.ic_form_date);
        selectedCalendar = Calendar.getInstance();
        editText.setFocusable(false); //Makes editText not editable
        editText.setClickable(true);//  but clickable
        editText.setOnFocusChangeListener(this::onFocusChanged);
        editText.setOnClickListener(this);
        clearButton.setOnClickListener( v -> { clearDate(); });
        descriptionIcon.setOnClickListener(this);
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

    public void setIsBgTransparent(boolean isBgTransparent) {
        this.isBgTransparent = isBgTransparent;
        setLayout();
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
        this.description = description;
        descriptionLabel.setVisibility(description != null ? View.VISIBLE : View.GONE);
        binding.setVariable(BR.description, description);
        binding.executePendingBindings();
    }

    public void setAllowFutureDates(boolean allowFutureDates) {
        this.allowFutureDates = allowFutureDates;
    }

    public void initData(String data) {
        if (data != null) {
            date = null;
            data = data.replace("'", ""); //TODO: Check why it is happening
                try {
                    date = DateUtils.oldUiDateFormat().parse(data);
                    data = DateUtils.uiDateFormat().format(date);
                } catch (ParseException e) {
                    Timber.e(e);
                }
                if(date == null) {
                    try {
                        date = DateUtils.databaseDateFormat().parse(data);
                        data = DateUtils.uiDateFormat().format(date);
                    } catch (ParseException e) {
                        Timber.e(e);
                    }
                }
                if(date == null) {
                    try {
                        date = DateUtils.uiDateFormat().parse(data);
                        data = DateUtils.uiDateFormat().format(date);
                    } catch (ParseException e) {
                        Timber.e(e);
                    }
                }
        } else {
            editText.setText("");
        }
        editText.setText(data);

        updateDeleteVisibility(clearButton);
    }

    public void setWarning(String msg) {
        inputLayout.setErrorTextAppearance(R.style.warning_appearance);
        inputLayout.setError(msg);
    }

    public void setError(String msg) {
        inputLayout.setErrorTextAppearance(R.style.error_appearance);
        inputLayout.setError(msg);
        editText.setText(null);
        editText.requestFocus();
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
        requestFocus();
        activate();
        showCustomCalendar();
    }

    @Override
    public void dispatchSetActivated(boolean activated) {
        super.dispatchSetActivated(activated);
        if (activated) {
            labelText.setTextColor(ColorUtils.getPrimaryColor(getContext(), ColorUtils.ColorType.PRIMARY));
        } else {
            labelText.setTextColor(ResourcesCompat.getColor(getResources(), R.color.textPrimary, null));
        }
    }

    private void showCustomCalendar() {

        DatePickerUtils.getDatePickerDialog(getContext(), label, date, allowFutureDates,
                new DatePickerUtils.OnDatePickerClickListener() {
                    @Override
                    public void onNegativeClick() {
                        clearDate();
                    }

                    @Override
                    public void onPositiveClick(DatePicker datePicker) {
                        selectedCalendar.set(Calendar.YEAR, datePicker.getYear());
                        selectedCalendar.set(Calendar.MONTH, datePicker.getMonth());
                        selectedCalendar.set(Calendar.DAY_OF_MONTH, datePicker.getDayOfMonth());
                        selectedCalendar.set(Calendar.HOUR_OF_DAY, 0);
                        selectedCalendar.set(Calendar.MINUTE, 0);
                        Date selectedDate = selectedCalendar.getTime();
                        String result = DateUtils.uiDateFormat().format(selectedDate);
                        editText.setText(result);
                        listener.onDateSelected(selectedDate);
                        nextFocus(DateView.this);
                        date = null;
                        updateDeleteVisibility(clearButton);
                    }
                }).show();
    }

    private void clearDate() {
        editText.setText(null);
        listener.onDateSelected(null);
        date = null;
        updateDeleteVisibility(clearButton);
    }

    public TextInputEditText getEditText() {
        return editText;
    }

    public void setEditable(Boolean editable) {
        editText.setEnabled(editable);
        clearButton.setEnabled(editable);
        descriptionIcon.setEnabled(editable);
        editText.setTextColor(
                !isBgTransparent ? ColorUtils.getPrimaryColor(getContext(), ColorUtils.ColorType.ACCENT) :
                        ContextCompat.getColor(getContext(), R.color.textPrimary)
        );

        setEditable(editable,
                labelText,
                inputLayout,
                findViewById(R.id.descIcon),
                descriptionLabel,
                clearButton
        );

        updateDeleteVisibility(clearButton);
    }

    @Override
    protected boolean hasValue() {
        return editText.getText() != null && !editText.getText().toString().isEmpty();
    }

    @Override
    protected boolean isEditable() {
        return editText.isEnabled();
    }
}
