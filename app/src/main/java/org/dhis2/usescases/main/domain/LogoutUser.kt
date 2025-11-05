package org.dhis2.usescases.main.domain

import org.dhis2.commons.filters.FilterManager
import org.dhis2.data.service.SyncStatusController
import org.dhis2.data.service.workManager.WorkManagerController
import org.dhis2.mobile.commons.error.DomainError
import org.dhis2.usescases.main.HomeRepository

typealias AccountCount = Int

class LogoutUser(
    private val repository: HomeRepository,
    private val workManagerController: WorkManagerController,
    private val syncStatusController: SyncStatusController,
    private val filterManager: FilterManager,
) {
    suspend operator fun invoke(): Result<AccountCount> =
        try {
            workManagerController.cancelAllWork()
            syncStatusController.restore()
            filterManager.clearAllFilters()
            repository.clearSessionLock()
            repository.logOut()
            Result.success(repository.accountsCount())
        } catch (e: DomainError) {
            Result.failure(e)
        }
}
