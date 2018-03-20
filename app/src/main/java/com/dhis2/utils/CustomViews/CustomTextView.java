package com.dhis2.utils.CustomViews;

import android.content.Context;
import android.databinding.BindingAdapter;
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

import com.dhis2.R;
import com.dhis2.databinding.CustomTextViewBinding;
import com.dhis2.utils.TextChangedListener;

import org.hisp.dhis.android.core.common.ValueType;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeModel;

/**
 * Created by frodriguez on 1/17/2018.
 */

public class CustomTextView extends RelativeLayout implements TextWatcher {

    private static EditText editText;
    private CustomTextViewBinding binding;

    private TextChangedListener listener;

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
        LayoutInflater inflater = LayoutInflater.from(context);
        binding = CustomTextViewBinding.inflate(inflater, this, true);
        editText = findViewById(R.id.button);
        editText.addTextChangedListener(this);
    }

    public void setLabel(String label) {
        binding.setLabel(label);
        binding.executePendingBindings();
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
