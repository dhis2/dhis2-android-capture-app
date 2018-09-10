package org.dhis2.utils.CustomViews;

import android.content.Context;
import android.databinding.BindingAdapter;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.RelativeLayout;

import org.dhis2.BR;
import org.dhis2.R;
import org.dhis2.utils.TextChangedListener;

import org.hisp.dhis.android.core.common.ValueType;

/**
 * QUADRAM. Created by frodriguez on 1/17/2018.
 */

public class CustomTextView extends RelativeLayout implements TextWatcher {

    private static boolean isBgTransparent;
    private EditText editText;
    public static String label;
    public static ValueType valueType;
    private static ViewDataBinding binding;

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
        if (valueType != null)
            switch (valueType) {
                case PHONE_NUMBER:
                    editText.setInputType(InputType.TYPE_CLASS_PHONE);
                    break;
                case EMAIL:
                    editText.setInputType(InputType.TYPE_CLASS_TEXT |
                            InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                    break;
                case TEXT:
                    editText.setInputType(InputType.TYPE_CLASS_TEXT);
                    editText.setLines(1);
                    editText.setEllipsize(TextUtils.TruncateAt.END);
                    break;
                case LETTER:
                    editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
                    editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(1)});
                    return;
                case NUMBER:
                    editText.setInputType(InputType.TYPE_CLASS_NUMBER |
                            InputType.TYPE_NUMBER_FLAG_DECIMAL |
                            InputType.TYPE_NUMBER_FLAG_SIGNED);
                    break;
                case INTEGER_NEGATIVE:
                case INTEGER:
                    editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
                    break;
                case INTEGER_ZERO_OR_POSITIVE:
                case INTEGER_POSITIVE:
                    editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                    editText.setKeyListener(DigitsKeyListener.getInstance(false, false));
                    break;
                case UNIT_INTERVAL:
                    editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                    break;
                default:
                    break;
            }
        binding.setVariable(BR.label, label);

        binding.executePendingBindings();
    }


    public void setTextChangedListener(TextChangedListener listener) {
        this.listener = listener;
    }

    @BindingAdapter(value = {"isBgTransparent", "label", "valueType"})
    public static void setIsBgTransparent(CustomTextView view, boolean mIsBgTransparent, String mLabel, ValueType mValueType) {
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
