package org.dhis2.usescases.teiDashboard.dashboardfragments.tei_data;

import org.dhis2.BR;
import org.dhis2.Bindings.Bindings;
import org.dhis2.databinding.ItemDashboardProgramBinding;
import org.dhis2.usescases.teiDashboard.DashboardProgramModel;
import org.dhis2.usescases.teiDashboard.TeiDashboardContracts;
import org.dhis2.usescases.teiDashboard.dashboardfragments.tei_data.TEIDataContracts;
import org.hisp.dhis.android.core.enrollment.EnrollmentModel;
import org.hisp.dhis.android.core.program.ProgramModel;

import androidx.recyclerview.widget.RecyclerView;

/**
 * QUADRAM. Created by ppajuelo on 29/11/2017.
 */

class DashboardProgramViewHolder extends RecyclerView.ViewHolder {
    private ItemDashboardProgramBinding binding;

    DashboardProgramViewHolder(ItemDashboardProgramBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public void bind(TEIDataContracts.Presenter presenter, DashboardProgramModel dashboardProgramModel, int position) {
        ProgramModel programModel = dashboardProgramModel.getEnrollmentProgramModels().get(position);
        EnrollmentModel enrollment = dashboardProgramModel.getEnrollmentForProgram(programModel.uid());
        binding.setVariable(BR.presenter, presenter);
        binding.setVariable(BR.program, programModel);

        Bindings.setObjectStyle(binding.programImage, binding.programImage, dashboardProgramModel.getObjectStyleForProgram(programModel.uid()));

        if (enrollment != null)
            binding.setVariable(BR.enrollment, enrollment);
        binding.executePendingBindings();

        itemView.setOnClickListener(v -> presenter.setProgram(dashboardProgramModel.getEnrollmentProgramModels().get(position)));
    }
}
