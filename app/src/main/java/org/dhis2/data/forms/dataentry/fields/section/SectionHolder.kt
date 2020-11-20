package org.dhis2.data.forms.dataentry.fields.section

import androidx.databinding.ViewDataBinding
import org.dhis2.data.forms.dataentry.fields.FieldViewModel
import org.dhis2.data.forms.dataentry.fields.FormViewHolder
import org.dhis2.databinding.FormSectionBinding
import org.jetbrains.annotations.NotNull

class SectionHolder(
    binding: @NotNull ViewDataBinding
) : FormViewHolder(binding) {

    private val formBinding: FormSectionBinding = binding as FormSectionBinding

    public override fun update(viewModel: FieldViewModel) {
        (viewModel as SectionViewModel).apply {
            formBinding.sectionView.setViewModel(this)
        }
    }
}
