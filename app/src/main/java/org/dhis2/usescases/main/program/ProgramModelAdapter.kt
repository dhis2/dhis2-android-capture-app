package org.dhis2.usescases.main.program

import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup

import org.dhis2.R
import org.dhis2.databinding.ItemProgramModelBinding
import org.dhis2.utils.Period

import java.util.ArrayList
import java.util.Collections

/**
 * QUADRAM. Created by ppajuelo on 13/06/2018.
 */

class ProgramModelAdapter internal constructor(private val presenter: ProgramContract.Presenter) : RecyclerView.Adapter<ProgramModelHolder>() {
    private val programList: MutableList<ProgramViewModel>

    init {
        this.programList = ArrayList()
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProgramModelHolder {
        val binding = DataBindingUtil.inflate<ItemProgramModelBinding>(LayoutInflater.from(parent.context), R.layout.item_program_model, parent, false)
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
        this.programList.clear()
        this.programList.addAll(data)
        notifyDataSetChanged()
    }
}
