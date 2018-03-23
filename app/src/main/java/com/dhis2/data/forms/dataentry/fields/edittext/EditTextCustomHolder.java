package com.dhis2.data.forms.dataentry.fields.edittext;

import android.support.annotation.NonNull;
import android.text.Editable;

import com.dhis2.data.forms.dataentry.fields.RowAction;
import com.dhis2.data.tuples.Pair;
import com.dhis2.databinding.FormEditTextCustomBinding;
import com.dhis2.usescases.searchTrackEntity.SearchTEContractsModule;
import com.dhis2.usescases.searchTrackEntity.formHolders.FormViewHolder;
import com.dhis2.utils.Preconditions;
import com.dhis2.utils.TextChangedListener;

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

    private final FormEditTextCustomBinding binding;
    FlowableProcessor<RowAction> processor;

    @NonNull
    private BehaviorProcessor<EditTextModel> model;

    public EditTextCustomHolder(FormEditTextCustomBinding binding, FlowableProcessor<RowAction> processor) {
        super(binding);

        this.binding = binding;
        this.processor = processor;

        model = BehaviorProcessor.create();
        model.subscribe(editTextModel -> {

            binding.setLabel(editTextModel.label());
            binding.setValueType(editTextModel.valueType());

            binding.customEdittext.getEditText().setText(editTextModel.value() == null ?
                    null : valueOf(editTextModel.value()));

            if (!isEmpty(editTextModel.warning())) {
                binding.customEdittext.getEditText().setError(editTextModel.warning());
            } else if (!isEmpty(editTextModel.error())) {
                binding.customEdittext.getEditText().setError(editTextModel.error());
            } else
                binding.customEdittext.getEditText().setError(null);

            binding.executePendingBindings();
        });

        binding.customEdittext.setTextChangedListener(new TextChangedListener() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
//                if (valueHasChanged())
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

    }


    @NonNull
    private Predicate<Pair<Boolean, Boolean>> valueHasChangedPredicate() {
        return state -> valueHasChanged();
    }

    @NonNull
    private Boolean valueHasChanged() {
        return !Preconditions.equals(isEmpty(binding.customEdittext.getEditText().getText()) ? "" : binding.customEdittext.getEditText().getText().toString(),
                model.getValue().value() == null ? "" : valueOf(model.getValue().value()));
    }

    void update(@NonNull EditTextModel editTextModel) {
        model.onNext(editTextModel);
    }
}
