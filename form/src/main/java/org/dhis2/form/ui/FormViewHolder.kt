package org.dhis2.form.ui

import android.text.TextWatcher
import android.widget.ImageView
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import org.dhis2.commons.bindings.clipWithAllRoundedCorners
import org.dhis2.commons.bindings.dp
import org.dhis2.form.BR
import org.dhis2.form.R
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.ui.event.RecyclerViewUiEvents
import org.dhis2.form.ui.intent.FormIntent

class FormViewHolder(private val binding: ViewDataBinding) : RecyclerView.ViewHolder(
    binding.root,
) {

    init {
        val fieldSelected = binding.root.findViewById<ImageView>(R.id.fieldSelected)
        fieldSelected?.clipWithAllRoundedCorners(2.dp)
    }

    fun bind(
        uiModel: FieldUiModel,
        callback: FieldItemCallback,
        textWatcher: TextWatcher,
        coordinateTextWatcher: LatitudeLongitudeTextWatcher,
    ) {
        val itemCallback: FieldUiModel.Callback = object : FieldUiModel.Callback {
            override fun recyclerViewUiEvents(uiEvent: RecyclerViewUiEvents) {
                callback.recyclerViewEvent(uiEvent)
            }

            override fun intent(intent: FormIntent) {
                var formIntent = intent
                if (intent is FormIntent.OnNext) {
                    formIntent = intent.copy(
                        position = layoutPosition,
                    )
                }
                callback.intent(formIntent)
            }
        }
        uiModel.setCallback(itemCallback)
        binding.setVariable(BR.textWatcher, textWatcher)
        binding.setVariable(BR.coordinateWatcher, coordinateTextWatcher)
        binding.setVariable(BR.item, uiModel)
        binding.executePendingBindings()
    }

    interface FieldItemCallback {
        fun intent(intent: FormIntent)
        fun recyclerViewEvent(uiEvent: RecyclerViewUiEvents)
    }
}
