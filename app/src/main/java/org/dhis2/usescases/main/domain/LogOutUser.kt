package org.dhis2.usescases.main.domain

import org.dhis2.commons.filters.FilterManager
import org.dhis2.data.service.SyncStatusController
import org.dhis2.data.service.workManager.WorkManagerController
import org.dhis2.mobile.commons.domain.UseCase
import org.dhis2.mobile.commons.error.DomainError
import org.dhis2.usescases.main.data.HomeRepository

typealias AccountCount = Int

class LogOutUser(
    private val workManagerController: WorkManagerController,
    private val syncStatusController: SyncStatusController,
    private val filterManager: FilterManager,
    private val repository: HomeRepository,
) : UseCase<Unit, AccountCount> {
    override suspend fun invoke(input: Unit): Result<AccountCount> =
        try {
            workManagerController.cancelAllWork()
            syncStatusController.restore()
            filterManager.clearAllFilters()
            repository.clearPin()
            repository.logOut()
            val accountCount = repository.accountsCount()
            Result.success(accountCount)
        } catch (domainError: DomainError) {
            Result.failure(domainError)
        }
}
