package org.dhis2.data.forms.dataentry.fields.edittext;


import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.dhis2.R;
import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.dhis2.data.forms.dataentry.fields.FormViewHolder;
import org.dhis2.data.forms.dataentry.fields.RowAction;
import org.dhis2.databinding.FormEditTextCustomBinding;
import org.dhis2.utils.Constants;
import org.dhis2.utils.Preconditions;
import org.hisp.dhis.android.core.common.ValueTypeDeviceRenderingModel;
import org.hisp.dhis.android.core.common.ValueTypeRenderingType;

import java.lang.reflect.Type;
import java.util.List;

import io.reactivex.processors.FlowableProcessor;

import static android.content.Context.MODE_PRIVATE;
import static android.text.TextUtils.isEmpty;
import static java.lang.String.valueOf;


/**
 * QUADRAM. Created by frodriguez on 18/01/2018..
 */

final class EditTextCustomHolder extends FormViewHolder {

    private final FlowableProcessor<RowAction> processor;
    private final boolean isSearchMode;
    private List<String> autoCompleteValues;
    private FormEditTextCustomBinding formEditTextCustomBinding;
    private EditTextViewModel editTextModel;

    EditTextCustomHolder(FormEditTextCustomBinding binding, FlowableProcessor<RowAction> processor, boolean isSearchMode) {
        super(binding);
        this.formEditTextCustomBinding = binding;
        this.processor = processor;
        this.isSearchMode = isSearchMode;

        formEditTextCustomBinding.customEdittext.setFocusChangedListener((v, hasFocus) -> {
            if (hasFocus) {
                openKeyboard(formEditTextCustomBinding.customEdittext.getEditText());
                setSelectedBackground(isSearchMode);
            } else
                clearBackground(isSearchMode);

            if (isSearchMode || (!hasFocus && editTextModel != null && editTextModel.editable() && valueHasChanged())) {
                sendAction();
            }
        });
        formEditTextCustomBinding.customEdittext.setOnEditorActionListener((v, actionId, event) -> {
            sendAction();
            closeKeyboard(formEditTextCustomBinding.customEdittext.getEditText());
            sendAction();
            return true;
        });
    }

    private void sendAction() {
        if (!isEmpty(formEditTextCustomBinding.customEdittext.getEditText().getText())) {
            checkAutocompleteRendering();
            editTextModel.withValue(formEditTextCustomBinding.customEdittext.getEditText().getText().toString());
            processor.onNext(RowAction.create(editTextModel.uid(), formEditTextCustomBinding.customEdittext.getEditText().getText().toString(), getAdapterPosition()));

        } else {
            processor.onNext(RowAction.create(editTextModel.uid(), null, getAdapterPosition()));
        }

        clearBackground(isSearchMode);
    }

    public void update(@NonNull FieldViewModel model) {
        this.editTextModel = (EditTextViewModel) model;
        formEditTextCustomBinding.customEdittext.setObjectSyle(model.objectStyle());
        label = new StringBuilder(model.label());
        formEditTextCustomBinding.customEdittext.setLabel(model.label(), model.mandatory());
        descriptionText = model.description();
        formEditTextCustomBinding.customEdittext.setDescription(descriptionText);
        formEditTextCustomBinding.customEdittext.setWarning(model.warning(), model.error());
        formEditTextCustomBinding.customEdittext.setText(editTextModel.value());
        formEditTextCustomBinding.customEdittext.setEditable(model.editable());
        formEditTextCustomBinding.customEdittext.setValueType(editTextModel.valueType());

        setRenderingType(editTextModel.fieldRendering());
    }

    private void checkAutocompleteRendering() {
        if (editTextModel.fieldRendering() != null &&
                editTextModel.fieldRendering().type() == ValueTypeRenderingType.AUTOCOMPLETE &&
                !autoCompleteValues.contains(formEditTextCustomBinding.customEdittext.getEditText().getText().toString())) {
            autoCompleteValues.add(formEditTextCustomBinding.customEdittext.getEditText().getText().toString());
            saveListToPreference(editTextModel.uid(), autoCompleteValues);
        }
    }

    @NonNull
    private Boolean valueHasChanged() {
        return !Preconditions.equals(isEmpty(formEditTextCustomBinding.customEdittext.getEditText().getText()) ? "" : formEditTextCustomBinding.customEdittext.getEditText().getText().toString(),
                editTextModel.value() == null ? "" : valueOf(editTextModel.value()));
    }

    private void setRenderingType(ValueTypeDeviceRenderingModel renderingType) {
        if (renderingType != null && renderingType.type() == ValueTypeRenderingType.AUTOCOMPLETE) {
            autoCompleteValues = getListFromPreference(editTextModel.uid());
            ArrayAdapter<String> autoCompleteAdapter = new ArrayAdapter<>(formEditTextCustomBinding.customEdittext.getContext(), android.R.layout.simple_dropdown_item_1line, autoCompleteValues);
            formEditTextCustomBinding.customEdittext.getEditText().setAdapter(autoCompleteAdapter);
        }
    }

    private void saveListToPreference(String key, List<String> list) {
        Gson gson = new Gson();
        String json = gson.toJson(list);
        formEditTextCustomBinding.customEdittext.getContext().getSharedPreferences(Constants.SHARE_PREFS, MODE_PRIVATE).edit().putString(key, json).apply();
    }

    private List<String> getListFromPreference(String key) {
        Gson gson = new Gson();
        String json = formEditTextCustomBinding.customEdittext.getContext().getSharedPreferences(Constants.SHARE_PREFS, MODE_PRIVATE).getString(key, "[]");
        Type type = new TypeToken<List<String>>() {
        }.getType();

        return gson.fromJson(json, type);
    }


    public void dispose() {
        // unused
    }

    @Override
    public void performAction() {
        itemView.setBackground(AppCompatResources.getDrawable(itemView.getContext(), R.drawable.item_selected_bg));
        formEditTextCustomBinding.customEdittext.performOnFocusAction();
    }
}
