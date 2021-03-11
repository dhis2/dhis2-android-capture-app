package org.dhis2.utils.customviews

import android.content.Context
import android.view.Menu
import android.view.View
import android.widget.PopupMenu
import java.util.Date
import org.dhis2.data.dhislogic.inDateRange
import org.hisp.dhis.android.core.category.CategoryOption

class CatOptionPopUp(
    val context: Context,
    anchor: View,
    private val categoryName: String,
    val options: List<CategoryOption>,
    private val showOnlyWithAccess: Boolean,
    val date: Date?,
    private val onOptionSelected: (CategoryOption?) -> Unit
) :
    PopupMenu(context, anchor) {

    private val selectableOptions = options.filter { option ->
        if (showOnlyWithAccess) {
            option.access().data().write()
        } else {
            true
        }
    }.filter { option ->
        option.inDateRange(date)
    }

    override fun show() {
        setOnMenuItemClickListener {
            when (it.order) {
                0 -> onOptionSelected.invoke(null)
                else -> onOptionSelected.invoke(selectableOptions[it.order - 1])
            }
            true
        }

        selectableOptions.forEachIndexed { index, option ->
            menu.add(Menu.NONE, Menu.NONE, index + 1, option.displayName())
        }
        super.show()
    }
}
