package org.dhis2.usescases.main.program

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import java.util.ArrayList
import org.dhis2.R
import org.dhis2.databinding.ItemProgramModelBinding

/**
 * QUADRAM. Created by ppajuelo on 13/06/2018.
 */

class ProgramModelAdapter(
    private val presenter: ProgramPresenter
) : RecyclerView.Adapter<ProgramModelHolder>() {

    private val programList: MutableList<ProgramViewModel>

    init {
        this.programList = ArrayList()
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProgramModelHolder {
        val binding = DataBindingUtil.inflate<ItemProgramModelBinding>(
            LayoutInflater.from(parent.context),
            R.layout.item_program_model,
            parent,
            false
        )
        return ProgramModelHolder(binding)
    }

    override fun onBindViewHolder(holder: ProgramModelHolder, position: Int) {
        holder.bind(presenter, programList[holder.adapterPosition])
    }

    override fun getItemId(position: Int): Long {
        return programList[position].id().hashCode().toLong()
    }

    override fun getItemCount(): Int {
        return programList.size
    }

    fun setData(data: List<ProgramViewModel>) {
        val diffResult = DiffUtil.calculateDiff(ProgramDiffUtil(programList, data))
        this.programList.clear()
        this.programList.addAll(data)
        diffResult.dispatchUpdatesTo(this)
    }

    private class ProgramDiffUtil(
        val oldFields: List<ProgramViewModel>,
        val newFields: List<ProgramViewModel>
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int = oldFields.size
        override fun getNewListSize(): Int = newFields.size

        override fun areItemsTheSame(oldItem: Int, newItem: Int): Boolean {
            return oldFields[oldItem].id() == newFields[newItem].id()
        }

        override fun areContentsTheSame(oldItem: Int, newItem: Int): Boolean {
            return oldFields[oldItem] == newFields[newItem]
        }
    }
}
