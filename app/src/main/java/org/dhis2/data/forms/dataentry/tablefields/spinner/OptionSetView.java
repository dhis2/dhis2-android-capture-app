package org.dhis2.data.forms.dataentry.tablefields.spinner;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import org.dhis2.R;
import org.dhis2.commons.resources.ColorType;
import org.dhis2.commons.resources.ColorUtils;
import org.dhis2.utils.customviews.FieldLayout;
import org.dhis2.utils.customviews.OptionSetOnClickListener;
import org.hisp.dhis.android.core.option.Option;

import javax.inject.Inject;

public class OptionSetView extends FieldLayout implements OptionSetOnClickListener {
    private TextView editText;
    private OnSelectedOption listener;

    @Inject
    ColorUtils colorUtils;

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
                !isBgTransparent ? colorUtils.getPrimaryColor(getContext(), ColorType.ACCENT) :
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
