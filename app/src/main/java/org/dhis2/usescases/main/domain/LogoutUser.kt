package org.dhis2.usescases.main.domain

import org.dhis2.commons.filters.FilterManager
import org.dhis2.data.service.SyncStatusController
import org.dhis2.data.service.workManager.WorkManagerController
import org.dhis2.usescases.main.HomeRepository

typealias AccountCount = Int

class LogoutUser(
    private val repository: HomeRepository,
    private val workManagerController: WorkManagerController,
    private val syncStatusController: SyncStatusController,
    private val filterManager: FilterManager,
) {
    suspend operator fun invoke(): Result<AccountCount> {
        workManagerController.cancelAllWorkAndWait()
        syncStatusController.restore()
        filterManager.clearAllFilters()

        repository
            .clearSessionLock()
            .onFailure { return Result.failure(it) }

        repository
            .logOut()
            .onFailure { return Result.failure(it) }

        return Result.success(repository.accountsCount())
    }
}
