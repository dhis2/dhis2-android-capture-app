package org.dhis2.android.rtsm.ui.filter

import android.view.View
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.RecyclerView
import org.dhis2.android.rtsm.data.models.StockItem
import org.dhis2.android.rtsm.databinding.ItemDropdownBinding

class FilterViewHolder constructor(
    private val binding: ItemDropdownBinding
): RecyclerView.ViewHolder(binding.root) {

    fun bind(data: StockItem) {
        val adapter = ArrayAdapter(
            binding.root.context,
            android.R.layout.simple_spinner_dropdown_item,
            mutableListOf(data)
        )

        binding.filter.setAdapter(adapter)
    }
}