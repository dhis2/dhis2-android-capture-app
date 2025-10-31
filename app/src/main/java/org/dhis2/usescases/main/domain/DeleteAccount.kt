package org.dhis2.usescases.main.domain

import org.dhis2.commons.filters.FilterManager
import org.dhis2.data.service.SyncStatusController
import org.dhis2.data.service.workManager.WorkManagerController
import org.dhis2.mobile.commons.domain.UseCase
import org.dhis2.mobile.commons.error.DomainError
import org.dhis2.usescases.main.data.HomeRepository
import org.dhis2.usescases.settings.deleteCache
import java.io.File

class DeleteAccount(
    private val workManagerController: WorkManagerController,
    private val syncStatusController: SyncStatusController,
    private val filterManager: FilterManager,
    private val repository: HomeRepository,
) : UseCase<File?, AccountCount> {
    override suspend fun invoke(input: File?): Result<AccountCount> =
        try {
            workManagerController.cancelAllWork()
            workManagerController.pruneWork()
            syncStatusController.restore()
            filterManager.clearAllFilters()
            input?.let { repository.clearCache(it) }
            repository.clearPreferences()
            repository.wipeAll()
            repository.deleteCurrentAccount()
            val accountCount = repository.accountsCount()
            Result.success(accountCount)
        } catch (domainError: DomainError) {
            Result.failure(domainError)
        }
}
