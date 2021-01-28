package org.dhis2.utils.customviews

import android.content.Context
import android.view.Menu
import android.view.View
import android.widget.PopupMenu
import java.util.Date
import org.dhis2.Bindings.inDateRange
import org.hisp.dhis.android.core.category.CategoryOption

class CatOptionPopUp(
    val context: Context,
    anchor: View,
    private val categoryName: String,
    val options: List<CategoryOption>,
    val showOnlyWithAccess: Boolean,
    val date: Date?,
    private val onOptionSelected: (CategoryOption?) -> Unit
) :
    PopupMenu(context, anchor) {

    override fun show() {
        setOnMenuItemClickListener {
            when (it.order) {
                0 -> onOptionSelected.invoke(null)
                else -> onOptionSelected.invoke(options[it.order - 1])
            }
            true
        }
        menu.add(Menu.NONE, Menu.NONE, 0, categoryName)
        options.filter { option ->
            showOnlyWithAccess && option.access().data().write()
        }.forEachIndexed { index, option ->
            if (option.inDateRange(date)) {
                menu.add(Menu.NONE, Menu.NONE, index + 1, option.displayName())
            }
        }
        super.show()
    }
}
