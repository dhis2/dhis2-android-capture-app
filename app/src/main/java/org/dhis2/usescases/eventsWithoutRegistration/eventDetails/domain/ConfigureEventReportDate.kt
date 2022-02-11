package org.dhis2.usescases.eventsWithoutRegistration.eventDetails.domain

import org.dhis2.commons.data.EventCreationType
import org.dhis2.commons.data.EventCreationType.SCHEDULE
import org.dhis2.commons.date.DateUtils
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.models.ReportingDate
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.providers.EventDetailResourcesProvider
import org.hisp.dhis.android.core.D2

class ConfigureEventReportDate(
    private val d2: D2,
    private val eventId: String?,
    private val programStageId: String,
    private val creationType: EventCreationType,
    private val resourceProvider: EventDetailResourcesProvider
) {

    operator fun invoke(): ReportingDate {
        return ReportingDate(
            active = isActive(),
            hint = getLabel(),
            value = getValue()
        )
    }

    private fun isActive(): Boolean {
        getProgramStageById()?.let {
            if (creationType == SCHEDULE && it.hideDueDate() == true) {
                return false
            }
        }
        return true
    }

    private fun getLabel(): String {
        return when (creationType) {
            SCHEDULE ->
                getProgramStageById()?.dueDateLabel()
                    ?: resourceProvider.provideDueDate()
            else -> {
                getProgramStageById()?.executionDateLabel()
                    ?: resourceProvider.provideEventDate()
            }
        }
    }

    private fun getValue(): String? {
        return getEventById()?.eventDate()?.let {
            DateUtils.uiDateFormat().format(it)
        }
    }

    private fun getEventById() =
        d2.eventModule().events().uid(eventId).blockingGet()

    private fun getProgramStageById() =
        d2.programModule().programStages().uid(programStageId).blockingGet()
}
