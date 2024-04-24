package org.dhis2.utils.customviews

import android.content.Context
import android.view.Menu
import android.view.View
import android.widget.PopupMenu
import org.dhis2.data.dhislogic.inDateRange
import org.dhis2.data.dhislogic.inOrgUnit
import org.hisp.dhis.android.core.category.CategoryOption
import java.util.Date

class CatOptionPopUp(
    val context: Context,
    anchor: View,
    val options: List<CategoryOption>,
    val date: Date?,
    private val orgUnitUid: String?,
    private val onOptionSelected: (CategoryOption?) -> Unit,
) : PopupMenu(context, anchor) {

    private val selectableOptions = options
        .filter { option ->
            option.access().data().write()
        }.filter { option ->
            option.inDateRange(date)
        }.filter { option ->
            option.inOrgUnit(orgUnitUid)
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
