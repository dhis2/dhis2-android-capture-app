package org.dhis2.data.forms.dataentry.fields.edittext;


import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.dhis2.R;
import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.dhis2.data.forms.dataentry.fields.FormViewHolder;
import org.dhis2.data.forms.dataentry.fields.RowAction;
import org.dhis2.databinding.FormEditTextCustomBinding;
import org.dhis2.utils.Constants;
import org.dhis2.utils.Preconditions;
import org.dhis2.utils.ValidationUtils;
import org.dhis2.utils.customviews.TextInputAutoCompleteTextView;
import org.hisp.dhis.android.core.arch.handlers.internal.Handler;
import org.hisp.dhis.android.core.common.ValueTypeDeviceRendering;
import org.hisp.dhis.android.core.common.ValueTypeRenderingType;

import java.lang.reflect.Type;
import java.util.List;

import io.reactivex.processors.FlowableProcessor;
import timber.log.Timber;

import static android.content.Context.MODE_PRIVATE;
import static android.text.TextUtils.isEmpty;
import static java.lang.String.valueOf;


public class EditTextCustomHolder extends FormViewHolder {

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
            if(!hasFocus){
                clearBackground(isSearchMode);
                binding.customEdittext.getEditText().setFocusable(false);
            }

            if (isSearchMode || (!hasFocus && editTextModel != null && editTextModel.editable())) {
                if(isSearchMode || valueHasChanged())
                    sendAction();
                else
                    closeKeyboard(binding.customEdittext.getEditText());
            }
            validateRegex();
        });
        binding.customEdittext.setOnEditorActionListener((v, actionId, event) -> {
            binding.customEdittext.getEditText().clearFocus();
            sendAction();
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
            String value = ValidationUtils.validate(editTextModel.valueType(), binding.customEdittext.getEditText().getText().toString());
            processor.onNext(RowAction.create(editTextModel.uid(), value, getAdapterPosition()));

        } else {
            processor.onNext(RowAction.create(editTextModel.uid(), null, getAdapterPosition()));
        }

        clearBackground(isSearchMode);
        closeKeyboard(binding.customEdittext.getEditText());

    }

    public void update(@NonNull FieldViewModel model) {
        this.editTextModel = (EditTextViewModel) model;
        fieldUid = model.uid();

        binding.customEdittext.setValueType(editTextModel.valueType());

        binding.customEdittext.setObjectSyle(model.objectStyle());
        if (model.objectStyle() != null) {
            objectStyle = model.objectStyle();
        }
        label = new StringBuilder(model.label());
        binding.customEdittext.setLabel(model.label(), model.mandatory());
        descriptionText = model.description();
        binding.customEdittext.setDescription(descriptionText);

        binding.customEdittext.setText(editTextModel.value());

        binding.customEdittext.setWarning(model.warning(), model.error());

        if (!isSearchMode && model.value() != null && !model.value().isEmpty()
                && editTextModel.fieldMask() != null && !model.value().matches(editTextModel.fieldMask()))
            binding.customEdittext.setWarning(binding.getRoot().getContext().getString(R.string.wrong_pattern), "");

        binding.customEdittext.setEditable(model.editable());

        setRenderingType(editTextModel.fieldRendering());

        initFieldFocus();

        setLongClick();
    }

    private void checkAutocompleteRendering() {
        if (editTextModel.fieldRendering() != null &&
                editTextModel.fieldRendering().type() == ValueTypeRenderingType.AUTOCOMPLETE &&
                !autoCompleteValues.contains(binding.customEdittext.getEditText().getText().toString())) {
            autoCompleteValues.add(binding.customEdittext.getEditText().getText().toString());
            saveListToPreference(editTextModel.uid(), autoCompleteValues);
        }
    }

    private void validateRegex() {
        if (!isSearchMode)
            if (editTextModel.fieldMask() != null && !binding.customEdittext.getEditText().getText().toString().isEmpty() &&
                    !binding.customEdittext.getEditText().getText().toString().matches(editTextModel.fieldMask()))
                binding.customEdittext.setWarning(binding.getRoot().getContext().getString(R.string.wrong_pattern), "");
            else
                binding.customEdittext.setWarning(editTextModel.warning(), editTextModel.error());
    }

    @NonNull
    private Boolean valueHasChanged() {
        return !Preconditions.equals(isEmpty(binding.customEdittext.getEditText().getText()) ? "" : binding.customEdittext.getEditText().getText().toString(),
                editTextModel.value() == null ? "" : valueOf(editTextModel.value())) || editTextModel.error() != null;
    }

    private void setRenderingType(ValueTypeDeviceRendering renderingType) {
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

    private void setLongClick() {
        binding.customEdittext.setOnLongActionListener(view -> {
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
