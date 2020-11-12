package org.dhis2.data.forms.dataentry.fields.scan

import androidx.databinding.ViewDataBinding
import androidx.lifecycle.MutableLiveData
import io.reactivex.processors.FlowableProcessor
import org.dhis2.data.forms.dataentry.fields.FieldViewModel
import org.dhis2.data.forms.dataentry.fields.FormViewHolder
import org.dhis2.data.forms.dataentry.fields.RowAction

class ScanTextHolder(
    binding: ViewDataBinding,
    val processor: FlowableProcessor<RowAction>,
    val isSearchMode: Boolean,
    val currentSelection: MutableLiveData<String>?
) : FormViewHolder(binding) {

    init {
        this.currentUid = currentSelection
    }

    public override fun update(viewModel: FieldViewModel) {
    }
}
