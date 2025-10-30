package org.dhis2.tracker.events

class CreateEventUseCase(
    private val repository: CreateEventUseCaseRepository,
) {
    suspend operator fun invoke(
        programUid: String,
        orgUnitUid: String,
        programStageUid: String,
        enrollmentUid: String?,
    ) = repository.createEvent(enrollmentUid, programUid, programStageUid, orgUnitUid)
}
