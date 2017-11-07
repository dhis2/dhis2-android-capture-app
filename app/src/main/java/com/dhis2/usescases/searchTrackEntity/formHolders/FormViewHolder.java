package com.dhis2.usescases.searchTrackEntity.formHolders;

import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;

import com.dhis2.usescases.searchTrackEntity.SearchTEContractsModule;

import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeModel;

/**
 * Created by ppajuelo on 06/11/2017.
 */

public abstract class FormViewHolder extends RecyclerView.ViewHolder {
    ViewDataBinding binding;

    public FormViewHolder(ViewDataBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public abstract void bind(SearchTEContractsModule.Presenter presenter, TrackedEntityAttributeModel bindableOnject);
}
