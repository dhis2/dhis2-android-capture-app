package org.dhis2.usescases.programStageSelection

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import org.dhis2.R
import org.dhis2.databinding.ItemProgramStageBinding
import org.hisp.dhis.android.core.program.ProgramStage

class ProgramStageSelectionAdapter(
    val onItemClick: (ProgramStage) -> Unit,
) : ListAdapter<ProgramStageData, ProgramStageSelectionViewHolder>(object :
    DiffUtil.ItemCallback<ProgramStageData>() {
    override fun areItemsTheSame(oldItem: ProgramStageData, newItem: ProgramStageData): Boolean {
        return oldItem.programStage.uid() == newItem.programStage.uid()
    }

    override fun areContentsTheSame(oldItem: ProgramStageData, newItem: ProgramStageData): Boolean {
        return oldItem == newItem
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
        return ProgramStageSelectionViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: ProgramStageSelectionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
