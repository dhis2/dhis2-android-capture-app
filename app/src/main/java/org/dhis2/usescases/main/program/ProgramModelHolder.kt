package org.dhis2.usescases.main.program

import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.composethemeadapter.MdcTheme
import org.dhis2.R
import org.dhis2.commons.resources.ColorUtils
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.commons.ui.MetadataIcon
import org.dhis2.commons.ui.MetadataIconData
import org.dhis2.databinding.ItemProgramModelBinding

class ProgramModelHolder(private val binding: ItemProgramModelBinding) :
    RecyclerView.ViewHolder(binding.root) {

    init {
        binding.composeProgramImage.setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
        )
    }

    fun bind(presenter: ProgramPresenter, programViewModel: ProgramViewModel) {
        binding.program = programViewModel
        binding.presenter = presenter

        val color = ColorUtils.getColorFrom(
            programViewModel.color(),
            ColorUtils.getPrimaryColor(
                itemView.context,
                ColorUtils.ColorType.PRIMARY_LIGHT
            )
        )

        val iconResource = ResourceManager(itemView.context).getObjectStyleDrawableResource(
            programViewModel.icon(),
            R.drawable.ic_default_outline
        )

        binding.composeProgramImage.setContent {
            MdcTheme {
                MetadataIcon(
                    MetadataIconData(
                        programColor = color,
                        iconResource = iconResource
                    )
                )
            }
        }

        itemView.setOnClickListener {
            presenter.onItemClick(programViewModel)
        }

        binding.root.alpha = if (programViewModel.translucent()) {
            0.5f
        } else {
            1.0f
        }
    }
}
