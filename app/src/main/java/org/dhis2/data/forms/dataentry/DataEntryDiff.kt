package org.dhis2.data.forms.dataentry

import androidx.recyclerview.widget.DiffUtil
import org.dhis2.data.forms.dataentry.fields.FieldUiModel

class DataEntryDiff : DiffUtil.ItemCallback<FieldUiModel>() {
    override fun areItemsTheSame(oldItem: FieldUiModel, newItem: FieldUiModel): Boolean =
        oldItem.getUid() == newItem.getUid()

    override fun areContentsTheSame(oldItem: FieldUiModel, newItem: FieldUiModel): Boolean =
        oldItem.equals(newItem)
}
