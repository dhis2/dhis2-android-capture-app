package org.dhis2.data.forms.dataentry.fields.edittext;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.util.AttributeSet;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;

import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.dhis2.BR;
import org.dhis2.Components;
import org.dhis2.R;
import org.dhis2.utils.ColorUtils;
import org.dhis2.utils.Constants;
import org.dhis2.utils.ObjectStyleUtils;
import org.dhis2.utils.Preconditions;
import org.dhis2.utils.ValidationUtils;
import org.dhis2.utils.Validator;
import org.dhis2.utils.customviews.CustomDialog;
import org.dhis2.utils.customviews.FieldLayout;
import org.dhis2.utils.customviews.TextInputAutoCompleteTextView;
import org.hisp.dhis.android.core.common.ObjectStyle;
import org.hisp.dhis.android.core.common.ValueType;
import org.hisp.dhis.android.core.common.ValueTypeDeviceRendering;
import org.hisp.dhis.android.core.common.ValueTypeRenderingType;
import org.hisp.dhis.android.core.program.ProgramStageSectionRenderingType;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

import timber.log.Timber;

import static android.content.Context.MODE_PRIVATE;
import static android.text.TextUtils.isEmpty;
import static android.view.inputmethod.EditorInfo.IME_ACTION_DONE;
import static android.view.inputmethod.EditorInfo.IME_ACTION_NEXT;
import static java.lang.String.valueOf;
import static org.dhis2.Bindings.ViewExtensionsKt.closeKeyboard;
import static org.dhis2.Bindings.ViewExtensionsKt.openKeyboard;


public class CustomTextView extends FieldLayout {

    String urlStringPattern = "^(http://www\\.|https://www\\.|http://|https://)[a-z0-9]+([\\-.]{1}[a-z0-9]+)*\\.[a-z]{2,5}(:[0-9]{1,5})?(/.*)?$";
    Pattern urlPattern = Pattern.compile(urlStringPattern);

    private boolean isBgTransparent;
    private TextInputAutoCompleteTextView editText;
    private ImageView icon;
    private ImageView descIcon;
    private String label;
    private ValueType valueType;
    private ViewDataBinding binding;
    private LayoutInflater inflater;
    private TextInputLayout inputLayout;
    private boolean isLongText;
    private View descriptionLabel;
    private TextView labelText;
    private ImageView clearButton;
    private Map<ValueType, Validator> validators;
    private Validator validator;

    private List<String> autoCompleteValues;

    private EditTextViewModel viewModel;

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
        super.init(context);
        inflater = LayoutInflater.from(context);
        validators = ((Components) context.getApplicationContext()).appComponent().injectValidators();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setLayout(boolean isBgTransparent, boolean isLongText) {
        this.isBgTransparent = isBgTransparent;
        this.isLongText = isLongText;

        if (isBgTransparent && !isLongText)
            binding = DataBindingUtil.inflate(inflater, R.layout.custom_text_view, this, true);
        else if (!isBgTransparent && !isLongText)
            binding = DataBindingUtil.inflate(inflater, R.layout.custom_text_view_accent, this, true);
        else if (isBgTransparent)
            binding = DataBindingUtil.inflate(inflater, R.layout.custom_long_text_view, this, true);
        else
            binding = DataBindingUtil.inflate(inflater, R.layout.custom_long_text_view_accent, this, true);

        this.setFocusableInTouchMode(false);
        this.setFocusable(false);

        inputLayout = findViewById(R.id.input_layout);
        editText = findViewById(R.id.input_editText);
        icon = findViewById(R.id.renderImage);
        descriptionLabel = binding.getRoot().findViewById(R.id.descriptionLabel);
        labelText = findViewById(R.id.label);
        descIcon = findViewById(R.id.descIcon);
        editText.setOnTouchListener((v, event) -> {
            if (MotionEvent.ACTION_UP == event.getAction())
                viewModel.onItemClick();
            return false;
        });
        editText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus && viewModel.isSearchMode()) {
                openKeyboard(v);
            }
            if (!hasFocus && valueHasChanged()) {
                if (validate()) {
                    inputLayout.setError(null);
                }
                sendAction();
                validateRegex();
            }
        });

        editText.setOnEditorActionListener((v, actionId, event) -> {
            switch (actionId) {
                case IME_ACTION_NEXT:
                    viewModel.onNext();
                    return true;
                case IME_ACTION_DONE:
                    closeKeyboard(v);
                    return true;
                default:
                    return false;
            }

        });

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (valueHasChanged() && editText.hasFocus()) {
                    viewModel.onTextChange(charSequence.toString());
                }
                if (isLongText) {
                    updateDeleteVisibility(findViewById(R.id.clear_button));
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        if (isLongText) {
            clearButton = findViewById(R.id.clear_button);
        }
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
    }

    private void configureViews() {
        editText.setFilters(new InputFilter[]{});
        editText.setMaxLines(1);
        editText.setVerticalScrollBarEnabled(false);
        descIcon.setVisibility(View.GONE);

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
                    editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(50000)});
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
                    clearButton.setOnClickListener(v -> {
                        editText.getText().clear();
                        sendAction();
                        updateDeleteVisibility(findViewById(R.id.clear_button));
                    });
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
                    descIcon.setVisibility(VISIBLE);
                    descIcon.setImageResource(R.drawable.ic_form_percentage);
                    editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                    break;
                case URL:
                    editText.setInputType(InputType.TYPE_TEXT_VARIATION_WEB_EDIT_TEXT);
                    break;
                default:
                    break;
            }
        editText.setCompoundDrawablePadding(24);
        binding.executePendingBindings();
    }

    public void setValueType(ValueType valueType) {
        this.valueType = valueType;
        this.validator = validators.get(valueType);

        configureViews();
    }

    public void setEditable(Boolean editable) {
        editText.setEnabled(editable);
        editText.setTextColor(
                !isBgTransparent && !isLongText ? ColorUtils.getPrimaryColor(getContext(), ColorUtils.ColorType.ACCENT) :
                        ContextCompat.getColor(getContext(), R.color.textPrimary)
        );

        if (findViewById(R.id.clear_button) != null) {
            findViewById(R.id.clear_button).setVisibility(editable ? View.VISIBLE : View.GONE);
        }

        setEditable(editable, labelText,
                inputLayout, descIcon, descriptionLabel, findViewById(R.id.clear_button));

        updateDeleteVisibility(findViewById(R.id.clear_button));
    }

    public void setWarning(String warning, String error) {
        if (!isEmpty(error)) {
            inputLayout.setErrorTextAppearance(R.style.error_appearance);
            inputLayout.setError(error);
            inputLayout.setErrorTextColor(ColorStateList.valueOf(ResourcesCompat.getColor(getResources(), R.color.error_color, null)));
        } else if (!isEmpty(warning)) {
            inputLayout.setErrorTextAppearance(R.style.warning_appearance);
            inputLayout.setError(warning);
            inputLayout.setErrorTextColor(ColorStateList.valueOf(ResourcesCompat.getColor(getResources(), R.color.warning_color, null)));
        } else
            inputLayout.setError(null);
    }

    public void setText(String text) {
        editText.setText(text);
        editText.setSelection(editText.getText() == null ?
                0 : editText.getText().length());
        updateDeleteVisibility(findViewById(R.id.clear_button));
    }

    public void setLabel(String label) {
        if (inputLayout.getHint() == null || !inputLayout.getHint().toString().equals(label)) {
            this.label = label;
            inputLayout.setHint(null);
            binding.setVariable(BR.label, label);
        }
    }

    public void setHint(String hint) {
        binding.setVariable(BR.fieldHint, hint);
    }

    public TextInputAutoCompleteTextView getEditText() {
        return editText;
    }

    public TextInputLayout getInputLayout() {
        return inputLayout;
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
                    if (validator.validate(editText.getText().toString()))
                        return true;
                    else {
                        inputLayout.setError(editText.getContext().getString(R.string.invalid_negative_number));
                        return false;
                    }
                case INTEGER_ZERO_OR_POSITIVE:
                    if (validator.validate(editText.getText().toString()))
                        return true;
                    else {
                        inputLayout.setError(editText.getContext().getString(R.string.invalid_possitive_zero));
                        return false;
                    }
                case INTEGER_POSITIVE:
                    if (validator.validate(editText.getText().toString()))
                        return true;
                    else {
                        inputLayout.setError(editText.getContext().getString(R.string.invalid_possitive));
                        return false;
                    }
                case UNIT_INTERVAL:
                    if (Float.parseFloat(editText.getText().toString()) >= 0 && Float.parseFloat(editText.getText().toString()) <= 1)
                        return true;
                    else {
                        inputLayout.setError(editText.getContext().getString(R.string.invalid_interval));
                        return false;
                    }
                case PERCENTAGE:
                    if (Float.parseFloat(editText.getText().toString()) >= 0 && Float.parseFloat(editText.getText().toString()) <= 100)
                        return true;
                    else {
                        inputLayout.setError(editText.getContext().getString(R.string.invalid_percentage));
                        return false;
                    }
                case URL:
                    if (urlPattern.matcher(editText.getText().toString()).matches()) {
                        inputLayout.setError(null);
                        return true;
                    } else {
                        inputLayout.setError(getContext().getString(R.string.validation_url));
                        return false;
                    }
                case INTEGER:
                    if (validator.validate(editText.getText().toString()))
                        return true;
                    else {
                        inputLayout.setError(editText.getContext().getString(R.string.invalid_integer));
                        return false;
                    }
                case NUMBER:
                    if (validator.validate(editText.getText().toString()))
                        return true;
                    else {
                        inputLayout.setError(editText.getContext().getString(R.string.formatting_error));
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

    public void setObjectStyle(ObjectStyle objectStyle) {
        Drawable styleIcon = ObjectStyleUtils.getIconResource(editText.getContext(), objectStyle.icon(), -1);
        icon.setImageDrawable(styleIcon);
        int colorResource = ObjectStyleUtils.getColorResource(
                editText.getContext(),
                objectStyle.color(),
                isBgTransparent ? R.color.default_field_icon_color : R.color.colorAccent
        );
        descIcon.setColorFilter(colorResource);
    }

    private void setOnLongActionListener() {
        if (!editText.isFocusable()) {
            editText.setOnLongClickListener(view -> {
                ClipboardManager clipboard = (ClipboardManager) binding.getRoot().getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                try {
                    if (!((TextInputAutoCompleteTextView) view).getText().toString().equals("")) {
                        ClipData clip = ClipData.newPlainText("copy", ((TextInputAutoCompleteTextView) view).getText());
                        clipboard.setPrimaryClip(clip);
                        Toast.makeText(binding.getRoot().getContext(),
                                binding.getRoot().getContext().getString(R.string.copied_text), Toast.LENGTH_LONG).show();
                    }
                    return true;
                } catch (Exception e) {
                    Timber.e(e);
                    return false;
                }
            });
        }
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

    @Override
    protected boolean hasValue() {
        return editText.getText() != null && !editText.getText().toString().isEmpty();
    }

    @Override
    protected boolean isEditable() {
        return editText.isEnabled();
    }

    public void setViewModel(EditTextViewModel viewModel) {
        this.viewModel = viewModel;

        if (binding == null) {
            setLayout(viewModel.isBackgroundTransparent(), viewModel.isLongText());
        }

        binding.setVariable(BR.keyboardActionType, viewModel.keyBoardAction());

        setRenderType(viewModel.renderType());
        setValueType(viewModel.valueType());
        setObjectStyle(viewModel.objectStyle());
        setLabel(viewModel.getFormattedLabel());
        setHint(viewModel.hint());
        binding.setVariable(BR.legend, viewModel.legendValue());
        String description = viewModel.description();

        if (viewModel.url() != null){
            description = description + "\n" + viewModel.url()  ;
        }

        setDescription(description);
        setText(viewModel.value());
        setWarning(viewModel.warning(), viewModel.error());
        if (!viewModel.isSearchMode() && viewModel.value() != null &&
                !Objects.requireNonNull(viewModel.value()).isEmpty() &&
                viewModel.fieldMask() != null &&
                !Objects.requireNonNull(viewModel.value()).matches(Objects.requireNonNull(viewModel.fieldMask()))) {
            setWarning(binding.getRoot().getContext().getString(R.string.wrong_pattern), "");
        }
        setEditable(viewModel.editable());
        setRenderingType(viewModel.fieldRendering(), viewModel.uid());
        setOnLongActionListener();
        binding.setVariable(BR.focus, viewModel.activated());
    }

    private void setRenderingType(ValueTypeDeviceRendering renderingType, String uid) {
        if (renderingType != null && renderingType.type() == ValueTypeRenderingType.AUTOCOMPLETE) {
            autoCompleteValues = getListFromPreference(uid);
            ArrayAdapter<String> autoCompleteAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, autoCompleteValues);
            getEditText().setAdapter(autoCompleteAdapter);
        }
    }

    private List<String> getListFromPreference(String key) {
        Gson gson = new Gson();
        String json = getContext().getSharedPreferences(Constants.SHARE_PREFS, MODE_PRIVATE).getString(key, "[]");
        Type type = new TypeToken<List<String>>() {
        }.getType();

        return gson.fromJson(json, type);
    }

    private void sendAction() {
        if (!isEmpty(getEditText().getText())) {
            checkAutocompleteRendering();
            String value = ValidationUtils.validate(viewModel.valueType(), getEditText().getText().toString());
            String error = (String) inputLayout.getError();
            viewModel.onTextFilled(value, error);
        } else {
            viewModel.onTextFilled(null, null);
        }
    }

    private void checkAutocompleteRendering() {
        if (viewModel.fieldRendering() != null &&
                viewModel.fieldRendering().type() == ValueTypeRenderingType.AUTOCOMPLETE &&
                !autoCompleteValues.contains(getEditText().getText().toString())) {
            autoCompleteValues.add(getEditText().getText().toString());
            saveListToPreference(viewModel.uid(), autoCompleteValues);
        }
    }

    private void saveListToPreference(String key, List<String> list) {
        Gson gson = new Gson();
        String json = gson.toJson(list);
        getContext().getSharedPreferences(Constants.SHARE_PREFS, MODE_PRIVATE).edit().putString(key, json).apply();
    }

    private void validateRegex() {
        if (!viewModel.isSearchMode())
            if (viewModel.fieldMask() != null && !getEditText().getText().toString().isEmpty() &&
                    !getEditText().getText().toString().matches(Objects.requireNonNull(viewModel.fieldMask())))
                setWarning(binding.getRoot().getContext().getString(R.string.wrong_pattern), "");
            else
                setWarning(viewModel.warning(), viewModel.error());
    }

    private Boolean valueHasChanged() {
        return !Preconditions.equals(isEmpty(getEditText().getText()) ? "" : getEditText().getText().toString(),
                viewModel.value() == null ? "" : valueOf(viewModel.value()));
    }

    public void setBackgroundColor(@ColorInt int color) {
        inputLayout.setBackgroundColor(color);
    }
}
