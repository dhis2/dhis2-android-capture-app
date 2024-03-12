package org.dhis2.usescases.searchTrackEntity.listView

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import org.dhis2.databinding.ResultSearchListBinding

class SearchListResultAdapter(
    private val onSearchOutsideProgram: () -> Unit,
) : ListAdapter<SearchResult, SearchResultHolder>(
    object : DiffUtil.ItemCallback<SearchResult>() {
        override fun areItemsTheSame(oldItem: SearchResult, newItem: SearchResult): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: SearchResult, newItem: SearchResult): Boolean {
            return oldItem == newItem
        }
    },
) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchResultHolder {
        return SearchResultHolder(
            ResultSearchListBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false,
            ),
            onSearchOutsideProgram,
        )
    }

    override fun onBindViewHolder(holder: SearchResultHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun getItemViewType(position: Int): Int {
        return getItem(position).type.ordinal
    }
}
