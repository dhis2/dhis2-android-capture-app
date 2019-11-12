package org.dhis2.usescases.programStageSelection

import android.content.res.ColorStateList
import android.graphics.Color

import org.dhis2.BR
import org.dhis2.Bindings.Bindings
import org.dhis2.databinding.ItemProgramStageBinding
import org.hisp.dhis.android.core.common.ObjectStyle
import org.hisp.dhis.android.core.program.ProgramStage

import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView

class ProgramStageSelectionViewHolder(
    private val binding: ItemProgramStageBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(presenter: ProgramStageSelectionPresenter, programStage: ProgramStage) {
        binding.setVariable(BR.presenter, presenter)
        binding.setVariable(BR.programStage, programStage)
        binding.executePendingBindings()

        val style: ObjectStyle = programStage.style() ?: ObjectStyle.builder().build()

        style.icon()?.let {
            val resources = binding.programStageIcon.context.resources
            val iconName = if (it.startsWith("ic_")) it else "ic_$it"
            val icon = resources.getIdentifier(
                iconName,
                "drawable",
                binding.programStageIcon.context.packageName
            )
            binding.programStageIcon.setImageResource(icon)
        }

        style.color()?.let {
            val color = if (it.startsWith("#")) it else "#$it"
            val colorRes = Color.parseColor(color)
            val colorStateList = ColorStateList.valueOf(colorRes)
            ViewCompat.setBackgroundTintList(binding.programStageIcon, colorStateList)
            Bindings.setFromResBgColor(binding.programStageIcon, colorRes)
        }

        itemView.setOnClickListener {
            if (programStage.access().data().write())
                presenter.onProgramStageClick(programStage)
        }
    }
}
