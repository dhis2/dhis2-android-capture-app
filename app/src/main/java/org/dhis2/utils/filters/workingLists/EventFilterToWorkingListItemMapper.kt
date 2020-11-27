package org.dhis2.utils.filters.workingLists

import org.hisp.dhis.android.core.common.AssignedUserMode
import org.hisp.dhis.android.core.common.DateFilterPeriod
import org.hisp.dhis.android.core.common.DatePeriodType
import org.hisp.dhis.android.core.event.EventFilter

class EventFilterToWorkingListItemMapper(
    private val defaultWorkingListLabel: String,
    private val relativePeriodMapper: RelativePeriodToStringMapper
) {
    fun map(eventFilter: EventFilter): WorkingListItem {
        return EventWorkingListItem(
            eventFilter.uid(),
            eventFilter.displayName() ?: defaultWorkingListLabel,
            eventFilter.eventQueryCriteria()?.assignedUserMode()
                ?.let { it == AssignedUserMode.CURRENT },
            eventFilter.eventQueryCriteria()?.eventDate()?.let {
                handleDatePeriodFilter(it)
            },
            eventFilter.eventQueryCriteria()?.eventStatus(),
            eventFilter.eventQueryCriteria()?.organisationUnit()
        )
    }

    private fun handleDatePeriodFilter(eventDatePeriod: DateFilterPeriod): String? {
        return when(eventDatePeriod.type()){
            DatePeriodType.RELATIVE -> relativePeriodMapper.map(eventDatePeriod.period())
            DatePeriodType.ABSOLUTE -> relativePeriodMapper.span().format(eventDatePeriod.startDate(), eventDatePeriod.endDate())
            null -> null
        }
    }
}