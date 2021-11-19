package org.dhis2.data.forms.dataentry.fields.datetime;

import android.app.TimePickerDialog;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.dhis2.R;
import org.dhis2.commons.dialogs.calendarpicker.CalendarPicker;
import org.dhis2.commons.dialogs.calendarpicker.OnDatePickerListener;
import org.dhis2.databinding.DateTimeViewBinding;
import org.dhis2.commons.resources.ColorUtils;
import org.dhis2.utils.Constants;
import org.dhis2.utils.DateUtils;
import org.dhis2.commons.dialogs.CustomDialog;
import org.dhis2.utils.customviews.FieldLayout;
import org.jetbrains.annotations.NotNull;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import timber.log.Timber;

import static org.dhis2.Bindings.ViewExtensionsKt.closeKeyboard;

public class DateTimeView extends FieldLayout implements View.OnClickListener, View.OnFocusChangeListener {

    private TextInputEditText editText;
    private TextInputLayout inputLayout;
    private DateTimeViewBinding binding;
    private ImageView icon;

    private Calendar selectedCalendar;
    private DateFormat dateFormat;
    private OnDateSelected listener;
    private boolean allowFutureDates;
    private Date date;
    private TextView labelText;
    private View clearButton;
    private DateTimeViewModel viewModel;

    public DateTimeView(Context context) {
        super(context);
        init(context);
    }

    public DateTimeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public DateTimeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void setLabel(String label) {
        binding.setLabel(label);
    }

    public void setDescription(String description) {
        binding.setDescription(description);
        binding.descriptionLabel.setVisibility(description != null ? View.VISIBLE : View.GONE);
        binding.descriptionLabel.setOnClickListener(v ->
                new CustomDialog(
                        getContext(),
                        label,
                        description != null ? description : getContext().getString(R.string.empty_description),
                        getContext().getString(R.string.action_close),
                        null,
                        Constants.DESCRIPTION_DIALOG,
                        null
                ).show());
    }

    public void initData(String data) {
        if (data != null) {
            date = null;
            try {
                date = DateUtils.databaseDateFormat().parse(data);
            } catch (ParseException e) {
                Timber.w(e);
            }

            if (date == null) {
                try {
                    if (DateUtils.dateHasNoSeconds(data))
                        date = DateUtils.databaseDateFormatNoSeconds().parse(data);
                    else
                        date = DateUtils.databaseDateFormatNoMillis().parse(data);
                } catch (ParseException e) {
                    Timber.e(e);
                }
            }

            if (date == null) {
                try {
                    date = DateUtils.dateTimeFormat().parse(data);
                    data = DateUtils.dateTimeFormat().format(date);
                } catch (ParseException e) {
                    Timber.e(e);
                }
            }

            data = date != null ? DateUtils.dateTimeFormat().format(date) : data;
        } else {
            editText.setText("");
        }
        editText.setText(data);

        updateDeleteVisibility(clearButton);
    }

    public void setWarningErrorMessage(String warning, String error) {
        if (error != null) {
            inputLayout.setErrorTextAppearance(R.style.error_appearance);
            inputLayout.setError(error);
            editText.setText(null);
        } else if (warning != null) {
            inputLayout.setErrorTextAppearance(R.style.warning_appearance);
            inputLayout.setError(warning);
        }else{
            inputLayout.setError(null);
        }
    }

    public void setAllowFutureDates(boolean allowFutureDates) {
        this.allowFutureDates = allowFutureDates;
    }

    private void setLayout(boolean isBgTransparent) {
        this.isBgTransparent = isBgTransparent;
        binding = DateTimeViewBinding.inflate(inflater, this, true);
        inputLayout = findViewById(R.id.inputLayout);
        icon = findViewById(R.id.descIcon);
        editText = findViewById(R.id.inputEditText);
        clearButton = findViewById(R.id.clear_button);
        labelText = findViewById(R.id.label);
        inputLayout.setHint(getContext().getString(R.string.choose_date));
        icon.setImageResource(R.drawable.ic_form_date_time);
        icon.setOnClickListener(this);
        selectedCalendar = Calendar.getInstance();
        dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
        editText.setFocusable(false); //Makes editText not editable
        editText.setClickable(true);//  but clickable
        editText.setOnFocusChangeListener(this);
        editText.setOnClickListener(this);
        clearButton.setOnClickListener(v -> {
            viewModel.onItemClick();
            clearDate();
        });
    }

    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        if (hasFocus)
            onClick(view);
    }

    public void setDateListener(OnDateSelected listener) {
        this.listener = listener;
    }

    @Override
    public void onClick(View view) {
        requestFocus();
        viewModel.onItemClick();
        closeKeyboard(binding.getRoot());
        showCustomCalendar(view);
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

    private void showCustomCalendar(View view) {
        CalendarPicker dialog = new CalendarPicker(view.getContext());
        dialog.setTitle(label);
        dialog.setInitialDate(date);
        dialog.isFutureDatesAllowed(allowFutureDates);
        dialog.setListener(new OnDatePickerListener() {
            @Override
            public void onNegativeClick() {
                clearDate();
            }

            @Override
            public void onPositiveClick(@NotNull DatePicker datePicker) {
                selectedCalendar.set(Calendar.YEAR, datePicker.getYear());
                selectedCalendar.set(Calendar.MONTH, datePicker.getMonth());
                selectedCalendar.set(Calendar.DAY_OF_MONTH, datePicker.getDayOfMonth());
                showTimePicker(view);
            }
        });
        dialog.show();
    }

    private void showTimePicker(View view) {
        final Calendar c = Calendar.getInstance();
        if (date != null) {
            c.setTime(date);
        }
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        boolean is24HourFormat = android.text.format.DateFormat.is24HourFormat(getContext());

        TimePickerDialog dialog = new TimePickerDialog(getContext(), (
                timePicker, hourOfDay, minutes) -> {
            selectedCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            selectedCalendar.set(Calendar.MINUTE, minutes);
            Date selectedDate = selectedCalendar.getTime();
            String result = dateFormat.format(selectedDate);
            editText.setText(result);
            listener.onDateSelected(selectedDate);
            nextFocus(view);
            date = null;
            updateDeleteVisibility(clearButton);
        },
                hour,
                minute,
                is24HourFormat);
        dialog.setTitle(binding.getLabel());
        dialog.show();
    }

    public TextInputEditText getEditText() {
        return editText;
    }

    public void setEditable(Boolean editable) {
        editText.setEnabled(editable);
        clearButton.setEnabled(editable);
        icon.setEnabled(editable);
        editText.setTextColor(
                !isBgTransparent ? ColorUtils.getPrimaryColor(getContext(), ColorUtils.ColorType.ACCENT) :
                        ContextCompat.getColor(getContext(), R.color.textPrimary)
        );

        setEditable(editable,
                labelText,
                inputLayout,
                findViewById(R.id.descIcon),
                findViewById(R.id.descriptionLabel),
                clearButton
        );
        updateDeleteVisibility(clearButton);
    }

    private void clearDate() {
        editText.setText(null);
        listener.onDateSelected(null);
        date = null;
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

    public void setViewModel(DateTimeViewModel viewModel) {
        this.viewModel = viewModel;

        if (binding == null) {
            setLayout(viewModel.isBackgroundTransparent());
        }

        setLabel(viewModel.getFormattedLabel());
        String description = viewModel.description();

        if (viewModel.url() != null){
            description = description + "\n" + viewModel.url()  ;
        }

        setDescription(description);
        initData(viewModel.value());
        setWarningErrorMessage(viewModel.warning(), viewModel.error());
        setAllowFutureDates(viewModel.allowFutureDate());
        setEditable(viewModel.editable());
        setDateListener(viewModel::onDateSelected);
    }

}
