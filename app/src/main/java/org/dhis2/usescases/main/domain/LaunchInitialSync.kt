package org.dhis2.usescases.main.domain

import org.dhis2.data.service.VersionRepository
import org.dhis2.mobile.commons.domain.UseCase
import org.dhis2.mobile.commons.error.DomainError
import org.dhis2.usescases.main.data.HomeRepository

class LaunchInitialSync(
    private val skipSync: Boolean,
    private val homeRepository: HomeRepository,
    private val versionRepository: VersionRepository,
) : UseCase<Unit, InitialSyncAction> {
    override suspend fun invoke(input: Unit): Result<InitialSyncAction> =
        try {
            if (skipSync || homeRepository.isImportedDb() || homeRepository.getInitialSyncDone()) {
                Result.success(InitialSyncAction.Skip)
            } else {
                versionRepository.checkVersionUpdates()
                homeRepository.syncData()

                Result.success(InitialSyncAction.Syncing)
            }
        } catch (domainError: DomainError) {
            Result.failure(domainError)
        }
}

sealed interface InitialSyncAction {
    data object Skip : InitialSyncAction

    data object Syncing : InitialSyncAction
}
