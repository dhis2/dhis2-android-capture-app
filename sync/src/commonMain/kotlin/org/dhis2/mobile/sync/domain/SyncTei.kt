package org.dhis2.mobile.sync.domain

import org.dhis2.mobile.commons.domain.UseCase
import org.dhis2.mobile.sync.data.SyncTeiRepository

internal typealias EnrollmentUid = String

internal class SyncTei(
    private val syncTeiRepository: SyncTeiRepository,
) : UseCase<String, Unit> {
    override suspend fun invoke(input: EnrollmentUid): Result<Unit> =
        try {
            val enrollmentInfo = syncTeiRepository.getEnrollmentInfo(input)
            syncTeiRepository.uploadTei(enrollmentInfo)
            syncTeiRepository.downloadTei(enrollmentInfo)
            syncTeiRepository.downloadFileResources(enrollmentInfo)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
}
