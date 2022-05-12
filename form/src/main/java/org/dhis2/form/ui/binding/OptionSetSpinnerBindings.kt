package org.dhis2.form.ui.binding

import android.view.Menu
import android.view.View
import android.widget.PopupMenu
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.google.android.material.textfield.TextInputEditText
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
            PopupMenu(view.context, view).apply {
                setOnMenuItemClickListener { item ->
                    dismiss()
                    val optionSelected =
                        field.optionsToDisplay?.first { it.displayName() == item.title }
                    field.onSave(optionSelected?.code())
                    true
                }
                field.optionsToDisplay?.forEachIndexed { index, option ->
                    menu.add(
                        Menu.NONE,
                        Menu.NONE,
                        index + 1,
                        option.displayName()
                    )
                }
                show()
            }
        }
        else -> field.invokeUiEvent(UiEventType.OPTION_SET)
    }
    field.onItemClick()
}
