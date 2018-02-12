package com.dhis2.data.forms.dataentry.fields.edittext;

import android.databinding.ViewDataBinding;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.widget.EditText;

import com.dhis2.BR;
import com.dhis2.data.forms.dataentry.fields.RowAction;
import com.dhis2.data.tuples.Pair;
import com.dhis2.databinding.FormAgeCustomBinding;
import com.dhis2.databinding.FormEditTextCustomBinding;
import com.dhis2.usescases.searchTrackEntity.SearchTEContractsModule;
import com.dhis2.usescases.searchTrackEntity.formHolders.FormViewHolder;
import com.dhis2.utils.OnErrorHandler;
import com.dhis2.utils.Preconditions;
import com.dhis2.utils.TextChangedListener;
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

    EditText editText;

    @NonNull
    BehaviorProcessor<EditTextModel> model;

    public EditTextCustomHolder(ViewDataBinding binding, FlowableProcessor<RowAction> processor) {
        super(binding);
        this.processor = processor;
    }

    public void bind(SearchTEContractsModule.Presenter presenter, TrackedEntityAttributeModel bindableObject) {
        this.presenter = presenter;
        this.bindableObject = bindableObject;
        binding.setVariable(BR.attribute, bindableObject);
        binding.executePendingBindings();

        model = BehaviorProcessor.create();

        if (binding instanceof FormAgeCustomBinding) {
            modelFormAge((FormAgeCustomBinding) binding);

        } else {
            modelFormEditText((FormEditTextCustomBinding) binding);
        }
    }

    private void modelFormEditText(FormEditTextCustomBinding binding) {

        editText = binding.customEdittext.getEditText();
        binding.customEdittext.setTextChangedListener(new TextChangedListener() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                presenter.query(String.format("%s:LIKE:%s", bindableObject.uid(), charSequence), true); //Searchs for attributes which contains charSequece in its value
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        model.subscribe(editTextModel -> {
           editText.setText(editTextModel.value() == null ?
                    null : valueOf(editTextModel.value()));
        });

        ConnectableObservable<Boolean> editTextObservable = RxView.focusChanges(binding.customEdittext).takeUntil(RxView.detaches(binding.getRoot())).publish();

        // persist value on focus change
        editTextObservable
                .scan(Pair.create(false, false), (state, hasFocus) ->
                        Pair.create(state.val1() && !hasFocus, hasFocus))
                .filter(state -> state.val0() && model.hasValue())
                .filter(valueHasChangedPredicate())
                .map(event -> RowAction.create(model.getValue().uid(),
                        editText.getText().toString()))
                .subscribe(action -> processor.onNext(action), OnErrorHandler.create(), () -> {
                    // this is necessary for persisting last value in the form
                    if (valueHasChanged()) {
                        processor.onNext(RowAction.create(model.getValue().uid(),
                                binding.customEdittext.getEditText().getText().toString()));
                    }
                });

        editTextObservable.connect();
    }

    private void modelFormAge(FormAgeCustomBinding binding) {
        binding.customAgeview.setTextChangedListener(new TextChangedListener() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                presenter.query(String.format("%s:LIKE:%s", bindableObject.uid(), charSequence), true);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
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
