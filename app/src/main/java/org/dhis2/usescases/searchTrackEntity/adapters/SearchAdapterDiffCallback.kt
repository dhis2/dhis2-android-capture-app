package org.dhis2.usescases.searchTrackEntity.adapters

import androidx.recyclerview.widget.DiffUtil
import org.dhis2.usescases.searchTrackEntity.SearchTeiModel

class SearchAdapterDiffCallback : DiffUtil.ItemCallback<SearchTeiModel>() {
    override fun areItemsTheSame(
        oldItem: SearchTeiModel,
        newItem: SearchTeiModel,
    ): Boolean = oldItem.tei?.uid == newItem.tei?.uid

    override fun areContentsTheSame(
        oldItem: SearchTeiModel,
        newItem: SearchTeiModel
    ): Boolean {
        return oldItem.tei.equals(newItem.tei)
    }


    override fun getChangePayload(
        oldItem: SearchTeiModel,
        newItem: SearchTeiModel,
    ): Any? =
        if (oldItem != newItem) {
            newItem
        } else {
            super.getChangePayload(oldItem, newItem)
        }
}
