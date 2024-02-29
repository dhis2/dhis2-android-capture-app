package org.dhis2.usescases.programStageSelection

import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.recyclerview.widget.RecyclerView
import org.dhis2.databinding.ItemProgramStageBinding
import org.dhis2.ui.setUpMetadataIcon
import org.hisp.dhis.android.core.program.ProgramStage

class ProgramStageSelectionViewHolder(
    private val binding: ItemProgramStageBinding,
    val onItemClick: (ProgramStage) -> Unit,
) : RecyclerView.ViewHolder(binding.root) {

    init {
        binding.composeProgramStageIcon.setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed,
        )
    }

    fun bind(programStageData: ProgramStageData) {
        binding.programStage = programStageData.programStage
        binding.executePendingBindings()

        binding.composeProgramStageIcon.setUpMetadataIcon(
            programStageData.metadataIconData,
            false,
        )

        itemView.setOnClickListener {
            onItemClick(programStageData.programStage)
        }
    }
}
