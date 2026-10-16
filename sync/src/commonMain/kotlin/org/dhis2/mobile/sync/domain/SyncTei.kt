package org.dhis2.mobile.sync.domain

import org.dhis2.mobile.commons.domain.UseCase
import org.dhis2.mobile.commons.error.DomainError
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
        } catch (domainError: DomainError) {
            sessionRenewalNotifier.notifyIfRequired(domainError)
            Result.failure(domainError)
        } catch (e: Exception) {
            Result.failure(e)
        }
}
