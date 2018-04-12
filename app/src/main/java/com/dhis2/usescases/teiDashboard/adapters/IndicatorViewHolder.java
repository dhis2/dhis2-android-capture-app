package com.dhis2.usescases.teiDashboard.adapters;

import android.support.v7.widget.RecyclerView;

import com.dhis2.BR;
import com.dhis2.databinding.ItemEventBinding;
import com.dhis2.databinding.ItemIndicatorBinding;

import org.hisp.dhis.android.core.program.ProgramIndicatorModel;

/**
 * Created by ppajuelo on 29/11/2017.
 *
 */

class IndicatorViewHolder extends RecyclerView.ViewHolder {
    ItemIndicatorBinding binding;

    IndicatorViewHolder(ItemIndicatorBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public void bind(ProgramIndicatorModel programIndicatorModel) {
        binding.setVariable(BR.indicator, programIndicatorModel);
        binding.executePendingBindings();
    }
}
