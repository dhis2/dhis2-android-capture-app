package org.dhis2.utils.optionset

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil

import org.dhis2.R
import org.dhis2.databinding.ItemOptionBinding
import org.dhis2.utils.custom_views.OptionSetOnClickListener
import org.hisp.dhis.android.core.option.Option

class OptionSetAdapter internal constructor(private val listener: OptionSetOnClickListener) :
        PagedListAdapter<Option, OptionSetViewHolder>(object : DiffUtil.ItemCallback<Option>() {
    override fun areItemsTheSame(oldItem: Option, newItem: Option): Boolean {
        return oldItem === newItem
    }

    override fun areContentsTheSame(oldItem: Option, newItem: Option): Boolean {
        return oldItem.uid() == newItem.uid()
    }
}) {

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): OptionSetViewHolder {
        val binding = DataBindingUtil.inflate<ItemOptionBinding>(LayoutInflater.from(viewGroup.context), R.layout.item_option, viewGroup, false)
        return OptionSetViewHolder(binding)

    }

    override fun onBindViewHolder(holder: OptionSetViewHolder, position: Int) {
        holder.bind(getItem(position)!!, listener)
    }
}
