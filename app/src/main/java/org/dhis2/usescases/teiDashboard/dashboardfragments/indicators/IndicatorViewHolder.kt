package org.dhis2.usescases.teiDashboard.dashboardfragments.indicators

import androidx.recyclerview.widget.RecyclerView
import org.dhis2.R
import org.dhis2.data.analytics.IndicatorModel
import org.dhis2.databinding.ItemIndicatorBinding
import org.dhis2.utils.Constants
import org.dhis2.utils.customviews.CustomDialog
import org.hisp.dhis.android.core.program.ProgramIndicator

class IndicatorViewHolder(
    val binding: ItemIndicatorBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(programIndicatorModel: IndicatorModel) {
        binding.indicatorModel = programIndicatorModel
        binding.descriptionLabel.setOnClickListener {
            showDescription(programIndicatorModel.programIndicator!!)
        }
    }

    private fun showDescription(programIndicatorModel: ProgramIndicator) {
        CustomDialog(
            itemView.context,
            programIndicatorModel.displayName()!!,
            programIndicatorModel.displayDescription()!!,
            itemView.getContext().getString(R.string.action_accept),
            null,
            Constants.DESCRIPTION_DIALOG,
            null
        ).show()
    }
}
