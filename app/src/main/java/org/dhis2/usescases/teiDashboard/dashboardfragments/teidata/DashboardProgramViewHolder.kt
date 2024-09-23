package org.dhis2.usescases.teiDashboard.dashboardfragments.teidata

import androidx.recyclerview.widget.RecyclerView
import org.dhis2.BR
import org.dhis2.databinding.ItemDashboardProgramBinding
import org.dhis2.usescases.teiDashboard.DashboardTEIModel
import org.hisp.dhis.android.core.program.Program

class DashboardProgramViewHolder(
    private val binding: ItemDashboardProgramBinding,
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(
        presenter: TEIDataPresenter,
        dashboardProgramModel: DashboardTEIModel,
        position: Int,
    ) {
        dashboardProgramModel.getProgramsWithActiveEnrollment()?.get(position)?.let { program: Program ->
            val enrollment = dashboardProgramModel.getEnrollmentForProgram(program.uid())
            binding.setVariable(BR.presenter, presenter)
            binding.setVariable(BR.program, program)
            binding.metadataIconData = dashboardProgramModel.getIconForProgram(program.uid())
            if (enrollment != null) {
                binding.setVariable(BR.enrollment, enrollment)
                binding.setVariable(
                    BR.orgUnit,
                    presenter.getOrgUnitName(enrollment.organisationUnit() ?: ""),
                )
            }
            binding.executePendingBindings()
            itemView.setOnClickListener {
                presenter.setProgram(
                    program,
                    enrollment!!.uid(),
                )
            }
        }
    }
}
