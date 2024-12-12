package org.dhis2.usescases.eventsWithoutRegistration.eventDetails.domain

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.dhis2.commons.periods.domain.GetEventPeriods
import org.dhis2.commons.periods.model.Period
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.data.EventDetailsRepository
import org.hisp.dhis.android.core.period.PeriodType

class ConfigurePeriodSelector(
    private val eventDetailRepository: EventDetailsRepository,
    private val periodUseCase: GetEventPeriods,
) {
    operator fun invoke(): Flow<PagingData<Period>> {
        val programStage = eventDetailRepository.getProgramStage() ?: return emptyFlow()
        val event = eventDetailRepository.getEvent() ?: return emptyFlow()
        val periodType = programStage.periodType() ?: PeriodType.Daily
        return with(periodUseCase) {
            fetchPeriods(
                eventUid = event.uid(),
                periodType = periodType,
                selectedDate = if (eventDetailRepository.isScheduling()) {
                    event.dueDate()
                } else {
                    event.eventDate()
                },
                programStage = programStage,
                isScheduling = eventDetailRepository.isScheduling(),
                eventEnrollmentUid = event.enrollment(),
            )
        }
    }
}
