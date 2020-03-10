package org.dhis2.Bindings

import org.hisp.dhis.android.core.arch.helpers.UidsHelper
import org.hisp.dhis.android.core.category.CategoryOptionCombo
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.event.EventCollectionRepository
import org.hisp.dhis.android.core.event.EventStatus
import org.hisp.dhis.android.core.period.DatePeriod
import java.util.Date

fun Event.primaryDate(): Date {
    return when (status()) {
        EventStatus.ACTIVE -> eventDate()!!
        EventStatus.COMPLETED -> eventDate()!!
        EventStatus.SCHEDULE -> dueDate()!!
        EventStatus.SKIPPED -> dueDate()!!
        EventStatus.VISITED -> eventDate()!!
        EventStatus.OVERDUE -> dueDate()!!
        null -> Date()
    }
}

fun EventCollectionRepository.applyFilters(
    periodFilters: MutableList<DatePeriod>,
    orgUnitFilters: MutableList<String>,
    stateFilters: MutableList<State>,
    assignedUser: String?,
    eventStatusFilters: MutableList<EventStatus>,
    catOptComboFilters: MutableList<CategoryOptionCombo>
): EventCollectionRepository {
    var eventRepo = this

    if (periodFilters.isNotEmpty()) {
        eventRepo = eventRepo.byEventDate().inDatePeriods(periodFilters)
    }
    if (orgUnitFilters.isNotEmpty()) {
        eventRepo = eventRepo.byOrganisationUnitUid().`in`(orgUnitFilters)
    }
    if (catOptComboFilters.isNotEmpty()) {
        eventRepo = eventRepo.byAttributeOptionComboUid()
            .`in`(UidsHelper.getUids<CategoryOptionCombo>(catOptComboFilters))
    }
    if (eventStatusFilters.isNotEmpty()) {
        eventRepo = eventRepo.byStatus().`in`(eventStatusFilters)
    }
    if (stateFilters.isNotEmpty()) {
        eventRepo = eventRepo.byState().`in`(stateFilters)
    }
    if (assignedUser != null) {
        eventRepo = eventRepo.byAssignedUser().eq(assignedUser)
    }

    return eventRepo
}
