package org.dhis2.data.forms.dataentry.fields.optionset

import androidx.databinding.ViewDataBinding
import androidx.lifecycle.MutableLiveData
import io.reactivex.processors.FlowableProcessor
import org.dhis2.data.forms.dataentry.fields.FieldViewModel
import org.dhis2.data.forms.dataentry.fields.FormViewHolder
import org.dhis2.data.forms.dataentry.fields.RowAction
import org.dhis2.databinding.FormOptionSetSelectorBinding
import org.dhis2.utils.customviews.OptionSetSelectionView

class OptionSetHolder(
    binding: ViewDataBinding,
    private val processor: FlowableProcessor<RowAction>,
    private val isBgTransparent: Boolean,
    currentSelection: MutableLiveData<String>
) : FormViewHolder(binding) {

    private lateinit var viewModel: OptionSetViewModel
    private var formBinding: FormOptionSetSelectorBinding

    init {
        this.currentUid = currentSelection
        this.formBinding = binding as FormOptionSetSelectorBinding

        formBinding.optionSetSelectionView.setOnSelectedOptionListener(
            object : OptionSetSelectionView.OnSelectedOption {
                override fun onSelectedOption(optionName: String?, optionCode: String?) {
                    processor.onNext(RowAction.create(viewModel.uid(), optionCode))
                }

                override fun onOptionsClear() {
                    processor.onNext(RowAction.create(viewModel.uid(), null))
                }
            }
        )
    }

    override fun update(viewModel: FieldViewModel) {
        this.viewModel = viewModel as OptionSetViewModel
        label = StringBuilder().append(viewModel.label())
        formBinding.optionSetSelectionView.setOptionsToShow(
            viewModel.optionsToHide, viewModel.optionsToShow
        )
        formBinding.optionSetSelectionView.setObjectStyle(viewModel.objectStyle())
        formBinding.optionSetSelectionView.setEditable(viewModel.editable() == true)
        formBinding.optionSetSelectionView.setInitValue(
            viewModel.value(), viewModel.options(), viewModel.fieldRendering().type()
        )
        formBinding.optionSetSelectionView.setLabel(viewModel.label(), viewModel.mandatory())
        formBinding.optionSetSelectionView.setDescription(viewModel.description())

        setFormFieldBackground()
    }
}
