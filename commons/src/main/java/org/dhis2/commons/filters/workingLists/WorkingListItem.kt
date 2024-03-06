package org.dhis2.commons.filters.workingLists

import android.view.View
import org.dhis2.commons.filters.FilterManager

sealed class WorkingListItem(
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

class TrackedEntityInstanceWorkingList(uid: String, label: String) : WorkingListItem(uid, label)
class ProgramStageWorkingList(uid: String, label: String) : WorkingListItem(uid, label)
class EventWorkingList(uid: String, label: String) : WorkingListItem(uid, label)
