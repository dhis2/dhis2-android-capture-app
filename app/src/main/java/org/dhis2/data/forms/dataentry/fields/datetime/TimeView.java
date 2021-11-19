package org.dhis2.data.forms.dataentry.fields.datetime;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.View;
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
import org.dhis2.databinding.CustomCellViewBinding;
import org.dhis2.usescases.datasets.dataSetTable.dataSetSection.DataSetTableAdapter;
import org.dhis2.commons.resources.ColorUtils;
import org.dhis2.utils.Constants;
import org.dhis2.utils.DateUtils;
import org.dhis2.commons.dialogs.CustomDialog;
import org.dhis2.utils.customviews.FieldLayout;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import timber.log.Timber;

import static org.dhis2.Bindings.ViewExtensionsKt.closeKeyboard;


public class TimeView extends FieldLayout implements View.OnClickListener {

    private TextInputEditText editText;
    private TextInputLayout inputLayout;
    private ViewDataBinding binding;
    private TextView labelText;

    private OnDateSelected listener;

    private String label;
    private Date date;
    private View clearButton;
    private View descriptionLabel;
    private ImageView descriptionIcon;
    private DateTimeViewModel viewModel;

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

    private void setLayout() {
        binding = DataBindingUtil.inflate(inflater, R.layout.date_time_view, this, true);
        editText = findViewById(R.id.inputEditText);
        inputLayout = findViewById(R.id.inputLayout);
        labelText = findViewById(R.id.label);
        descriptionLabel = findViewById(R.id.descriptionLabel);
        descriptionIcon = findViewById(R.id.descIcon);
        inputLayout.setHint(getContext().getString(R.string.select_time));
        descriptionIcon.setImageResource(R.drawable.ic_form_time);
        clearButton = findViewById(R.id.clear_button);
        clearButton.setOnClickListener(v -> {
            viewModel.onItemClick();
            clearTime();
        });
        editText.setFocusable(false); //Makes editText not editable
        editText.setClickable(true);//  but clickable
        editText.setOnFocusChangeListener(this::onFocusChanged);
        editText.setOnClickListener(this);
        descriptionIcon.setOnClickListener(this);
    }

    public void setCellLayout(ObservableField<DataSetTableAdapter.TableScale> tableScale) {
        binding = DataBindingUtil.inflate(inflater, R.layout.custom_cell_view, this, true);
        ((CustomCellViewBinding) binding).setTableScale(tableScale);
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
            data = date != null ? DateUtils.timeFormat().format(date) : data;
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

    public void setMandatory() {
        ImageView mandatory = binding.getRoot().findViewById(R.id.ic_mandatory);
        mandatory.setVisibility(View.VISIBLE);
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
        viewModel.onItemClick();
        closeKeyboard(binding.getRoot());

        final Calendar c = Calendar.getInstance();
        if (date != null)
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
            } else {
                calendarTime = twelveHourFormat.format(selectedDate);
            }
            editText.setText(calendarTime);

            listener.onDateSelected(selectedDate);
            nextFocus(view);
            date = null;
            updateDeleteVisibility(clearButton);
        }, hour, minute, is24HourFormat);

        dialog.setTitle(label);

        dialog.setButton(
                DialogInterface.BUTTON_NEGATIVE, getContext().getString(R.string.date_dialog_clear),
                (timeDialog, which) -> clearTime()
        );

        dialog.show();
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

    private void clearTime() {
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
            setIsBgTransparent(viewModel.isBackgroundTransparent());
        }

        setLabel(viewModel.getFormattedLabel());

        String description = viewModel.description();

        if (viewModel.url() != null){
            description = description + "\n" + viewModel.url()  ;
        }

        setDescription(description);

        initData(viewModel.value());
        setEditable(viewModel.editable());
        setDateListener(viewModel::onDateSelected);
    }
}
