package org.dhis2.android.rtsm.ui.filter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.dhis2.android.rtsm.data.models.StockItem
import org.dhis2.android.rtsm.databinding.ItemDropdownBinding

class FilterAdapter
constructor(
    private val data: MutableList<StockItem>
): RecyclerView.Adapter<FilterViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        FilterViewHolder (
            ItemDropdownBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

    override fun onBindViewHolder(holder: FilterViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount() = data.size

    fun submitList(items: List<StockItem>) {
        data.run {
            clear()
            addAll(items)
        }
    }
}