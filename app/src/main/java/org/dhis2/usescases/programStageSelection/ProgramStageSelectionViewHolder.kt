package org.dhis2.usescases.programStageSelection

import android.content.res.ColorStateList
import android.graphics.Color
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import org.dhis2.Bindings.Bindings
import org.dhis2.R
import org.dhis2.databinding.ItemProgramStageBinding
import org.dhis2.utils.resources.ResourceManager
import org.hisp.dhis.android.core.common.ObjectStyle
import org.hisp.dhis.android.core.program.ProgramStage
import timber.log.Timber

class ProgramStageSelectionViewHolder(
    private val binding: ItemProgramStageBinding,
    val onItemClick: (ProgramStage) -> Unit
) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(programStage: ProgramStage) {
        binding.programStage = programStage
        binding.executePendingBindings()
        val style: ObjectStyle = if (programStage.style() != null) {
            programStage.style()
        } else {
            ObjectStyle.builder().build()
        }
        if (style.icon() != null) {
            try {
                val icon = ResourceManager(binding.programStageIcon.context)
                    .getObjectStyleDrawableResource(style.icon(), R.drawable.ic_default_icon)
                binding.programStageIcon.setImageResource(icon)
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
        if (style.color() != null) {
            val color = if (style.color()!!.startsWith("#")) style.color() else "#" + style.color()
            val colorRes = Color.parseColor(color)
            val colorStateList = ColorStateList.valueOf(colorRes)
            ViewCompat.setBackgroundTintList(binding.programStageIcon, colorStateList)
            Bindings.setFromResBgColor(binding.programStageIcon, colorRes)
        }
        itemView.setOnClickListener {
            onItemClick(programStage)
        }
    }
}
