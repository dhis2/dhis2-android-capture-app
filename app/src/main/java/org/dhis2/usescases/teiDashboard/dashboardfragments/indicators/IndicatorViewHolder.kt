package org.dhis2.usescases.teiDashboard.dashboardfragments.indicators

import android.graphics.Color
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import org.dhis2.BR
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
        if (programIndicatorModel.programIndicator == null) {
            val params = binding.guideline.layoutParams as ConstraintLayout.LayoutParams
            params.guidePercent = 0F
            binding.guideline.layoutParams = params
        } else {
            val params = binding.guideline.layoutParams as ConstraintLayout.LayoutParams
            params.guidePercent = 0.6F
            binding.guideline.layoutParams = params
            binding.label = programIndicatorModel.programIndicator.displayName()
            binding.description = programIndicatorModel.programIndicator.displayDescription()
        }

        val color = when {
            programIndicatorModel.color.isNullOrEmpty() -> -1
            else -> Color.parseColor(programIndicatorModel.color)
        }

        binding.setVariable(BR.value, programIndicatorModel.value)
        binding.setVariable(BR.colorBg, color)
        binding.executePendingBindings()

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
