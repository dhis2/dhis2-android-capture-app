package org.dhis2.commons.periods.domain

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.dhis2.commons.periods.data.EventPeriodRepository
import org.dhis2.commons.periods.data.PeriodLabelProvider
import org.dhis2.commons.periods.model.Period
import org.hisp.dhis.android.core.period.PeriodType
import org.hisp.dhis.android.core.program.ProgramStage
import java.util.Date

class GetEventPeriods(
    private val eventPeriodRepository: EventPeriodRepository,
    private val periodLabelProvider: PeriodLabelProvider = PeriodLabelProvider(),
) {
    operator fun invoke(
        eventUid: String?,
        periodType: PeriodType,
        selectedDate: Date?,
        programStage: ProgramStage,
        isScheduling: Boolean,
        eventEnrollmentUid: String?,
    ): Flow<PagingData<Period>> =
        Pager(
            config = PagingConfig(pageSize = 20, maxSize = 100, initialLoadSize = 20),
            pagingSourceFactory = {
                eventPeriodRepository.getPeriodSource(
                    periodLabelProvider = periodLabelProvider,
                    periodType = periodType,
                    initialDate =
                        eventPeriodRepository.getEventPeriodMinDate(
                            programStage,
                            isScheduling,
                            eventEnrollmentUid,
                        ),
                    maxDate =
                        eventPeriodRepository.getEventPeriodMaxDate(
                            programStage,
                            isScheduling,
                            eventEnrollmentUid,
                        ),
                    selectedDate = selectedDate,
                )
            },
        ).flow
            .map { paging ->
                paging.map { period ->
                    period.copy(
                        enabled =
                            eventPeriodRepository
                                .getEventUnavailableDates(
                                    programStageUid = programStage.uid(),
                                    enrollmentUid = eventEnrollmentUid,
                                    currentEventUid = eventUid,
                                ).contains(period.startDate)
                                .not(),
                    )
                }
            }
}
