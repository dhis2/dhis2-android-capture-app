package org.dhis2.commons.filters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter

class FiltersAdapter :
    ListAdapter<FilterItem, FilterHolder>(
        object : DiffUtil.ItemCallback<FilterItem>() {
            override fun areItemsTheSame(
                oldItem: FilterItem,
                newItem: FilterItem,
            ): Boolean = oldItem == newItem

            override fun areContentsTheSame(
                oldItem: FilterItem,
                newItem: FilterItem,
            ): Boolean = oldItem == newItem
        },
    ) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): FilterHolder =
        FilterHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                Filters.values()[viewType].layoutId,
                parent,
                false,
            ),
        )

    override fun onBindViewHolder(
        holder: FilterHolder,
        position: Int,
    ) {
        holder.bind(getItem(position))
    }

    override fun getItemViewType(position: Int): Int = getItem(position).type.ordinal
}
