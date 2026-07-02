package org.dhis2.mobile.sync.domain

import org.dhis2.mobile.commons.domain.UseCase
import org.dhis2.mobile.sync.data.SyncEventRepository

internal typealias EventUid = String

internal class SyncEvent(
    private val syncEventRepository: SyncEventRepository,
) : UseCase<EventUid, Unit> {
    override suspend fun invoke(input: EventUid): Result<Unit> =
        try {
            syncEventRepository.downloadEvent(input)
            syncEventRepository.uploadEvent(input)
            syncEventRepository.downloadFileResources(input)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
}
