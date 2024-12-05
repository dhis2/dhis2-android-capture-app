package org.dhis2.usescases.eventsWithoutRegistration.eventDetails.domain

import androidx.paging.PagingData
import androidx.paging.map
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import org.dhis2.commons.periods.Period
import org.dhis2.commons.periods.PeriodUseCase
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.data.EventDetailsRepository
import org.hisp.dhis.android.core.period.PeriodType

class ConfigurePeriodSelector(
    private val eventDetailRepository: EventDetailsRepository,
    private val periodUseCase: PeriodUseCase,
) {
    operator fun invoke(): Flow<PagingData<Period>> {
        val programStage = eventDetailRepository.getProgramStage() ?: return emptyFlow()
        val event = eventDetailRepository.getEvent()
        val periodType = programStage.periodType() ?: PeriodType.Daily
        return with(periodUseCase) {
            val unavailableDate = getEventUnavailableDates(
                programStageUid = programStage.uid(),
                enrollmentUid = event?.enrollment(),
                currentEventUid = event?.uid(),
            )
            fetchPeriods(
                periodType = periodType,
                selectedDate = if (eventDetailRepository.isScheduling()) {
                    event?.dueDate()
                } else {
                    event?.eventDate()
                },
                initialDate = getEventPeriodMinDate(
                    programStage = programStage,
                    isScheduling = eventDetailRepository.isScheduling(),
                    eventEnrollmentUid = event?.enrollment(),
                ),
                maxDate = getEventPeriodMaxDate(
                    programStage = programStage,
                    isScheduling = eventDetailRepository.isScheduling(),
                    eventEnrollmentUid = event?.enrollment(),
                ),
            ).map { paging ->
                paging.map { period ->
                    period.copy(enabled = unavailableDate.contains(period.startDate).not())
                }
            }
        }
    }
}
