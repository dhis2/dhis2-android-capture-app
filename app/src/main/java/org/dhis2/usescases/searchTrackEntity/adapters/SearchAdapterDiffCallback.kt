package org.dhis2.usescases.searchTrackEntity.adapters

import androidx.recyclerview.widget.DiffUtil
import org.dhis2.usescases.searchTrackEntity.SearchTeiModel

class SearchAdapterDiffCallback : DiffUtil.ItemCallback<SearchTeiModel>() {
    override fun areItemsTheSame(
        oldItem: SearchTeiModel,
        newItem: SearchTeiModel,
    ): Boolean = oldItem.tei?.uid() == newItem.tei?.uid()

    override fun areContentsTheSame(
        oldItem: SearchTeiModel,
        newItem: SearchTeiModel,
    ): Boolean =
        oldItem.tei?.uid() == newItem.tei?.uid() &&
            oldItem.tei?.state() == newItem.tei?.state() &&
            oldItem.attributeValues == newItem.attributeValues &&
            oldItem.enrollments == newItem.enrollments &&
            oldItem.profilePicturePath == newItem.profilePicturePath &&
            oldItem.isAttributeListOpen == newItem.isAttributeListOpen &&
            oldItem.sortingKey == newItem.sortingKey &&
            oldItem.sortingValue == newItem.sortingValue &&
            oldItem.enrolledOrgUnit == newItem.enrolledOrgUnit

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
