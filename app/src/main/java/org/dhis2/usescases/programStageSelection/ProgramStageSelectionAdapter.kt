package org.dhis2.usescases.programStageSelection

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import org.dhis2.R
import org.dhis2.commons.resources.ColorUtils
import org.dhis2.databinding.ItemProgramStageBinding
import org.hisp.dhis.android.core.program.ProgramStage

class ProgramStageSelectionAdapter(
    val colorUtils: ColorUtils,
    val onItemClick: (ProgramStage) -> Unit,
) : ListAdapter<ProgramStage, ProgramStageSelectionViewHolder>(object :
    DiffUtil.ItemCallback<ProgramStage>() {
    override fun areItemsTheSame(oldItem: ProgramStage, newItem: ProgramStage): Boolean {
        return oldItem.uid() == newItem.uid()
    }

    override fun areContentsTheSame(oldItem: ProgramStage, newItem: ProgramStage): Boolean {
        return oldItem.equals(newItem)
    }
}) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ProgramStageSelectionViewHolder {
        val binding: ItemProgramStageBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_program_stage,
            parent,
            false,
        )
        return ProgramStageSelectionViewHolder(binding, colorUtils, onItemClick)
    }

    override fun onBindViewHolder(holder: ProgramStageSelectionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
