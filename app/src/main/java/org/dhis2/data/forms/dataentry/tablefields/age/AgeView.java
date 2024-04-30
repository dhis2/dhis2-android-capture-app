package org.dhis2.data.forms.dataentry.tablefields.age;

import static android.text.TextUtils.isEmpty;

import android.content.Context;
import android.content.res.ColorStateList;
import android.text.InputFilter;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.ViewCompat;
import androidx.databinding.ViewDataBinding;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.dhis2.R;
import org.dhis2.bindings.StringExtensionsKt;
import org.dhis2.commons.dialogs.CustomDialog;
import org.dhis2.commons.dialogs.calendarpicker.CalendarPicker;
import org.dhis2.commons.dialogs.calendarpicker.OnDatePickerListener;
import org.dhis2.commons.resources.ColorType;
import org.dhis2.commons.resources.ColorUtils;
import org.dhis2.databinding.AgeCustomViewAccentBinding;
import org.dhis2.databinding.AgeCustomViewBinding;
import org.dhis2.commons.Constants;
import org.dhis2.utils.DateUtils;
import org.dhis2.utils.customviews.FieldLayout;
import org.jetbrains.annotations.NotNull;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.inject.Inject;

public class AgeView extends FieldLayout implements View.OnClickListener {

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
    private TextView labelText;
    private View descriptionLabel;
    private View yearInputLayout;
    private View monthInputLayout;
    private View dayInputLayout;
    private AgeViewModel viewModel;
    private TextView errorView;
    Date selectedDate;

    @Inject
    ColorUtils colorUtils;

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

    @Override
    public void init(Context context) {
        super.init(context);
        listener = ageDate -> selectedDate = ageDate;
    }

    public void setLabel(String label, String description) {
        this.label = label;
        descriptionLabel.setVisibility(description != null ? View.VISIBLE : View.GONE);
        descriptionLabel.setOnClickListener(v ->
                new CustomDialog(
                        getContext(),
                        label,
                        description != null ? description : getContext().getString(R.string.empty_description),
                        getContext().getString(R.string.action_close),
                        null,
                        Constants.DESCRIPTION_DIALOG,
                        null
                ).show());
        if (binding instanceof AgeCustomViewAccentBinding) {
            ((AgeCustomViewAccentBinding) binding).setLabel(label);
            ((AgeCustomViewAccentBinding) binding).setDescription(description);
        } else {
            ((AgeCustomViewBinding) binding).setLabel(label);
            ((AgeCustomViewBinding) binding).setDescription(description);
        }
    }

    public void setWarning(String msg) {
        setErrorColor(ContextCompat.getColor(getContext(), R.color.warning_color));
        errorView.setText(msg);
        errorView.setVisibility(View.VISIBLE);
    }

    public void setError(String msg) {
        setErrorColor(ContextCompat.getColor(getContext(), R.color.error_color));
        errorView.setText(msg);
        errorView.setVisibility(View.VISIBLE);
    }

    public void clearErrors() {
        setErrorColor(ContextCompat.getColor(getContext(), R.color.textPrimary));
        errorView.setVisibility(View.GONE);
    }

    private void setErrorColor(int color) {
        ViewCompat.setBackgroundTintList(day, ColorStateList.valueOf(color));
        ViewCompat.setBackgroundTintList(month, ColorStateList.valueOf(color));
        ViewCompat.setBackgroundTintList(year, ColorStateList.valueOf(color));
        ViewCompat.setBackgroundTintList(date, ColorStateList.valueOf(color));
        errorView.setTextColor(color);
    }

    private void onFocusChanged(View view, boolean hasFocus) {
        if (hasFocus) {
            onClick(view);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.date_picker:
                showCustomCalendar(view);
                break;
            case R.id.input_days:
            case R.id.input_month:
            case R.id.input_year:
                getYearsDialog().show();
                break;
        }

    }

    @Override
    public void dispatchSetActivated(boolean activated) {
        super.dispatchSetActivated(activated);
        if (activated) {
            labelText.setTextColor(colorUtils.getPrimaryColor(getContext(), ColorType.PRIMARY));
        } else {
            labelText.setTextColor(ResourcesCompat.getColor(getResources(), R.color.textPrimary, null));
        }
    }

    private void showCustomCalendar(View view) {
        CalendarPicker dialog = new CalendarPicker(view.getContext());
        dialog.setTitle(label);
        dialog.setInitialDate(selectedCalendar.getTime());
        dialog.isFutureDatesAllowed(true);
        dialog.setListener(new OnDatePickerListener() {
            @Override
            public void onNegativeClick() {
                listener.onAgeSet(null);
                clearValues();
            }

            @Override
            public void onPositiveClick(@NotNull DatePicker datePicker) {
                handleDateInput(view, datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());
            }
        });
        dialog.show();
    }

    private AlertDialog getYearsDialog() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_age, null);
        TextInputEditText yearPicker = view.findViewById(R.id.input_year);
        TextInputEditText monthPicker = view.findViewById(R.id.input_month);
        TextInputEditText dayPicker = view.findViewById(R.id.input_days);
        yearPicker.setText(year.getText());
        monthPicker.setText(month.getText());
        dayPicker.setText(day.getText());

        return new MaterialAlertDialogBuilder(getContext(), R.style.MaterialDialog)
                .setView(view)
                .setPositiveButton(R.string.action_accept, (dialog, which) -> handleSingleInputs(
                        isEmpty(yearPicker.getText().toString()) ? 0 : -Integer.valueOf(yearPicker.getText().toString()),
                        isEmpty(monthPicker.getText().toString()) ? 0 : -Integer.valueOf(monthPicker.getText().toString()),
                        isEmpty(dayPicker.getText().toString()) ? 0 : -Integer.valueOf(dayPicker.getText().toString())))
                .setNegativeButton(R.string.clear, (dialog, which) -> {
                    clearValues();
                    listener.onAgeSet(null);
                })
                .create();
    }

    public void setAgeChangedListener(OnAgeSet listener) {
        this.listener = listener;
    }

    protected void handleSingleInputs(int year, int month, int day) {

        Calendar calendar = Calendar.getInstance();

        calendar.add(Calendar.DAY_OF_MONTH, day);
        calendar.add(Calendar.MONTH, month);
        calendar.add(Calendar.YEAR, year);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        String birthDate = DateUtils.uiDateFormat().format(calendar.getTime());

        int[] dateDifference = DateUtils.getDifference(calendar.getTime(), Calendar.getInstance().getTime());
        this.day.setText(String.valueOf(dateDifference[2]));
        this.month.setText(String.valueOf(dateDifference[1]));
        this.year.setText(String.valueOf(dateDifference[0]));

        if (!date.getText().toString().equals(birthDate)) {
            date.setText(birthDate);
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
        try {
            Date initialDate = StringExtensionsKt.toDate(initialValue);
            if (initialDate != null) {
                String result = dateFormat.format(initialDate);
                selectedCalendar.setTime(initialDate);

                int[] dateDifference = DateUtils.getDifference(initialDate, Calendar.getInstance().getTime());
                day.setText(String.valueOf(dateDifference[2]));
                month.setText(String.valueOf(dateDifference[1]));
                year.setText(String.valueOf(dateDifference[0]));

                date.setText(result);
            }
        } catch (Exception e) {
            date.setError(e.getMessage());
        }
    }


    public void setIsBgTransparent() {
        binding = AgeCustomViewBinding.inflate(inflater, this, true);

        inputLayout = findViewById(R.id.inputLayout);
        yearInputLayout = findViewById(R.id.yearInputLayout);
        monthInputLayout = findViewById(R.id.monthInputLayout);
        dayInputLayout = findViewById(R.id.dayInputLayout);
        date = findViewById(R.id.date_picker);
        day = findViewById(R.id.input_days);
        month = findViewById(R.id.input_month);
        year = findViewById(R.id.input_year);
        labelText = findViewById(R.id.label);
        selectedCalendar = Calendar.getInstance();
        dateFormat = DateUtils.uiDateFormat();
        descriptionLabel = binding.getRoot().findViewById(R.id.descriptionLabel);
        errorView = findViewById(R.id.errorMessage);

        date.setFocusable(false); //Makes editText not editable
        date.setClickable(true);//  but clickable
        date.setOnFocusChangeListener(this::onFocusChanged);
        date.setOnClickListener(this);

        day.setFocusable(false);
        day.setClickable(true);
        month.setFocusable(false);
        month.setClickable(true);
        year.setFocusable(false);
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
        day.setOnClickListener(this);
        month.setOnClickListener(this);
        year.setOnClickListener(this);
    }

    public void setEditable(Boolean editable) {
        date.setEnabled(editable);
        day.setEnabled(editable);
        month.setEnabled(editable);
        year.setEnabled(editable);

        date.setTextColor(
                !isBgTransparent ? colorUtils.getPrimaryColor(getContext(), ColorType.ACCENT) :
                        ContextCompat.getColor(getContext(), R.color.textPrimary)
        );
        day.setTextColor(
                !isBgTransparent ? colorUtils.getPrimaryColor(getContext(), ColorType.ACCENT) :
                        ContextCompat.getColor(getContext(), R.color.textPrimary)
        );
        month.setTextColor(
                !isBgTransparent ? colorUtils.getPrimaryColor(getContext(), ColorType.ACCENT) :
                        ContextCompat.getColor(getContext(), R.color.textPrimary)
        );
        year.setTextColor(
                !isBgTransparent ? colorUtils.getPrimaryColor(getContext(), ColorType.ACCENT) :
                        ContextCompat.getColor(getContext(), R.color.textPrimary)
        );

        setEditable(editable,
                labelText,
                descriptionLabel,
                inputLayout,
                dayInputLayout,
                monthInputLayout,
                yearInputLayout
        );
    }

    public void clearValues() {
        date.setText(null);
        day.setText(null);
        month.setText(null);
        year.setText(null);
    }

    public interface OnAgeSet {
        void onAgeSet(Date ageDate);
    }

    public void setViewModel(AgeViewModel viewModel) {
        this.viewModel = viewModel;
        if (binding == null) {
            setIsBgTransparent();
        }

        setLabel(getFormattedLabel(), viewModel.description());

        if (!isEmpty(viewModel.value())) {
            setInitialValue(viewModel.value());
        } else {
            clearValues();
        }

        if (viewModel.warning() != null)
            setWarning(viewModel.warning());
        else if (viewModel.error() != null)
            setError(viewModel.error());
        else
            clearErrors();

        setEditable(viewModel.editable());
    }

    private String getFormattedLabel() {
        if (viewModel.mandatory()) {
            return viewModel.label() + " *";
        } else {
            return viewModel.label();
        }
    }

    public Date getSelectedDate() {
        return selectedDate;
    }
}