package com.dhis2.usescases.searchTrackEntity.formHolders;

import android.databinding.ViewDataBinding;
import android.text.Editable;
import android.text.TextWatcher;

import com.dhis2.BR;
import com.dhis2.databinding.FormEditTextCustomBinding;
import com.dhis2.usescases.searchTrackEntity.SearchTEContractsModule;

import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeModel;


/**
 * Created by frodriguez on 18/01/2018.
 */

public class EditTextFormHolder extends FormViewHolder{

    SearchTEContractsModule.Presenter presenter;
    TrackedEntityAttributeModel bindableObject;

    public EditTextFormHolder(ViewDataBinding binding) {
        super(binding);
    }

    public void bind(SearchTEContractsModule.Presenter presenter, TrackedEntityAttributeModel bindableObject) {
        this.presenter = presenter;
        this.bindableObject = bindableObject;
        binding.setVariable(BR.attribute, bindableObject);
        binding.executePendingBindings();

        /*((FormEditTextCustomBinding) binding).customEdittext..addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                presenter.query(String.format("%s:LIKE:%s", bindableObject.uid(), charSequence), true); //Searchs for attributes which contains charSequece in its value
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });*/
    }
}
