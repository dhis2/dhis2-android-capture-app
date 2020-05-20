package org.dhis2.data.forms.dataentry.fields.optionset

import androidx.lifecycle.MutableLiveData
import io.reactivex.processors.FlowableProcessor
import org.dhis2.data.forms.dataentry.fields.FormViewHolder
import org.dhis2.data.forms.dataentry.fields.RowAction
import org.dhis2.databinding.FormOptionSetSelectorBinding
import org.dhis2.utils.customviews.OptionSetSelectionView
import org.jetbrains.annotations.NotNull

class OptionSetHolder(
    private val formBinding: @NotNull FormOptionSetSelectorBinding,
    private val processor: FlowableProcessor<RowAction>,
    private val isBgTransparent: Boolean,
    currentSelection: MutableLiveData<String>
) : FormViewHolder(formBinding) {

    private lateinit var viewModel: OptionSetViewModel

    init {
        this.currentUid = currentSelection

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

    fun update(viewModel: OptionSetViewModel) {
        this.viewModel = viewModel
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
    }
}
