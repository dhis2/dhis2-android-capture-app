package org.dhis2.data.forms.dataentry.fields.optionset

import androidx.databinding.ViewDataBinding
import androidx.lifecycle.MutableLiveData
import org.dhis2.data.forms.dataentry.fields.FieldViewModel
import org.dhis2.data.forms.dataentry.fields.FormViewHolder

class OptionSetHolder(
    binding: ViewDataBinding,
    currentSelection: MutableLiveData<String>
) : FormViewHolder(binding) {

    init {
        this.currentUid = currentSelection
    }

    override fun update(viewModel: FieldViewModel) {
    }
}
