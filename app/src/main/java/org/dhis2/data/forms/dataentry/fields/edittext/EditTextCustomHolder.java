package org.dhis2.data.forms.dataentry.fields.edittext;

import android.annotation.SuppressLint;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Patterns;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.dhis2.Bindings.Bindings;
import org.dhis2.R;
import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.dhis2.data.forms.dataentry.fields.FormViewHolder;
import org.dhis2.data.forms.dataentry.fields.RowAction;
import org.dhis2.data.tuples.Pair;
import org.dhis2.utils.Constants;
import org.dhis2.utils.Preconditions;
import org.dhis2.utils.custom_views.CustomViewUtils;
import org.dhis2.utils.custom_views.TextInputAutoCompleteTextView;
import org.hisp.dhis.android.core.common.ValueType;
import org.hisp.dhis.android.core.common.ValueTypeDeviceRendering;
import org.hisp.dhis.android.core.common.ValueTypeRenderingType;
import org.hisp.dhis.android.core.program.ProgramStageSectionRenderingType;

import java.lang.reflect.Type;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ViewDataBinding;
import io.reactivex.functions.Predicate;
import io.reactivex.processors.FlowableProcessor;

import static android.content.Context.MODE_PRIVATE;
import static android.text.TextUtils.isEmpty;
import static java.lang.String.valueOf;


/**
 * QUADRAM. Created by frodriguez on 18/01/2018..
 */

final class EditTextCustomHolder extends FormViewHolder {

    private final TextInputLayout inputLayout;
    private TextInputAutoCompleteTextView editText;
    private ImageView icon;
    private List<String> autoCompleteValues;
    private EditTextViewModel editTextModel;
    private Boolean isEditable;

    @SuppressLint("RxLeakedSubscription")
    EditTextCustomHolder(ViewGroup parent, ViewDataBinding binding, FlowableProcessor<RowAction> processor,
                         boolean isBgTransparent, String renderType, ObservableBoolean isEditable) {
        super(binding);
        this.isEditable = isEditable.get();
        editText = binding.getRoot().findViewById(R.id.input_editText);
        icon = binding.getRoot().findViewById(R.id.renderImage);

        inputLayout = binding.getRoot().findViewById(R.id.input_layout);
        if (renderType != null && !renderType.equals(ProgramStageSectionRenderingType.LISTING.name()))
            icon.setVisibility(View.VISIBLE);

        editText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus && editTextModel != null && editTextModel.editable()) {
                if (!isEmpty(editText.getText()) && validate()) {
                    checkAutocompleteRendering();
                    processor.onNext(RowAction.create(editTextModel.uid(), editText.getText().toString()));

                } else
                    processor.onNext(RowAction.create(editTextModel.uid(), null));
            }
        });

        if (this.isEditable) {
            editText.setFocusable(true);
            editText.setFocusableInTouchMode(true);
            editText.setEnabled(true);
        } else {
            editText.setFocusable(false);
            editText.setFocusableInTouchMode(false);
            editText.setEnabled(false);
        }

    }

    private void checkAutocompleteRendering() {
        if (editTextModel.fieldRendering() != null &&
                editTextModel.fieldRendering().type() == ValueTypeRenderingType.AUTOCOMPLETE &&
                !autoCompleteValues.contains(editText.getText().toString())) {
            autoCompleteValues.add(editText.getText().toString());
            saveListToPreference(editTextModel.uid(), autoCompleteValues);
        }
    }

    private void setInputType(ValueType valueType) {

        if (this.isEditable) {
            editText.setFocusable(true);
            editText.setFocusableInTouchMode(true);
            editText.setEnabled(true);
        } else {
            editText.setFocusable(false);
            editText.setFocusableInTouchMode(false);
            editText.setEnabled(false);
        }

        editText.setFilters(new InputFilter[]{});

        if (editTextModel.editable())
            CustomViewUtils.setInputType(valueType, editText);
        else {
            editText.setInputType(InputType.TYPE_NULL);
        }
    }

    @NonNull
    private Predicate<Pair<Boolean, Boolean>> valueHasChangedPredicate() {
        return state -> valueHasChanged();
    }

    @NonNull
    private Boolean valueHasChanged() {
        return !Preconditions.equals(isEmpty(editText.getText()) ? "" : editText.getText().toString(),
                editTextModel.value() == null ? "" : valueOf(editTextModel.value()));
    }

    private void setErrors() {
        if (!isEmpty(editTextModel.warning())) {
            inputLayout.setError(editTextModel.warning());
        } else if (!isEmpty(editTextModel.error())) {
            inputLayout.setError(editTextModel.error());
        } else
            inputLayout.setError(null);
    }

    private void setEnabled() {
        if (this.isEditable) {
            editText.setFocusable(true);
            editText.setFocusableInTouchMode(true);
            editText.setEnabled(true);
        } else {
            editText.setFocusable(false);
            editText.setFocusableInTouchMode(false);
            editText.setEnabled(false);
        }
    }

    public void update(@NonNull FieldViewModel model) {
        this.editTextModel = (EditTextViewModel) model;

        Bindings.setObjectStyle(icon, itemView, editTextModel.objectStyle());

        setEnabled();

        if (editTextModel.value() != null)
            editText.post(() -> editText.setText(valueOf(editTextModel.value())));
        else
            editText.setText(null);

        setErrors();

        editText.setSelection(editText.getText() == null ?
                0 : editText.getText().length());
        if (inputLayout.getHint() == null || !inputLayout.getHint().toString().equals(editTextModel.label())) {
            label = new StringBuilder(editTextModel.label());
            if (editTextModel.mandatory())
                label.append("*");
            inputLayout.setHint(label);

            if (label.length() > 16 || model.description() != null)
                description.setVisibility(View.VISIBLE);
            else
                description.setVisibility(View.GONE);

        }

        descriptionText = editTextModel.description();
        setInputType(editTextModel.valueType());
        setRenderingType(editTextModel.fieldRendering());
    }

    private void setRenderingType(ValueTypeDeviceRendering renderingType) {
        if (renderingType != null && renderingType.type() == ValueTypeRenderingType.AUTOCOMPLETE) {
            autoCompleteValues = getListFromPreference(editTextModel.uid());
            ArrayAdapter<String> autoCompleteAdapter = new ArrayAdapter<>(editText.getContext(), android.R.layout.simple_dropdown_item_1line, autoCompleteValues);
            editText.setAdapter(autoCompleteAdapter);
        }
    }

    public void saveListToPreference(String key, List<String> list) {
        Gson gson = new Gson();
        String json = gson.toJson(list);
        editText.getContext().getSharedPreferences(Constants.SHARE_PREFS, MODE_PRIVATE).edit().putString(key, json).apply();
    }

    private List<String> getListFromPreference(String key) {
        Gson gson = new Gson();
        String json = editText.getContext().getSharedPreferences(Constants.SHARE_PREFS, MODE_PRIVATE).getString(key, "[]");
        Type type = new TypeToken<List<String>>() {
        }.getType();

        return gson.fromJson(json, type);
    }

    private boolean validatePhoneNumber() {
        if (Patterns.PHONE.matcher(editText.getText().toString()).matches())
            return true;
        else {
            inputLayout.setError(editText.getContext().getString(R.string.invalid_phone_number));
            return false;
        }
    }

    private boolean validateEmail() {
        if (Patterns.EMAIL_ADDRESS.matcher(editText.getText().toString()).matches())
            return true;
        else {
            inputLayout.setError(editText.getContext().getString(R.string.invalid_email));
            return false;
        }
    }

    private boolean validateIntegerNegative() {
        if (Integer.valueOf(editText.getText().toString()) < 0)
            return true;
        else {
            inputLayout.setError(editText.getContext().getString(R.string.invalid_negative_number));
            return false;
        }
    }

    private boolean validateIntegerZeroOrPositive() {
        if (Integer.valueOf(editText.getText().toString()) >= 0)
            return true;
        else {
            inputLayout.setError(editText.getContext().getString(R.string.invalid_possitive_zero));
            return false;
        }
    }

    private boolean validateIntegerPositive() {
        if (Integer.valueOf(editText.getText().toString()) > 0)
            return true;
        else {
            inputLayout.setError(editText.getContext().getString(R.string.invalid_possitive));
            return false;
        }
    }

    private boolean validateUnitInterval() {
        if (Float.valueOf(editText.getText().toString()) >= 0 && Float.valueOf(editText.getText().toString()) <= 1)
            return true;
        else {
            inputLayout.setError(editText.getContext().getString(R.string.invalid_interval));
            return false;
        }
    }

    private boolean validatePercentage() {
        if (Float.valueOf(editText.getText().toString()) >= 0 && Float.valueOf(editText.getText().toString()) <= 100)
            return true;
        else {
            inputLayout.setError(editText.getContext().getString(R.string.invalid_percentage));
            return false;
        }
    }

    private boolean validate() {
        switch (editTextModel.valueType()) {
            case PHONE_NUMBER:
                return validatePhoneNumber();
            case EMAIL:
                return validateEmail();
            case INTEGER_NEGATIVE:
                return validateIntegerNegative();
            case INTEGER_ZERO_OR_POSITIVE:
                return validateIntegerZeroOrPositive();
            case INTEGER_POSITIVE:
                return validateIntegerPositive();
            case UNIT_INTERVAL:
                return validateUnitInterval();
            case PERCENTAGE:
                return validatePercentage();
            default:
                return true;
        }
    }


    public void dispose() {
        // do nothing
    }
}
