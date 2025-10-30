package org.dhis2.usescases.main.domain

import org.dhis2.commons.Constants
import org.dhis2.data.service.VersionRepository
import org.dhis2.data.service.workManager.WorkManagerController
import org.dhis2.mobile.commons.domain.UseCase
import org.dhis2.mobile.commons.error.DomainError
import org.dhis2.usescases.main.data.HomeRepository

class LaunchInitialSync(
    private val skipSync: Boolean,
    private val homeRepository: HomeRepository,
    private val versionRepository: VersionRepository,
    private val workManagerController: WorkManagerController,
) : UseCase<Unit, InitialSyncAction> {
    override suspend fun invoke(input: Unit): Result<InitialSyncAction> =
        try {
            if (skipSync || homeRepository.isImportedDb() || homeRepository.getInitialSyncDone()) {
                Result.success(InitialSyncAction.Skip)
            } else {
                versionRepository.checkVersionUpdates()
                workManagerController
                    .syncDataForWorker(Constants.DATA_NOW, Constants.INITIAL_SYNC)
                Result.success(InitialSyncAction.Syncing)
            }
        } catch (domainError: DomainError) {
            Result.failure(domainError)
        }
}

sealed interface InitialSyncAction {
    object Skip : InitialSyncAction

    object Syncing : InitialSyncAction
}
