package org.dhis2.usescases.main.domain

import org.dhis2.commons.filters.FilterManager
import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.data.service.SyncStatusController
import org.dhis2.data.service.workManager.WorkManagerController
import org.dhis2.usescases.main.HomeRepository

typealias AccountCount = Int

class LogoutUser(
    val repository: HomeRepository,
    val workManagerController: WorkManagerController,
    val syncStatusController: SyncStatusController,
    val filterManager: FilterManager,
    val preferences: PreferenceProvider,
) {
    suspend operator fun invoke(): Result<AccountCount> {
        workManagerController.cancelAllWork()
        syncStatusController.restore()
        filterManager.clearAllFilters()
        repository.clearSessionLock()
        val logoutResult = repository.logOut()
        return when {
            logoutResult.isSuccess -> Result.success(repository.accountsCount())
            else -> Result.failure(logoutResult.exceptionOrNull()!!)
        }
    }
}
