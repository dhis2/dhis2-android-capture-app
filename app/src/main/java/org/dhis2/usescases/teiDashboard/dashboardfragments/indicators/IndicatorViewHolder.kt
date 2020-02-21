package org.dhis2.usescases.teiDashboard.dashboardfragments.indicators

import android.graphics.Color
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import org.dhis2.BR
import org.dhis2.R
import org.dhis2.data.tuples.Trio
import org.dhis2.databinding.ItemIndicatorBinding
import org.dhis2.utils.Constants
import org.dhis2.utils.customviews.CustomDialog
import org.hisp.dhis.android.core.program.ProgramIndicator

class IndicatorViewHolder(val binding: ItemIndicatorBinding) : RecyclerView.ViewHolder(binding.root) {

    fun bind(programIndicatorModel: Trio<ProgramIndicator, String, String>) {
        if (programIndicatorModel.val0() == null) {
            val params = binding.guideline.layoutParams as ConstraintLayout.LayoutParams
            params.guidePercent = 0F
            binding.guideline.layoutParams = params
        } else {
            binding.setVariable(BR.label, programIndicatorModel.val0()!!.displayName())
            binding.setVariable(BR.description, programIndicatorModel.val0()!!.displayDescription())
        }

        val color = when {
            programIndicatorModel.val2().isNullOrEmpty() -> -1
            else -> Color.parseColor(programIndicatorModel.val2())
        }

        binding.setVariable(BR.value, programIndicatorModel.val1())
        binding.setVariable(BR.colorBg, color)
        binding.executePendingBindings()

        binding.descriptionLabel.setOnClickListener {
            showDescription(programIndicatorModel.val0()!!)
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