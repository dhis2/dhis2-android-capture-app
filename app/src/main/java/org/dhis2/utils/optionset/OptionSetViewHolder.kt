package org.dhis2.utils.optionset

import androidx.recyclerview.widget.RecyclerView

import org.dhis2.databinding.ItemOptionBinding
import org.dhis2.utils.custom_views.OptionSetOnClickListener
import org.hisp.dhis.android.core.option.Option

class OptionSetViewHolder internal constructor(private val binding: ItemOptionBinding) : RecyclerView.ViewHolder(binding.root) {

    fun bind(option: Option, listener: OptionSetOnClickListener) {
        binding.option = option.displayName()
        binding.executePendingBindings()

        itemView.setOnClickListener { listener.onSelectOption(option) }
    }
}
