package org.dhis2.usescases.main.domain

import org.dhis2.data.service.VersionRepository
import org.dhis2.mobile.commons.domain.UseCase
import org.dhis2.mobile.commons.error.DomainError
import org.dhis2.mobile.sync.data.SyncBackgroundJobAction
import org.dhis2.usescases.main.data.HomeRepository
import timber.log.Timber

class LaunchInitialSync(
    private val skipSync: Boolean,
    private val homeRepository: HomeRepository,
    private val versionRepository: VersionRepository,
    private val syncBackgroundJobAction: SyncBackgroundJobAction,
) : UseCase<Unit, InitialSyncAction> {
    override suspend fun invoke(input: Unit): Result<InitialSyncAction> =
        try {
            if (skipSync || homeRepository.isImportedDb() || homeRepository.getInitialSyncDone()) {
                Result.success(InitialSyncAction.Skip)
            } else {
                try {
                    versionRepository.downloadLatestVersionInfo()
                } catch (e: Exception) {
                    Timber.w(e, "Version check failed during initial sync")
                }
                syncBackgroundJobAction.launchDataSync(0)
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
