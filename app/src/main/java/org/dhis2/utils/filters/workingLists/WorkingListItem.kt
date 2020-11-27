package org.dhis2.utils.filters.workingLists

import org.dhis2.utils.filters.FilterManager
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus

data class WorkingListItem(
    val uid: String,
    val label: String,
    val enrollentStatus: EnrollmentStatus?
) {
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

    fun isSelected():Boolean {
        return FilterManager.getInstance().currentWorkingList()?.uid == uid
    }
}