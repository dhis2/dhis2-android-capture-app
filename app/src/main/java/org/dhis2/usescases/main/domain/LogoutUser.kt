package org.dhis2.usescases.main.domain

import org.dhis2.commons.filters.FilterManager
import org.dhis2.mobile.commons.domain.UseCase
import org.dhis2.mobile.sync.data.SyncBackgroundJobAction
import org.dhis2.mobile.sync.domain.SyncStatusController
import org.dhis2.usescases.main.data.HomeRepository
import org.dhis2.usescases.main.domain.model.LogoutAction
import org.hisp.dhis.android.core.common.AuthorizationType

typealias AccountCount = Int

class LogoutUser(
    private val repository: HomeRepository,
    private val syncBackgroundJobAction: SyncBackgroundJobAction,
    private val syncStatusController: SyncStatusController,
    private val filterManager: FilterManager,
) : UseCase<Unit, LogoutAction> {
    override suspend operator fun invoke(input: Unit): Result<LogoutAction> {
        if (repository.accountType() == AuthorizationType.OAUTH2 && !repository.isPinStored()) {
            return Result.success(LogoutAction.CreatePin)
        } else {
            syncBackgroundJobAction.cancelAll()
            syncStatusController.restore()
            filterManager.clearAllFilters()

            repository
                .clearPin()
                .onFailure { return Result.failure(it) }

            repository
                .logOut()
                .onFailure { return Result.failure(it) }

            return Result.success(LogoutAction.SuccessLogout(repository.accountsCount()))
        }
    }
}
