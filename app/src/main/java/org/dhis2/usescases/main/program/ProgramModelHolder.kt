package org.dhis2.usescases.main.program

import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import org.dhis2.R
import org.dhis2.databinding.ItemProgramModelBinding
import org.dhis2.utils.ColorUtils
import org.dhis2.utils.ObjectStyleUtils
import org.dhis2.utils.resources.ResourceManager
import timber.log.Timber

/**
 * QUADRAM. Created by ppajuelo on 13/06/2018.
 */

class ProgramModelHolder(private val binding: ItemProgramModelBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(presenter: ProgramPresenter, programViewModel: ProgramViewModel) {
        binding.program = programViewModel
        binding.presenter = presenter

        val color = ColorUtils.getColorFrom(
            programViewModel.color(),
            ColorUtils.getPrimaryColor(binding.programImage.context, ColorUtils.ColorType.PRIMARY)
        )

        binding.programImage.background = ColorUtils.tintDrawableWithColor(
            binding.programImage.background,
            color
        )

        binding.programImage.setImageResource(
            ResourceManager(itemView.context).getObjectStyleDrawableResource(
                programViewModel.icon(),
                R.drawable.ic_program_default
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
