package org.dhis2.data.forms.dataentry.fields.image

import android.view.View
import androidx.databinding.ObservableField
import io.reactivex.processors.FlowableProcessor
import org.dhis2.Bindings.Bindings
import org.dhis2.data.forms.dataentry.fields.FormViewHolder
import org.dhis2.data.forms.dataentry.fields.RowAction
import org.dhis2.databinding.FormImageBinding

class ImageHolder(
    private val imageBinding: FormImageBinding,
    processor: FlowableProcessor<RowAction?>,
    imageSelector: ObservableField<String?>
) : FormViewHolder(imageBinding) {

    private val currentSelector: ObservableField<String?> = imageSelector
    private var isEditable = false
    private var model: ImageViewModel? = null

    init {
        itemView.setOnClickListener {
            if (isEditable) {
                val label = model!!.optionDisplayName()
                val code = model!!.optionCode()

                val value = if (imageSelector.get() == label) {
                    currentSelector.set("")
                    null
                } else {
                    currentSelector.set(label)
                    code
                }
                processor.onNext(RowAction.create(model!!.fieldUid(), value, adapterPosition))
            }
        }
    }

    fun update(viewModel: ImageViewModel) {
        model = viewModel

        isEditable = viewModel.editable()!!
        descriptionText = viewModel.description()

        label = StringBuilder(viewModel.formattedLabel)

        imageBinding.apply {
            setLabel(viewModel.formattedLabel)
            optionName = viewModel.optionDisplayName()
            currentSelection = currentSelector
            errorMessage.apply {
                visibility = if (viewModel.shouldShowError()) View.VISIBLE else View.GONE
                text = viewModel.errorMessage
            }
            Bindings.setObjectStyle(icon, itemView, viewModel.objectStyle())
            Bindings.setObjectStyle(label, itemView, viewModel.objectStyle())
        }

        viewModel.value()?.let { value ->
            if (value != currentSelector.get()) {
                currentSelector.set(value)
            }
        } ?: if (currentSelector.get() != null) {
            currentSelector.set("")
        }
    }
}
