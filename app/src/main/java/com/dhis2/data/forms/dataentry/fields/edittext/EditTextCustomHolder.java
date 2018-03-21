package com.dhis2.data.forms.dataentry.fields.edittext;

import android.graphics.Color;
import android.support.annotation.NonNull;

import com.dhis2.BR;
import com.dhis2.data.forms.dataentry.fields.RowAction;
import com.dhis2.data.tuples.Pair;
import com.dhis2.databinding.FormAgeCustomBinding;
import com.dhis2.databinding.FormEditTextCustomBinding;
import com.dhis2.usescases.searchTrackEntity.SearchTEContractsModule;
import com.dhis2.usescases.searchTrackEntity.formHolders.FormViewHolder;
import com.dhis2.utils.OnErrorHandler;
import com.dhis2.utils.Preconditions;
import com.jakewharton.rxbinding2.view.RxView;

import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeModel;

import io.reactivex.functions.Predicate;
import io.reactivex.observables.ConnectableObservable;
import io.reactivex.processors.BehaviorProcessor;
import io.reactivex.processors.FlowableProcessor;

import static android.text.TextUtils.isEmpty;
import static java.lang.String.valueOf;


/**
 * Created by frodriguez on 18/01/2018.
 */

public class EditTextCustomHolder extends FormViewHolder {

    private final FlowableProcessor<RowAction> processor;
    SearchTEContractsModule.Presenter presenter;
    TrackedEntityAttributeModel bindableObject;
    private final FormEditTextCustomBinding binding;
    @NonNull
    private BehaviorProcessor<EditTextModel> model;

    public EditTextCustomHolder(FormEditTextCustomBinding binding, FlowableProcessor<RowAction> processor) {
        super(binding);
        this.processor = processor;
        this.binding = binding;
        model = BehaviorProcessor.create();

        model.subscribe(editTextModel -> {
            binding.setLabel(editTextModel.label());
            binding.setValueType(editTextModel.valueType());
            binding.customEdittext.getEditText().setTextColor(Color.BLACK);
            binding.customEdittext.getEditText().setHintTextColor(Color.BLUE);
            binding.customEdittext.getEditText().setText(editTextModel.value() == null ?
                    null : valueOf(editTextModel.value()));
            if (!isEmpty(editTextModel.warning())) {
                binding.customEdittext.getEditText().setError(editTextModel.warning());
            } else if (!isEmpty(editTextModel.error())) {
                binding.customEdittext.getEditText().setError(editTextModel.error());
            } else
                binding.customEdittext.getEditText().setError(null);

            binding.customEdittext.getEditText().setHint(isEmpty(binding.customEdittext.getEditText().getText()) ? editTextModel.label() : "");

            binding.executePendingBindings();
        });

       /* if (binding instanceof FormAgeCustomBinding) {
            modelFormAge((FormAgeCustomBinding) binding);

        } else {*/
        modelFormEditText(binding);


    }

    public void bind(SearchTEContractsModule.Presenter presenter, TrackedEntityAttributeModel bindableObject) {
        this.presenter = presenter;
        this.bindableObject = bindableObject;
        binding.setVariable(BR.attribute, bindableObject);
        binding.executePendingBindings();
    }

    private void modelFormEditText(FormEditTextCustomBinding binding) {

        ConnectableObservable<Boolean> editTextObservable = RxView.focusChanges(binding.customEdittext.getEditText()).takeUntil(RxView.detaches(binding.getRoot())).publish();

        // persist value on focus change
        editTextObservable
                .scan(Pair.create(false, false), (state, hasFocus) ->
                        Pair.create(state.val1() && !hasFocus, hasFocus))
                .filter(state -> state.val0() && model.hasValue())
                .filter(valueHasChangedPredicate())
                .map(event -> RowAction.create(model.getValue().uid(),
                        binding.customEdittext.getEditText().getText().toString()))
                .subscribe(processor::onNext, OnErrorHandler.create(), () -> {
                    // this is necessary for persisting last value in the form
                    if (valueHasChanged()) {
                        processor.onNext(RowAction.create(model.getValue().uid(),
                                binding.customEdittext.getEditText().getText().toString()));
                    }
                });

        editTextObservable.connect();
    }

    private void modelFormAge(FormAgeCustomBinding binding) {

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
