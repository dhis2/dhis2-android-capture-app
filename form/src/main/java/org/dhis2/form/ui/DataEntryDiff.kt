package org.dhis2.form.ui

import androidx.recyclerview.widget.DiffUtil
import org.dhis2.form.model.FieldUiModel

class DataEntryDiff : DiffUtil.ItemCallback<FieldUiModel>() {
    override fun areItemsTheSame(oldItem: FieldUiModel, newItem: FieldUiModel): Boolean =
        oldItem.uid == newItem.uid

    override fun areContentsTheSame(oldItem: FieldUiModel, newItem: FieldUiModel): Boolean =
        oldItem.equals(newItem)
}
