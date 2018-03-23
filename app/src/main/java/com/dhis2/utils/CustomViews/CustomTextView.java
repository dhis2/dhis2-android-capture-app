package com.dhis2.utils.CustomViews;

import android.content.Context;
import android.content.res.TypedArray;
import android.databinding.BindingAdapter;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
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

import com.dhis2.BR;
import com.dhis2.R;
import com.dhis2.utils.TextChangedListener;

import org.hisp.dhis.android.core.common.ValueType;

/**
 * Created by frodriguez on 1/17/2018.
 */

public class CustomTextView extends RelativeLayout implements TextWatcher {

    private static EditText editText;
    private static ViewDataBinding binding;

    private TextChangedListener listener;

    private LayoutInflater inflater;
    private Boolean isBgTransparent;

    public CustomTextView(Context context) {
        super(context);
        init(context);
    }

    public CustomTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.CustomTextView,
                0, 0);

        try {
            isBgTransparent = a.getBoolean(R.styleable.CustomTextView_isBackgroundTransparent, false);
        } finally {
            a.recycle();
        }
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

        editText = null;
        editText = findViewById(R.id.input_editText);
        editText.addTextChangedListener(this);
    }

    public void setLabel(String label) {
        binding.setVariable(BR.label, label);
        binding.executePendingBindings();
    }

    public void setIsBgTransparent(boolean isBgTransparent) {
        this.isBgTransparent = isBgTransparent;
        setLayout();
//        requestLayout();
    }

    public void setTextChangedListener(TextChangedListener listener) {
        this.listener = listener;
    }

    @BindingAdapter("valueType")
    public static void setValueType(CustomTextView view, ValueType valueType) {
        if (valueType != null)
            switch (valueType) {
                case PHONE_NUMBER:
                    editText.setInputType(InputType.TYPE_CLASS_PHONE);
                    return;
                case EMAIL:
                    editText.setInputType(InputType.TYPE_CLASS_TEXT |
                            InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                    return;
                case TEXT:
                    editText.setInputType(InputType.TYPE_CLASS_TEXT);
                    editText.setLines(1);
                    editText.setEllipsize(TextUtils.TruncateAt.END);
                    return;
                case LETTER:
                    editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
                    editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(1)});
                    return;
                case NUMBER:
                    editText.setInputType(InputType.TYPE_CLASS_NUMBER |
                            InputType.TYPE_NUMBER_FLAG_DECIMAL |
                            InputType.TYPE_NUMBER_FLAG_SIGNED);
                    return;
                case INTEGER_NEGATIVE:
                case INTEGER:
                    editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
                    return;
                case INTEGER_ZERO_OR_POSITIVE:
                case INTEGER_POSITIVE:
                    editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                    editText.setKeyListener(DigitsKeyListener.getInstance(false, false));
                    return;
                case UNIT_INTERVAL:
                    editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                    return;
                default:
                    break;
            }

        editText.invalidate();
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
}
