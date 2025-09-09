package org.dhis2.usescases.programStageSelection

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.recyclerview.widget.RecyclerView
import org.dhis2.databinding.ItemProgramStageBinding
import org.hisp.dhis.android.core.program.ProgramStage
import org.hisp.dhis.mobile.ui.designsystem.component.MetadataAvatar
import org.hisp.dhis.mobile.ui.designsystem.component.MetadataAvatarSize
import org.hisp.dhis.mobile.ui.designsystem.component.MetadataIcon
import org.hisp.dhis.mobile.ui.designsystem.resource.provideDHIS2Icon
import org.hisp.dhis.mobile.ui.designsystem.theme.DHIS2Theme

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

        binding.composeProgramStageIcon.apply {
            setContent {
                DHIS2Theme {
                    MetadataAvatar(
                        modifier =
                            Modifier
                                .size(56.dp)
                                .alpha(0.5f),
                        icon = {
                            if (programStageData.metadataIconData.isFileLoaded()) {
                                MetadataIcon(
                                    imageCardData = programStageData.metadataIconData.imageCardData,
                                )
                            } else {
                                Icon(
                                    painter = provideDHIS2Icon("dhis2_image_not_supported"),
                                    contentDescription = "",
                                )
                            }
                        },
                        iconTint = programStageData.metadataIconData.color,
                        size = MetadataAvatarSize.M(),
                    )
                }
            }
        }

        itemView.setOnClickListener {
            onItemClick(programStageData.programStage)
        }
    }
}
