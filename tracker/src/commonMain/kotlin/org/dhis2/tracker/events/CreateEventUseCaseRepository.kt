package org.dhis2.tracker.events

interface CreateEventUseCaseRepository {
    suspend fun createEvent(
        enrollmentUid: String?,
        programUid: String,
        programStageUid: String?,
        orgUnitUid: String,
    ): Result<String>
}
