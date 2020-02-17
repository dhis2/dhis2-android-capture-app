package org.dhis2.data.forms.dataentry

import androidx.recyclerview.widget.DiffUtil
import org.dhis2.data.forms.dataentry.fields.FieldViewModel

class DataEntryDiff : DiffUtil.ItemCallback<FieldViewModel>() {
    override fun areItemsTheSame(oldItem: FieldViewModel, newItem: FieldViewModel): Boolean =
        oldItem == newItem

    override fun areContentsTheSame(oldItem: FieldViewModel, newItem: FieldViewModel): Boolean =
        oldItem == newItem
}