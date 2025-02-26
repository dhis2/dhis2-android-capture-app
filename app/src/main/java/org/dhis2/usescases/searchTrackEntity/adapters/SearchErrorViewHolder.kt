package org.dhis2.usescases.searchTrackEntity.adapters

import androidx.recyclerview.widget.RecyclerView
import org.dhis2.databinding.ItemSearchErrorBinding
import org.dhis2.usescases.searchTrackEntity.SearchTeiModel

class SearchErrorViewHolder(
    private val binding: ItemSearchErrorBinding,
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(item: SearchTeiModel) {
        binding.errorText.text = item.onlineErrorMessage
    }
}
