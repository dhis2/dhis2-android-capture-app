package com.dhis2.usescases.teiDashboard.adapters;

import android.support.v7.widget.RecyclerView;

import com.dhis2.BR;
import com.dhis2.databinding.ItemDashboardProgramBinding;
import com.dhis2.usescases.teiDashboard.DashboardProgramModel;
import com.dhis2.usescases.teiDashboard.TeiDashboardContracts;

/**
 * Created by ppajuelo on 29/11/2017.
 */

class DashboardProgramViewHolder extends RecyclerView.ViewHolder {
    ItemDashboardProgramBinding binding;

    public DashboardProgramViewHolder(ItemDashboardProgramBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public void bind(TeiDashboardContracts.Presenter presenter, DashboardProgramModel dashboardProgramModel, int position) {
        binding.setVariable(BR.presenter, presenter);
        binding.setVariable(BR.program, dashboardProgramModel.getEnrollmentProgramModels().get(position));
//        binding.setVariable(BR.enrollment, dashboardProgramModel.getEnrollment);
        binding.executePendingBindings();

        itemView.setOnClickListener(v -> presenter.setProgram(dashboardProgramModel.getEnrollmentProgramModels().get(position)));
    }
}
