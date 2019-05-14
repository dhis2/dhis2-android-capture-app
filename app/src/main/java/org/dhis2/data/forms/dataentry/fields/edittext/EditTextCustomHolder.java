package org.dhis2.data.forms.dataentry.fields.edittext;


import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.dhis2.data.forms.dataentry.fields.FormViewHolder;
import org.dhis2.data.forms.dataentry.fields.RowAction;
import org.dhis2.databinding.FormEditTextCustomBinding;
import org.dhis2.utils.Constants;
import org.dhis2.utils.Preconditions;
import org.hisp.dhis.android.core.common.ValueType;
import org.hisp.dhis.android.core.common.ValueTypeDeviceRenderingModel;
import org.hisp.dhis.android.core.common.ValueTypeRenderingType;

import java.lang.reflect.Type;
import java.util.List;

import androidx.annotation.NonNull;
import io.reactivex.processors.FlowableProcessor;

import static android.content.Context.MODE_PRIVATE;
import static android.text.TextUtils.isEmpty;
import static java.lang.String.valueOf;


/**
 * QUADRAM. Created by frodriguez on 18/01/2018..
 */

final class EditTextCustomHolder extends FormViewHolder {

    private List<String> autoCompleteValues;
    private FormEditTextCustomBinding binding;
    private EditTextViewModel editTextModel;

    EditTextCustomHolder(FormEditTextCustomBinding binding, FlowableProcessor<RowAction> processor, boolean isSearchMode) {
        super(binding);
        this.binding = binding;
        binding.customEdittext.setFocusChangedListener((v, hasFocus) -> {
            if(hasFocus)
                openKeyboard(binding.customEdittext.getEditText());
            if (isSearchMode || (!hasFocus && editTextModel != null && editTextModel.editable() && valueHasChanged())) {
                if (!isEmpty(binding.customEdittext.getEditText().getText())) {
                    checkAutocompleteRendering();
                    editTextModel.withValue(binding.customEdittext.getEditText().getText().toString());
                    processor.onNext(RowAction.create(editTextModel.uid(), binding.customEdittext.getEditText().getText().toString()));

                } else {
                    processor.onNext(RowAction.create(editTextModel.uid(), null));
                }
            }
        });
        binding.customEdittext.setOnEditorActionListener((v, actionId, event) -> {
            binding.customEdittext.getEditText().clearFocus();
            closeKeyboard(binding.customEdittext.getEditText());
            binding.customEdittext.nextFocus(v);
            return false;
        });
    }

    public void update(@NonNull FieldViewModel model) {

        this.editTextModel = (EditTextViewModel) model;

        descriptionText = editTextModel.description();
        binding.customEdittext.setValueType(editTextModel.valueType());
        binding.customEdittext.setEditable(model.editable());
        if(editTextModel.valueType() == ValueType.LONG_TEXT) {
            binding.customEdittext.getInputLayout().getEditText().setSingleLine(false);
            binding.customEdittext.getInputLayout().getEditText().setImeOptions(EditorInfo.IME_FLAG_NO_ENTER_ACTION);
        }
        label = new StringBuilder(editTextModel.label());
        if (editTextModel.mandatory())
            label.append("*");
        binding.customEdittext.setLabel(label.toString());

        if (editTextModel.warning() != null)
            binding.customEdittext.setWarning(editTextModel.warning());
        else if (editTextModel.error() != null)
            binding.customEdittext.setError(editTextModel.error());
        else
            binding.customEdittext.setError(null);


        if (editTextModel.value() != null)
            binding.customEdittext.setText(editTextModel.value());
        else
            binding.customEdittext.setText(null);

        setRenderingType(editTextModel.fieldRendering());

        binding.executePendingBindings();
    }

    private void checkAutocompleteRendering() {
        if (editTextModel.fieldRendering() != null &&
                editTextModel.fieldRendering().type() == ValueTypeRenderingType.AUTOCOMPLETE &&
                !autoCompleteValues.contains(binding.customEdittext.getEditText().getText().toString())) {
            autoCompleteValues.add(binding.customEdittext.getEditText().getText().toString());
            saveListToPreference(editTextModel.uid(), autoCompleteValues);
        }
    }

    @NonNull
    private Boolean valueHasChanged() {
        return !Preconditions.equals(isEmpty(binding.customEdittext.getEditText().getText()) ? "" : binding.customEdittext.getEditText().getText().toString(),
                editTextModel.value() == null ? "" : valueOf(editTextModel.value()));
    }

    private void setRenderingType(ValueTypeDeviceRenderingModel renderingType) {
        if (renderingType != null && renderingType.type() == ValueTypeRenderingType.AUTOCOMPLETE) {
            autoCompleteValues = getListFromPreference(editTextModel.uid());
            ArrayAdapter<String> autoCompleteAdapter = new ArrayAdapter<>(binding.customEdittext.getContext(), android.R.layout.simple_dropdown_item_1line, autoCompleteValues);
            binding.customEdittext.getEditText().setAdapter(autoCompleteAdapter);
        }
    }

    private void saveListToPreference(String key, List<String> list) {
        Gson gson = new Gson();
        String json = gson.toJson(list);
        binding.customEdittext.getContext().getSharedPreferences(Constants.SHARE_PREFS, MODE_PRIVATE).edit().putString(key, json).apply();
    }

    private List<String> getListFromPreference(String key) {
        Gson gson = new Gson();
        String json = binding.customEdittext.getContext().getSharedPreferences(Constants.SHARE_PREFS, MODE_PRIVATE).getString(key, "[]");
        Type type = new TypeToken<List<String>>() {
        }.getType();

        return gson.fromJson(json, type);
    }


    public void dispose() {

    }
}
