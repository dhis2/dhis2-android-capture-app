package org.dhis2.mobile.sync.domain

import kotlinx.coroutines.CancellationException
import org.dhis2.mobile.commons.domain.UseCase
import org.dhis2.mobile.commons.session.SessionRenewalNotifier
import org.dhis2.mobile.sync.data.SyncTeiRepository

internal typealias EnrollmentUid = String

internal class SyncTei(
    private val syncTeiRepository: SyncTeiRepository,
    private val sessionRenewalNotifier: SessionRenewalNotifier,
) : UseCase<String, Unit> {
    override suspend fun invoke(input: EnrollmentUid): Result<Unit> =
        try {
            val enrollmentInfo = syncTeiRepository.getEnrollmentInfo(input)
            syncTeiRepository.uploadTei(enrollmentInfo)
            syncTeiRepository.downloadTei(enrollmentInfo)
            syncTeiRepository.downloadFileResources(enrollmentInfo)
            Result.success(Unit)
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (error: Exception) {
            sessionRenewalNotifier.notifyIfRequired(error)
            Result.failure(error)
        }
}
