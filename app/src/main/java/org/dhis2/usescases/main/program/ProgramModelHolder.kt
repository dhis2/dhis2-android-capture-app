package org.dhis2.usescases.main.program

import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.composethemeadapter.MdcTheme
import org.dhis2.R
import org.dhis2.commons.resources.ColorUtils
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.databinding.ItemProgramModelBinding

class ProgramModelHolder(private val binding: ItemProgramModelBinding) :
    RecyclerView.ViewHolder(binding.root) {

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

        binding.programImage.background = ColorUtils.tintDrawableWithColor(
            binding.programImage.background,
            color
        )

        binding.programImage.setImageResource(
            ResourceManager(itemView.context).getObjectStyleDrawableResource(
                programViewModel.icon(),
                R.drawable.ic_default_outline
            )
        )

        itemView.setOnClickListener {
            val programTheme = ColorUtils.getThemeFromColor(programViewModel.color())
            presenter.onItemClick(programViewModel, programTheme)
        }

        binding.root.alpha = if (programViewModel.translucent()) {
            0.5f
        } else {
            1.0f
        }
    }
}
