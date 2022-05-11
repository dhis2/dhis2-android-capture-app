package org.dhis2.form.ui.binding

import android.widget.ArrayAdapter
import androidx.appcompat.widget.ListPopupWindow
import android.view.Menu
import android.view.View
import android.widget.PopupMenu
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.google.android.material.textfield.TextInputEditText
import org.dhis2.form.R
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.UiEventType

@BindingAdapter("options")
fun TextInputEditText.addOptions(field: FieldUiModel) {
    setOnClickListener { view ->
        showOptions(view, field)
    }
}

@BindingAdapter("options")
fun TextView.addOptions(field: FieldUiModel) {
    setOnClickListener { view ->
        showOptions(view, field)
    }
}

private fun showOptions(view: View, field: FieldUiModel) {
    when (field.optionsToDisplay?.size) {
        in 0..15 -> {
            ListPopupWindow(view.context, null, R.attr.listPopupWindowStyle).apply {
                anchorView = view
                val list = field.optionsToDisplay?.map { it.displayName() } ?: emptyList()
                val adapter = ArrayAdapter(view.context, R.layout.pop_up_menu_item, list)
                setAdapter(adapter)

                setOnItemClickListener { _, _, position, _ ->
                    field.optionsToDisplay?.get(position)?.code()?.let { field.onSave(it) }
                    dismiss()
                }
                show()
            }
        }
        else -> field.invokeUiEvent(UiEventType.OPTION_SET)
    }
    field.onItemClick()
}
