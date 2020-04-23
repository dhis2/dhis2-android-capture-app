package org.dhis2.usescases.teiDashboard.dashboardfragments.tei_data;

import androidx.recyclerview.widget.RecyclerView;

import org.dhis2.BR;
import org.dhis2.Bindings.Bindings;
import org.dhis2.databinding.ItemDashboardProgramBinding;
import org.dhis2.usescases.teiDashboard.DashboardProgramModel;
import org.hisp.dhis.android.core.enrollment.Enrollment;
import org.hisp.dhis.android.core.program.Program;

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
        Program program = dashboardProgramModel.getEnrollmentPrograms().get(position);
        Enrollment enrollment = dashboardProgramModel.getEnrollmentForProgram(program.uid());
        binding.setVariable(BR.presenter, presenter);
        binding.setVariable(BR.program, program);

        Bindings.setObjectStyle(binding.programImage, binding.programImage, dashboardProgramModel.getObjectStyleForProgram(program.uid()));

        if (enrollment != null)
            binding.setVariable(BR.enrollment, enrollment);
        binding.executePendingBindings();

        itemView.setOnClickListener(v -> presenter.setProgram(dashboardProgramModel.getEnrollmentPrograms().get(position), enrollment.uid()));
    }
}
