package org.dhis2.utils.filters.workingLists

import android.view.View
import org.dhis2.utils.filters.FilterManager

data class WorkingListItem(
    val uid: String,
    val label: String
) {
    private val itemId = View.generateViewId()

    fun id() = itemId

    private var isActive: Boolean = false

    private fun activateFilters() {
        isActive = true
        FilterManager.getInstance().currentWorkingList(this)
    }

    private fun clearFilters() {
        isActive = false
        FilterManager.getInstance().currentWorkingList(null)
    }

    fun select() {
        if (isActive) {
            clearFilters()
        } else {
            activateFilters()
        }
    }

    fun deselect() {
        isActive = false
    }

    fun isSelected(): Boolean {
        return FilterManager.getInstance().currentWorkingList()?.uid == uid
    }
}
