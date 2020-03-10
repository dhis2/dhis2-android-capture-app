package org.dhis2.data.forms.dataentry.fields.scan

import io.reactivex.processors.FlowableProcessor
import org.dhis2.data.forms.dataentry.fields.FormViewHolder
import org.dhis2.data.forms.dataentry.fields.RowAction
import org.dhis2.databinding.FormScanBinding
import org.dhis2.utils.customviews.ScanTextView

class ScanTextHolder(
    binding: FormScanBinding,
    val processor: FlowableProcessor<RowAction>
) : FormViewHolder(binding) {

    private lateinit var model: ScanTextViewModel
    private var scanTextView: ScanTextView = binding.scanTextView

    fun update(viewModel: ScanTextViewModel) {
        this.model = viewModel
        label = StringBuilder().append(viewModel.label())
        scanTextView.run {
            setText(model.value())
            setRenderingType(model.fieldRendering?.type())
            setLabel(model.label(), model.mandatory())
            setDescription(model.description())
            setAlert(model.warning(), model.error())
            setObjectStyle(model.objectStyle())
            updateEditable(model.editable() ?: false)
            optionSet = model.optionSet()
            setOnScannerListener { value ->
                this.setText(value)
                val rowAction = RowAction.create(
                    model.uid(), value,
                    adapterPosition
                )
                processor.onNext(rowAction)
            }
        }
    }

    override fun dispose() {
    }
}
