package org.dhis2.data.forms.dataentry.fields.edittext;


import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.dhis2.data.forms.dataentry.fields.FormViewHolder;
import org.dhis2.data.forms.dataentry.fields.RowAction;
import org.dhis2.databinding.FormEditTextCustomBinding;
import org.dhis2.utils.Constants;
import org.dhis2.utils.Preconditions;
import org.hisp.dhis.android.core.common.ObjectStyle;
import org.hisp.dhis.android.core.common.ValueTypeDeviceRenderingModel;
import org.hisp.dhis.android.core.common.ValueTypeRenderingType;
import org.hisp.dhis.android.core.d2manager.D2Manager;

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
    private FormEditTextCustomBinding binding;
    private EditTextViewModel editTextModel;

    EditTextCustomHolder(FormEditTextCustomBinding binding, FlowableProcessor<RowAction> processor, boolean isSearchMode, MutableLiveData<String> currentSelection) {
        super(binding);
        this.binding = binding;
        this.processor = processor;
        this.isSearchMode = isSearchMode;
        this.currentUid = currentSelection;

        binding.customEdittext.setFocusChangedListener((v, hasFocus) -> {
           /* if (hasFocus) {
                openKeyboard(binding.customEdittext.getEditText());
                setSelectedBackground(isSearchMode);
            } else
                clearBackground(isSearchMode);*/
           if(!hasFocus){
               clearBackground(isSearchMode);
               binding.customEdittext.getEditText().setFocusable(false);
           }

            if (isSearchMode || (!hasFocus && editTextModel != null && editTextModel.editable() && valueHasChanged())) {
                sendAction();
            }
        });
        binding.customEdittext.setOnEditorActionListener((v, actionId, event) -> {
            sendAction();
//            closeKeyboard(binding.customEdittext.getEditText());
            return true;
        });

        binding.customEdittext.setActivationListener(() -> {
            setSelectedBackground(isSearchMode);
            binding.customEdittext.getEditText().setFocusable(true);
            binding.customEdittext.getEditText().setFocusableInTouchMode(true);
            binding.customEdittext.getEditText().requestFocus();
            openKeyboard(binding.customEdittext.getEditText());
        });
    }

    private void sendAction() {
        if (!isEmpty(binding.customEdittext.getEditText().getText())) {
            checkAutocompleteRendering();
            editTextModel.withValue(binding.customEdittext.getEditText().getText().toString());
            processor.onNext(RowAction.create(editTextModel.uid(), binding.customEdittext.getEditText().getText().toString(), getAdapterPosition()));

        } else {
            processor.onNext(RowAction.create(editTextModel.uid(), null, getAdapterPosition()));
        }

        clearBackground(isSearchMode);
        closeKeyboard(binding.customEdittext.getEditText());

    }

    public void update(@NonNull FieldViewModel model) {
        this.editTextModel = (EditTextViewModel) model;
        fieldUid = model.uid();

        binding.customEdittext.setObjectSyle(model.objectStyle());
        if (model.objectStyle() != null) {
            objectStyle = ObjectStyle.builder()
                    .color(model.objectStyle().color())
                    .icon(model.objectStyle().icon())
                    .uid(model.objectStyle().uid())
                    .objectTable(model.objectStyle().objectTable())
                    .build();
        }
        label = new StringBuilder(model.label());
        binding.customEdittext.setLabel(model.label(), model.mandatory());
        descriptionText = model.description();
        binding.customEdittext.setDescription(descriptionText);

        binding.customEdittext.setText(editTextModel.value());

        binding.customEdittext.setWarning(model.warning(), model.error());

        binding.customEdittext.setEditable(model.editable());

        binding.customEdittext.setValueType(editTextModel.valueType());

        setRenderingType(editTextModel.fieldRendering());

        initFieldFocus();
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
