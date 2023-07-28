package org.dhis2.usescases.teiDashboard.dashboardfragments.teidata

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import org.dhis2.R
import org.dhis2.databinding.ItemDashboardProgramBinding
import org.dhis2.usescases.teiDashboard.DashboardProgramModel

class DashboardProgramAdapter(
    private val presenter: TEIDataPresenter,
    private val dashboardProgramModel: DashboardProgramModel
) : RecyclerView.Adapter<DashboardProgramViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DashboardProgramViewHolder {
        val binding = DataBindingUtil.inflate<ItemDashboardProgramBinding>(
            LayoutInflater.from(parent.context),
            R.layout.item_dashboard_program,
            parent,
            false
        )
        return DashboardProgramViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DashboardProgramViewHolder, position: Int) {
        holder.bind(presenter, dashboardProgramModel, position)
    }

    override fun getItemCount(): Int {
        return dashboardProgramModel.programsWithActiveEnrollment.size
    }
}
