package org.dhis2.tracker.events

import kotlinx.coroutines.withContext
import org.dhis2.commons.viewmodel.DispatcherProvider

class CreateEventUseCase(
    private val dispatcher: DispatcherProvider,
    private val repository: CreateEventUseCaseRepository,
) {
    suspend operator fun invoke(
        programUid: String,
        orgUnitUid: String,
        programStageUid: String,
        enrollmentUid: String?,
    ): Result<String> = withContext(dispatcher.io()) {
        repository.createEvent(enrollmentUid, programUid, programStageUid, orgUnitUid)
    }

}
