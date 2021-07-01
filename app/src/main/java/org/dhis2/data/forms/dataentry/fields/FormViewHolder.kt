package org.dhis2.data.forms.dataentry.fields

import android.widget.ImageView
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import org.dhis2.BR
import org.dhis2.Bindings.clipWithAllRoundedCorners
import org.dhis2.Bindings.dp
import org.dhis2.R
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.ui.RecyclerViewUiEvents
import org.dhis2.form.ui.intent.FormIntent

class FormViewHolder(private val binding: ViewDataBinding) : RecyclerView.ViewHolder(
    binding.root
) {

    init {
        val fieldSelected = binding.root.findViewById<ImageView>(R.id.fieldSelected)
        fieldSelected?.clipWithAllRoundedCorners(2.dp)
    }

    fun bind(uiModel: FieldUiModel, callback: FieldItemCallback) {
        val itemCallback: FieldUiModel.Callback = object : FieldUiModel.Callback {
            override fun recyclerViewUiEvents(uiEvent: RecyclerViewUiEvents) {
                callback.recyclerViewEvent(uiEvent)
            }

            override fun intent(intent: FormIntent) {
                var formIntent = intent
                if (intent is FormIntent.OnNext) {
                    formIntent = intent.copy(
                        position = layoutPosition
                    )
                }
                callback.intent(formIntent)
            }
        }
        uiModel.setCallback(itemCallback)
        binding.setVariable(BR.item, uiModel)
        binding.executePendingBindings()
    }

    interface FieldItemCallback {
        fun intent(intent: FormIntent)
        fun recyclerViewEvent(uiEvent: RecyclerViewUiEvents)
    }
}
