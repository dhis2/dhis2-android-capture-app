package org.dhis2.utils.custom_views;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.google.android.material.textfield.TextInputLayout;

import org.dhis2.BR;
import org.dhis2.R;
import org.dhis2.utils.TextChangedListener;
import org.hisp.dhis.android.core.common.ValueType;

import androidx.databinding.BindingAdapter;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;

/**
 * QUADRAM. Created by frodriguez on 1/17/2018.
 */

public class CustomTextView extends RelativeLayout implements TextWatcher {

    private boolean isBgTransparent;
    private EditText editText;
    private String label;
    private ValueType valueType;
    private ViewDataBinding binding;

    private TextChangedListener listener;

    private LayoutInflater inflater;
    private TextInputLayout inputLayout;

    public CustomTextView(Context context) {
        super(context);
        init(context);
    }

    public CustomTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CustomTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        inflater = LayoutInflater.from(context);
    }

    private void setLayout() {
        if (isBgTransparent)
            binding = DataBindingUtil.inflate(inflater, R.layout.custom_text_view, this, true);
        else
            binding = DataBindingUtil.inflate(inflater, R.layout.custom_text_view_accent, this, true);

        inputLayout = findViewById(R.id.input_layout);
        editText = findViewById(R.id.input_editText);
        editText.addTextChangedListener(this);
        configureViews();
    }

    private void configureViews() {
        if (valueType != null) {
            CustomViewUtils.setInputType(valueType, editText);
        }

        binding.setVariable(BR.label, label);
        binding.executePendingBindings();
    }


    public void setTextChangedListener(TextChangedListener listener) {
        this.listener = listener;
    }

    @BindingAdapter(value = {"isBgTransparent", "label", "valueType"})
    public void setIsBgTransparent(CustomTextView view, boolean mIsBgTransparent, String mLabel, ValueType mValueType) {
        isBgTransparent = mIsBgTransparent;
        label = mLabel;
        valueType = mValueType;
        view.setLayout();
    }


    @Override
    public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
        if (listener != null)
            listener.beforeTextChanged(charSequence, start, count, after);
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
        if (listener != null)
            listener.onTextChanged(charSequence, start, before, count);
    }

    @Override
    public void afterTextChanged(Editable editable) {
        if (listener != null)
            listener.afterTextChanged(editable);
    }

    public EditText getEditText() {
        return editText;
    }

    public TextInputLayout getInputLayout() {
        return inputLayout;
    }
}
