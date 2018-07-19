package com.dhis2.usescases.teiDashboard.adapters;

import android.support.v7.widget.RecyclerView;

import com.dhis2.BR;
import com.dhis2.Bindings.Bindings;
import com.dhis2.databinding.ItemDashboardProgramBinding;
import com.dhis2.usescases.teiDashboard.DashboardProgramModel;
import com.dhis2.usescases.teiDashboard.TeiDashboardContracts;

import org.hisp.dhis.android.core.enrollment.EnrollmentModel;
import org.hisp.dhis.android.core.program.ProgramModel;

/**
 * QUADRAM. Created by ppajuelo on 29/11/2017.
 */

class DashboardProgramViewHolder extends RecyclerView.ViewHolder {
    ItemDashboardProgramBinding binding;

    public DashboardProgramViewHolder(ItemDashboardProgramBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public void bind(TeiDashboardContracts.Presenter presenter, DashboardProgramModel dashboardProgramModel, int position) {
        ProgramModel programModel = dashboardProgramModel.getEnrollmentProgramModels().get(position);
        EnrollmentModel enrollment = dashboardProgramModel.getEnrollmentForProgram(programModel.uid());
        binding.setVariable(BR.presenter, presenter);
        binding.setVariable(BR.program, programModel);
        Bindings.setObjectStyle(binding.programImage, binding.programImage, programModel.uid());
        if (enrollment != null)
            binding.setVariable(BR.enrollment, enrollment);
        binding.executePendingBindings();

        itemView.setOnClickListener(v -> presenter.setProgram(dashboardProgramModel.getEnrollmentProgramModels().get(position)));
    }
}
