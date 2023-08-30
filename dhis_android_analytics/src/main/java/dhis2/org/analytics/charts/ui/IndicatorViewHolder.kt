package dhis2.org.analytics.charts.ui

import androidx.recyclerview.widget.RecyclerView
import dhis2.org.R
import dhis2.org.databinding.ItemIndicatorBinding
import org.dhis2.commons.dialogs.CustomDialog
import org.hisp.dhis.android.core.program.ProgramIndicator

class IndicatorViewHolder(
    val binding: ItemIndicatorBinding,
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
            itemView.context.getString(R.string.action_accept),
            null,
            CustomDialog.DESCRIPTION_DIALOG,
            null,
        ).show()
    }
}
