package org.dhis2.usescases.programStageSelection

import android.view.LayoutInflater
import android.view.ViewGroup

import org.dhis2.R
import org.dhis2.databinding.ItemProgramStageBinding
import org.hisp.dhis.android.core.program.ProgramStage
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView

/**
 * Created by Cristian on 13/02/2018.
 */

class ProgramStageSelectionAdapter(
    private val presenter: ProgramStageSelectionPresenter
) : RecyclerView.Adapter<ProgramStageSelectionViewHolder>() {

    private var programStages: List<ProgramStage> = arrayListOf()

    fun setProgramStages(programStages: List<ProgramStage>) {
        this.programStages = programStages
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ProgramStageSelectionViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = DataBindingUtil.inflate<ItemProgramStageBinding>(
            inflater,
            R.layout.item_program_stage,
            parent,
            false
        )
        return ProgramStageSelectionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProgramStageSelectionViewHolder, position: Int) {
        val programStage = programStages[position]
        holder.bind(presenter, programStage)
    }

    override fun getItemCount(): Int {
        return programStages.size
    }
}
