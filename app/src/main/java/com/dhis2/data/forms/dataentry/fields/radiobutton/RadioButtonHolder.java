package com.dhis2.data.forms.dataentry.fields.radiobutton;

import android.databinding.ViewDataBinding;

import com.dhis2.BR;
import com.dhis2.usescases.searchTrackEntity.SearchTEContractsModule;
import com.dhis2.usescases.searchTrackEntity.formHolders.FormViewHolder;

import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeModel;


/**
 * Created by frodriguez on 18/01/2018.
 */

public class RadioButtonHolder extends FormViewHolder {

    SearchTEContractsModule.Presenter presenter;
    TrackedEntityAttributeModel bindableObject;

    public RadioButtonHolder(ViewDataBinding binding) {
        super(binding);
    }

    public void bind(SearchTEContractsModule.Presenter presenter, TrackedEntityAttributeModel bindableObject) {
        this.presenter = presenter;
        this.bindableObject = bindableObject;
        binding.setVariable(BR.attribute, bindableObject);
        binding.executePendingBindings();


    }
}
