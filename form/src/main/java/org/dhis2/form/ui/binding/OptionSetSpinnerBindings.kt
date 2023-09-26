package org.dhis2.form.ui.binding

import android.view.View
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.widget.ListPopupWindow
import androidx.databinding.BindingAdapter
import com.google.android.material.textfield.TextInputEditText
import org.dhis2.form.R
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.OptionSetConfiguration
import org.dhis2.form.model.UiEventType
import java.lang.IllegalArgumentException

@BindingAdapter("options")
fun TextInputEditText.addOptions(field: FieldUiModel) {
    setOnClickListener { view ->
        showOptions(view, field)
    }
}

@BindingAdapter(value = ["options", "anchorView"])
fun TextView.addOptions(field: FieldUiModel, anchorView: TextInputEditText) {
    setOnClickListener {
        showOptions(anchorView, field)
    }
}

private fun showOptions(view: View, field: FieldUiModel) {
    when (val optionSetConfig = field.optionSetConfiguration) {
        is OptionSetConfiguration.BigOptionSet ->
            field.invokeUiEvent(UiEventType.OPTION_SET)
        is OptionSetConfiguration.DefaultOptionSet ->
            ListPopupWindow(view.context, null, R.attr.listPopupWindowStyle).apply {
                anchorView = view
                val list = optionSetConfig.optionsToDisplay().map { it.displayName() }
                val adapter = ArrayAdapter(view.context, R.layout.pop_up_menu_item, list)
                setAdapter(adapter)

                setOnItemClickListener { _, _, position, _ ->
                    optionSetConfig.optionsToDisplay()[position].code()?.let { field.onSave(it) }
                    dismiss()
                }
                show()
            }
        null -> {
            throw IllegalArgumentException("Unsupported OptionSetConfiguration")
        }
    }
    field.onItemClick()
}
