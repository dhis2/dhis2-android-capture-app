package org.dhis2.usescases.teiDashboard.adapters;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;

import org.dhis2.BR;
import org.dhis2.data.tuples.Trio;
import org.dhis2.databinding.ItemIndicatorBinding;

import org.hisp.dhis.android.core.program.ProgramIndicatorModel;

/**
 * Created by ppajuelo on 29/11/2017.
 */

class IndicatorViewHolder extends RecyclerView.ViewHolder {
    ItemIndicatorBinding binding;

    IndicatorViewHolder(ItemIndicatorBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public void bind(Trio<ProgramIndicatorModel, String, String> programIndicatorModel) {
        binding.setVariable(BR.indicator, programIndicatorModel.val0());
        binding.setVariable(BR.value, programIndicatorModel.val1());
        binding.setVariable(BR.colorBg, programIndicatorModel.val2().isEmpty() ? -1 : Color.parseColor(programIndicatorModel.val2()));
        binding.executePendingBindings();
    }
}
