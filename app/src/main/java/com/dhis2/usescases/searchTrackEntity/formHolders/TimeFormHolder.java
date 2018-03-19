package com.dhis2.usescases.searchTrackEntity.formHolders;

import android.databinding.ViewDataBinding;

import com.dhis2.BR;
import com.dhis2.usescases.searchTrackEntity.SearchTEContractsModule;

import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeModel;


/**
 * Created by frodriguez on 16/01/2018.
 */

public class TimeFormHolder extends FormViewHolder{

    SearchTEContractsModule.Presenter presenter;
    TrackedEntityAttributeModel bindableObject;

    public TimeFormHolder(ViewDataBinding binding) {
        super(binding);
    }

    public void bind(SearchTEContractsModule.Presenter presenter, TrackedEntityAttributeModel bindableObject) {
        this.presenter = presenter;
        this.bindableObject = bindableObject;
        binding.setVariable(BR.attribute, bindableObject);
        binding.executePendingBindings();
    }

}