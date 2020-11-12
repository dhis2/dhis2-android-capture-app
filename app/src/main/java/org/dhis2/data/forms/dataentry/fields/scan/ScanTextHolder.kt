package org.dhis2.data.forms.dataentry.fields.scan

import androidx.databinding.ViewDataBinding
import androidx.lifecycle.MutableLiveData
import org.dhis2.data.forms.dataentry.fields.FieldViewModel
import org.dhis2.data.forms.dataentry.fields.FormViewHolder

class ScanTextHolder(
    binding: ViewDataBinding,
    val currentSelection: MutableLiveData<String>?
) : FormViewHolder(binding) {

    init {
        this.currentUid = currentSelection
    }

    public override fun update(viewModel: FieldViewModel) {
    }
}
