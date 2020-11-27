package org.dhis2.utils.filters.workingLists

import org.dhis2.utils.filters.FilterManager
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus
import org.hisp.dhis.android.core.event.EventStatus

sealed class WorkingListItem(
    open val uid: String,
    open val label: String
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

    fun deselect() {
        isActive = false
    }

    fun isSelected(): Boolean {
        return FilterManager.getInstance().currentWorkingList()?.uid == uid
    }
}

data class TeiWorkingListItem(
    override val uid: String,
    override val label: String,
    val enrollentStatus: EnrollmentStatus?
) : WorkingListItem(uid, label)

data class EventWorkingListItem(
    override val uid: String,
    override val label: String,
    val assignedToMe: Boolean?,
    val eventDatePeriod: String?,
    val eventStatus: EventStatus?,
    val orgUnit: String?
) : WorkingListItem(uid, label)
