package org.dhis2.utils.custom_views;

import android.content.Context;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.text.method.DigitsKeyListener;
import android.util.AttributeSet;
import android.util.Patterns;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;

import com.google.android.material.textfield.TextInputLayout;

import org.dhis2.BR;
import org.dhis2.R;
import org.hisp.dhis.android.core.common.ObjectStyleModel;
import org.hisp.dhis.android.core.common.ValueType;
import org.hisp.dhis.android.core.program.ProgramStageSectionRenderingType;

import static android.text.TextUtils.isEmpty;
import static org.dhis2.Bindings.BindingAdapterKt.setObjectStyle;

/**
 * QUADRAM. Created by frodriguez on 1/17/2018.
 */

public class CustomTextView extends FieldLayout implements View.OnFocusChangeListener {

    private boolean isBgTransparent;
    private TextInputAutoCompleteTextView editText;
    private ImageView icon;
    private String label;
    private ValueType valueType;
    private ViewDataBinding binding;

    private OnFocusChangeListener listener;

    private LayoutInflater inflater;
    private TextInputLayout inputLayout;
    private boolean isLongText;
    private View descriptionLabel;

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

    public void init(Context context) {
        inflater = LayoutInflater.from(context);
    }

    @Override
    public void performOnFocusAction() {
        editText.requestFocus();
        editText.performClick();
    }

    private void setLayout() {
        if (isBgTransparent && !isLongText)
            binding = DataBindingUtil.inflate(inflater, R.layout.custom_text_view, this, true);
        else if (!isBgTransparent && !isLongText)
            binding = DataBindingUtil.inflate(inflater, R.layout.custom_text_view_accent, this, true);
        else if (isBgTransparent && isLongText)
            binding = DataBindingUtil.inflate(inflater, R.layout.custom_long_text_view, this, true);
        else
            binding = DataBindingUtil.inflate(inflater, R.layout.custom_long_text_view_accent, this, true);

        inputLayout = findViewById(R.id.input_layout);
        editText = findViewById(R.id.input_editText);
        icon = findViewById(R.id.renderImage);
        descriptionLabel = binding.getRoot().findViewById(R.id.descriptionLabel);

        editText.setOnFocusChangeListener(this);
    }

    public void setDescription(String description) {
        descriptionLabel.setVisibility(label.length() > 16 || description != null ? View.VISIBLE : View.GONE);
    }

    private void configureViews() {

        editText.setFilters(new InputFilter[]{});

        TextInputLayout.LayoutParams lp = new TextInputLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.weight = 1f;
        inputLayout.setLayoutParams(lp);
        editText.setMaxLines(1);
        editText.setVerticalScrollBarEnabled(false);

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
                case LONG_TEXT:
                    editText.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
                    editText.setMaxLines(Integer.MAX_VALUE);
                    editText.setEllipsize(null);
                    editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
                    editText.setVerticalScrollBarEnabled(true);
                    editText.setScrollBarStyle(View.SCROLLBARS_INSIDE_INSET);
                    editText.setOverScrollMode(View.OVER_SCROLL_ALWAYS);
                    editText.setSingleLine(false);
                    editText.setImeOptions(EditorInfo.IME_FLAG_NO_ENTER_ACTION);
                    break;
                case LETTER:
                    editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
                    editText.setFilters(new InputFilter[]{
                            new InputFilter.LengthFilter(1),
                            (source, start, end, dest, dstart, dend) -> {
                                if (source.equals(""))
                                    return source;
                                if (source.toString().matches("[a-zA-Z]"))
                                    return source;
                                return "";
                            }});
                    break;
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
                case PERCENTAGE:
                    editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                    break;
                case URL:
                    editText.setInputType(InputType.TYPE_TEXT_VARIATION_WEB_EDIT_TEXT);
                    break;
                default:
                    break;
            }

        binding.executePendingBindings();
    }

    public void setLayoutData(boolean isBgTransparent, boolean isLongText) {
        this.isBgTransparent = isBgTransparent;
        this.isLongText = isLongText;
        setLayout();
    }

    public void setValueType(ValueType valueType) {
        this.valueType = valueType;
        configureViews();
    }

    public void setEditable(Boolean editable) {
        editText.setFocusable(editable);
        editText.setFocusableInTouchMode(editable);
        editText.setEnabled(editable);
    }

    public void setWarning(String warning, String error) {
        if (!isEmpty(error)) {
            inputLayout.setErrorTextAppearance(R.style.error_appearance);
            inputLayout.setError(error);
        } else if (!isEmpty(warning)) {
            inputLayout.setErrorTextAppearance(R.style.warning_appearance);
            inputLayout.setError(warning);
        } else
            inputLayout.setError(null);
    }

    public void setText(String text) {
        editText.setText(text);
        editText.setSelection(editText.getText() == null ?
                0 : editText.getText().length());
    }

    public void setLabel(String label, boolean mandatory) {
        if (inputLayout.getHint() == null || !inputLayout.getHint().toString().equals(label)) {
            StringBuilder labelBuilder = new StringBuilder(label);
            if (mandatory)
                labelBuilder.append("*");
            this.label = labelBuilder.toString();
            inputLayout.setHint(this.label);
            binding.setVariable(BR.label, this.label);
        }
    }

    public TextInputAutoCompleteTextView getEditText() {
        return editText;
    }

    public TextInputLayout getInputLayout() {
        return inputLayout;
    }

    public void setFocusChangedListener(OnFocusChangeListener listener) {
        this.listener = listener;
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (listener != null && validate())
            listener.onFocusChange(v, hasFocus);
    }

    private boolean validate() {
        if (editText.getText() != null && !isEmpty(editText.getText())) {
            switch (valueType) {
                case PHONE_NUMBER:
                    if (Patterns.PHONE.matcher(editText.getText().toString()).matches())
                        return true;
                    else {
                        inputLayout.setError(editText.getContext().getString(R.string.invalid_phone_number));
                        return false;
                    }
                case EMAIL:
                    if (Patterns.EMAIL_ADDRESS.matcher(editText.getText().toString()).matches())
                        return true;
                    else {
                        inputLayout.setError(editText.getContext().getString(R.string.invalid_email));
                        return false;
                    }
                case INTEGER_NEGATIVE:
                    if (Integer.valueOf(editText.getText().toString()) < 0)
                        return true;
                    else {
                        inputLayout.setError(editText.getContext().getString(R.string.invalid_negative_number));
                        return false;
                    }
                case INTEGER_ZERO_OR_POSITIVE:
                    if (editText.getText() != null &&
                            Integer.valueOf(editText.getText().toString()) >= 0)
                        return true;
                    else {
                        inputLayout.setError(editText.getContext().getString(R.string.invalid_possitive_zero));
                        return false;
                    }
                case INTEGER_POSITIVE:
                    if (Integer.valueOf(editText.getText().toString()) > 0)
                        return true;
                    else {
                        inputLayout.setError(editText.getContext().getString(R.string.invalid_possitive));
                        return false;
                    }
                case UNIT_INTERVAL:
                    if (Float.valueOf(editText.getText().toString()) >= 0 && Float.valueOf(editText.getText().toString()) <= 1)
                        return true;
                    else {
                        inputLayout.setError(editText.getContext().getString(R.string.invalid_interval));
                        return false;
                    }
                case PERCENTAGE:
                    if (Float.valueOf(editText.getText().toString()) >= 0 && Float.valueOf(editText.getText().toString()) <= 100)
                        return true;
                    else {
                        inputLayout.setError(editText.getContext().getString(R.string.invalid_percentage));
                        return false;
                    }
                default:
                    return true;
            }
        }
        return true;
    }

    public void setRenderType(String renderType) {
        if (renderType != null && !renderType.equals(ProgramStageSectionRenderingType.LISTING.name()))
            icon.setVisibility(View.VISIBLE);
    }

    public void setOnEditorActionListener(TextView.OnEditorActionListener actionListener) {
        editText.setOnEditorActionListener((v, actionId, event) -> {
            if (validate())
                return actionListener.onEditorAction(v, actionId, event);
            return true;
        });
    }


    public void setObjectSyle(ObjectStyleModel objectStyle) {
        setObjectStyle(icon, this, objectStyle);
    }
}
