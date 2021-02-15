package org.dhis2.data.forms.dataentry

import androidx.recyclerview.widget.DiffUtil
import org.dhis2.data.forms.dataentry.fields.FieldUiModel
import org.dhis2.data.forms.dataentry.fields.optionset.OptionSetViewModel
import org.dhis2.data.forms.dataentry.fields.section.SectionViewModel
import org.dhis2.data.forms.dataentry.fields.spinner.SpinnerViewModel

class DataEntryDiff : DiffUtil.ItemCallback<FieldUiModel>() {
    override fun areItemsTheSame(oldItem: FieldUiModel, newItem: FieldUiModel): Boolean =
        oldItem.getUid() == newItem.getUid()

    override fun areContentsTheSame(oldItem: FieldUiModel, newItem: FieldUiModel): Boolean =
        if (newItem is SectionViewModel ||
            oldItem is SectionViewModel ||
            oldItem is OptionSetViewModel ||
            oldItem is SpinnerViewModel
        ) {
            false
        } else {
            oldItem.equals(newItem)
        }
}
