package org.dhis2.usescases.main.program

import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import org.dhis2.R
import org.dhis2.databinding.ItemProgramModelBinding
import org.dhis2.utils.ColorUtils
import timber.log.Timber

/**
 * QUADRAM. Created by ppajuelo on 13/06/2018.
 */

class ProgramModelHolder(private val binding: ItemProgramModelBinding) : RecyclerView.ViewHolder(binding.root) {

    fun bind(presenter: ProgramContract.Presenter, programViewModel: ProgramViewModel) {
        binding.program = programViewModel
        binding.presenter = presenter

        val color = ColorUtils.getColorFrom(programViewModel.color(), ColorUtils.getPrimaryColor(binding.programImage.context, ColorUtils.ColorType.PRIMARY))
        val icon = if (programViewModel.icon() != null) {
            val resources = binding.programImage.resources
            val iconName = if (programViewModel.icon()!!.startsWith("ic_")) programViewModel.icon() else "ic_" + programViewModel.icon()!!
            resources.getIdentifier(iconName, "drawable", binding.programImage.context.packageName)
        } else {
            R.drawable.ic_program_default
        }
        var iconImage = AppCompatResources.getDrawable(binding.programImage.context, R.drawable.ic_program_default)
        try {
            iconImage = AppCompatResources.getDrawable(binding.programImage.context, icon)
        } catch (e: Exception) {
            Timber.log(1, e)
        }

        iconImage?.mutate()

        binding.programImage.setImageDrawable(iconImage)
        binding.programImage.setColorFilter(ColorUtils.getContrastColor(color))
        binding.programImage.setBackgroundColor(color)

    }
}