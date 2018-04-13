package com.dhis2.data.forms.dataentry.fields.datetime;

import android.databinding.ViewDataBinding;

import com.dhis2.BR;
import com.dhis2.data.forms.dataentry.fields.FormViewHolder;
import com.dhis2.usescases.searchTrackEntity.SearchTEContractsModule;

import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeModel;


/**
 * Created by frodriguez on 16/01/2018.
 */

public class DateTimeHolder extends FormViewHolder {

    public DateTimeHolder(ViewDataBinding binding) {
        super(binding);
    }


    public void update(DateTimeViewModel viewModel) {
        binding.setVariable(BR.label, viewModel.label());
        binding.executePendingBindings();
    }

}