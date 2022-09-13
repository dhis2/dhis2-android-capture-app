package org.dhis2.android.rtsm.ui.filter

import android.R
import androidx.recyclerview.widget.RecyclerView
import org.dhis2.android.rtsm.data.models.StockItem
import org.dhis2.android.rtsm.databinding.ItemDropdownBinding

class FilterViewHolder constructor(
    private val binding: ItemDropdownBinding
): RecyclerView.ViewHolder(binding.root) {

    fun bind(filter: StockItem) {

        val adapter = DropdownFilterAdapter(
            binding.root.context,
            R.layout.simple_spinner_dropdown_item,
            mutableListOf(filter)
        )

       //binding.filterDropdown.startIconDrawable = filter.icon().toDrawable()
        binding.filter.setAdapter(adapter)
        binding.filter.setText(filter.name)
    }
}