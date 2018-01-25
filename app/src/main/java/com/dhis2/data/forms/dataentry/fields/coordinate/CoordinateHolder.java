package com.dhis2.data.forms.dataentry.fields.coordinate;


import android.databinding.ViewDataBinding;

import com.dhis2.BR;
import com.dhis2.data.forms.dataentry.fields.FormViewHolder;
import com.dhis2.usescases.searchTrackEntity.SearchTEContractsModule;

import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeModel;

public class CoordinateHolder extends FormViewHolder {

    SearchTEContractsModule.Presenter presenter;
    TrackedEntityAttributeModel bindableObject;

    public CoordinateHolder(ViewDataBinding binding) {
        super(binding);
    }

    @Override
    public void bind(SearchTEContractsModule.Presenter presenter, TrackedEntityAttributeModel bindableObject) {
        this.presenter = presenter;
        this.bindableObject = bindableObject;
        binding.setVariable(BR.attribute, bindableObject);
        binding.executePendingBindings();
    }
}