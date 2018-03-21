package com.dhis2.data.forms.dataentry.fields.edittext;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import com.dhis2.BR;
import com.dhis2.data.forms.dataentry.fields.RowAction;
import com.dhis2.data.tuples.Pair;
import com.dhis2.databinding.FormAgeCustomBinding;
import com.dhis2.databinding.FormEditTextCustomBinding;
import com.dhis2.usescases.searchTrackEntity.SearchTEContractsModule;
import com.dhis2.usescases.searchTrackEntity.formHolders.FormViewHolder;
import com.dhis2.utils.Preconditions;

import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeModel;

import io.reactivex.functions.Predicate;
import io.reactivex.processors.BehaviorProcessor;
import io.reactivex.processors.FlowableProcessor;

import static android.text.TextUtils.isEmpty;
import static java.lang.String.valueOf;


/**
 * Created by frodriguez on 18/01/2018.
 */

public class EditTextCustomHolder extends FormViewHolder {

    private SearchTEContractsModule.Presenter presenter;
    private TrackedEntityAttributeModel bindableObject;
    private final FormEditTextCustomBinding binding;

    private EditText editText;

    @NonNull
    private BehaviorProcessor<EditTextModel> model;

    public EditTextCustomHolder(FormEditTextCustomBinding binding, FlowableProcessor<RowAction> processor) {
        super(binding);

        this.editText = binding.customEdittext.getEditText();
        this.binding = binding;
        model = BehaviorProcessor.create();
        model.subscribe(editTextModel -> {

            binding.setLabel(editTextModel.label());
            binding.setValueType(editTextModel.valueType());

            editText.setText(editTextModel.value() == null ?
                    null : valueOf(editTextModel.value()));

            if (!isEmpty(editTextModel.warning())) {
                editText.setError(editTextModel.warning());
            } else if (!isEmpty(editTextModel.error())) {
                editText.setError(editTextModel.error());
            } else
                editText.setError(null);

            binding.executePendingBindings();
        });

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (valueHasChanged())
                    processor.onNext(
                            RowAction.create(model.getValue().uid(), charSequence.toString())
                    );
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


    }

    public void bind(SearchTEContractsModule.Presenter presenter, TrackedEntityAttributeModel bindableObject) {
        this.presenter = presenter;
        this.bindableObject = bindableObject;
//        binding.setVariable(BR.attribute, bindableObject);
        binding.executePendingBindings();
        binding.setLabel(bindableObject.displayShortName());
        binding.setValueType(bindableObject.valueType());
    }

    private void modelFormEditText(FormEditTextCustomBinding binding, FlowableProcessor<RowAction> processor) {


    }

    private void modelFormAge(FormAgeCustomBinding binding) {

    }

    @NonNull
    private Predicate<Pair<Boolean, Boolean>> valueHasChangedPredicate() {
        return state -> valueHasChanged();
    }

    @NonNull
    private Boolean valueHasChanged() {
        return !Preconditions.equals(isEmpty(editText.getText()) ? "" : editText.getText().toString(),
                model.getValue().value() == null ? "" : valueOf(model.getValue().value()));
    }

    void update(@NonNull EditTextModel editTextModel) {
        model.onNext(editTextModel);
    }
}
