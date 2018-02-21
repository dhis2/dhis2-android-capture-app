package com.dhis2.usescases.searchTrackEntity.formHolders;

import android.databinding.ViewDataBinding;
import android.text.Editable;

import com.dhis2.BR;
import com.dhis2.databinding.FormAgeCustomBinding;
import com.dhis2.databinding.FormEditTextCustomBinding;
import com.dhis2.usescases.searchTrackEntity.SearchTEContractsModule;
import com.dhis2.utils.TextChangedListener;

import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeModel;

import io.reactivex.processors.FlowableProcessor;


/**
 * Created by frodriguez on 18/01/2018.
 */

public class EditTextFormHolder extends FormViewHolder {

    SearchTEContractsModule.Presenter presenter;
    TrackedEntityAttributeModel bindableObject;
    private final FlowableProcessor<FormViewHolder> processor;

    public EditTextFormHolder(ViewDataBinding binding, FlowableProcessor<FormViewHolder> processor) {
        super(binding);
        this.processor = processor;
    }

    public void bind(SearchTEContractsModule.Presenter presenter, TrackedEntityAttributeModel bindableObject) {
        this.presenter = presenter;
        this.bindableObject = bindableObject;
        binding.setVariable(BR.attribute, bindableObject);
        binding.executePendingBindings();

        if (binding instanceof FormAgeCustomBinding) {
            ((FormAgeCustomBinding) binding).customAgeview.setTextChangedListener(new TextChangedListener() {
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
        } else {
            ((FormEditTextCustomBinding) binding).customEdittext.setTextChangedListener(new TextChangedListener() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                    value = charSequence.toString();
                    id = bindableObject.id();
                    presenter.query(String.format("%s:LIKE:%s", bindableObject.uid(), charSequence), true); //Searchs for attributes which contains charSequece in its value
                    processor.onNext(EditTextFormHolder.this);
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });
        }


    }
}
