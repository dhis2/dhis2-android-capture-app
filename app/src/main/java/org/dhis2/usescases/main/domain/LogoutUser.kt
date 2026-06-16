package org.dhis2.usescases.main.domain

import org.dhis2.commons.filters.FilterManager
import org.dhis2.mobile.commons.domain.UseCase
import org.dhis2.mobile.sync.data.SyncBackgroundJobAction
import org.dhis2.mobile.sync.domain.SyncStatusController
import org.dhis2.usescases.main.data.HomeRepository

typealias AccountCount = Int

class LogoutUser(
    private val repository: HomeRepository,
    private val syncBackgroundJobAction: SyncBackgroundJobAction,
    private val syncStatusController: SyncStatusController,
    private val filterManager: FilterManager,
) : UseCase<Unit, AccountCount> {
    override suspend operator fun invoke(input: Unit): Result<AccountCount> {
        syncBackgroundJobAction.cancelAll()
        syncStatusController.restore()
        filterManager.clearAllFilters()

        repository
            .clearPin()
            .onFailure { return Result.failure(it) }

        repository
            .logOut()
            .onFailure { return Result.failure(it) }

        return Result.success(repository.accountsCount())
    }
}
