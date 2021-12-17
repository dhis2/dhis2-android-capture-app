package org.dhis2.usescases.main.program

import androidx.recyclerview.widget.RecyclerView
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
                binding.programImage.context,
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

        binding.programImage.setColorFilter(ColorUtils.getContrastColor(color))

        itemView.setOnClickListener { v ->
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
