package org.dhis2.usescases.programEventDetail.usecase

import kotlinx.coroutines.withContext
import org.dhis2.commons.date.DateUtils
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.event.EventCreateProjection
import org.hisp.dhis.android.core.maintenance.D2Error

class CreateEventUseCase(
    private val dispatcher: DispatcherProvider,
    private val d2: D2,
    private val dateUtils: DateUtils,
) {
    suspend operator fun invoke(
        programUid: String,
        orgUnitUid: String,
        programStageUid: String,
        enrollmentUid: String?,
    ): Result<String> = withContext(dispatcher.io()) {
        try {
            val eventUid = d2.eventModule().events().blockingAdd(
                EventCreateProjection.builder().apply {
                    enrollmentUid?.let { enrollment(enrollmentUid) }
                    program(programUid)
                    programStage(programStageUid)
                    organisationUnit(orgUnitUid)
                }.build(),
            )

            val eventRepository = d2.eventModule().events().uid(eventUid)
            eventRepository.setEventDate(dateUtils.today)

            Result.success(eventUid)
        } catch (error: D2Error) {
            Result.failure(error)
        }
    }
}
