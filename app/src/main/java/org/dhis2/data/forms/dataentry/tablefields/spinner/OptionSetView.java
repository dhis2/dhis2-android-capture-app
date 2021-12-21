package org.dhis2.data.forms.dataentry.tablefields.spinner;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ObservableField;
import androidx.databinding.ViewDataBinding;
import org.dhis2.R;
import org.dhis2.databinding.CustomCellViewBinding;
import org.dhis2.usescases.datasets.dataSetTable.dataSetSection.DataSetTableAdapter;
import org.dhis2.commons.resources.ColorUtils;
import org.dhis2.utils.customviews.FieldLayout;
import org.dhis2.utils.customviews.OptionSetOnClickListener;
import org.hisp.dhis.android.core.option.Option;

public class OptionSetView extends FieldLayout implements OptionSetOnClickListener {
    private ViewDataBinding binding;

    private TextView editText;
    private OnSelectedOption listener;

    public OptionSetView(Context context) {
        super(context);
        init(context);
    }

    public OptionSetView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public OptionSetView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void setCellLayout(ObservableField<DataSetTableAdapter.TableScale> tableScale) {
        binding = DataBindingUtil.inflate(inflater, R.layout.custom_cell_view, this, true);
        isBgTransparent = true;
        ((CustomCellViewBinding) binding).setTableScale(tableScale);
        editText = findViewById(R.id.inputEditText);
        editText.setFocusable(false); //Makes editText not editable
        editText.setClickable(true);//  but clickable

        editText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus)
                editText.performClick();
        });
    }


    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        editText.setOnClickListener(l);
    }


    public void setOnSelectedOptionListener(OnSelectedOption listener) {
        this.listener = listener;
    }

    @Override
    public void onSelectOption(Option option) {
        setValueOption(option.displayName(), option.code());
    }

    private void setValueOption(String optionDisplayName, String optionCode) {
        editText.setText(optionDisplayName);
        listener.onSelectedOption(optionDisplayName, optionCode);
    }

    public void updateEditable(boolean isEditable) {
        editText.setEnabled(isEditable);
        editText.setFocusable(false);
        editText.setClickable(isEditable);
        editText.setTextColor(
                !isBgTransparent ? ColorUtils.getPrimaryColor(getContext(), ColorUtils.ColorType.ACCENT) :
                        ContextCompat.getColor(getContext(), R.color.textPrimary)
        );
    }

    public void setValue(String value) {
        if (value != null && value.contains("_os_"))
            value = value.split("_os_")[0];
        editText.setText(value);
    }

    public TextView textView() {
        return editText;
    }

    public interface OnSelectedOption {
        void onSelectedOption(String optionName, String optionCode);
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
