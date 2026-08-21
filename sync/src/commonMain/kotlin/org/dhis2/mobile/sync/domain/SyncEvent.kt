package org.dhis2.mobile.sync.domain

import org.dhis2.mobile.commons.domain.UseCase
import org.dhis2.mobile.commons.error.DomainError
import org.dhis2.mobile.commons.session.SessionRenewalNotifier
import org.dhis2.mobile.sync.data.SyncEventRepository

internal typealias EventUid = String

internal class SyncEvent(
    private val syncEventRepository: SyncEventRepository,
    private val sessionRenewalNotifier: SessionRenewalNotifier,
) : UseCase<EventUid, Unit> {
    override suspend fun invoke(input: EventUid): Result<Unit> =
        try {
            syncEventRepository.uploadEvent(input)
            syncEventRepository.downloadEvent(input)
            syncEventRepository.downloadFileResources(input)
            Result.success(Unit)
        } catch (domainError: DomainError) {
            sessionRenewalNotifier.notifyIfRequired(domainError)
            Result.failure(domainError)
        } catch (e: Exception) {
            Result.failure(e)
        }
}
