package org.dhis2.usescases.main.domain

import org.dhis2.commons.filters.FilterManager
import org.dhis2.mobile.commons.domain.UseCase
import org.dhis2.mobile.commons.error.DomainError
import org.dhis2.usescases.main.data.HomeRepository
import java.io.File

class DeleteAccount(
    private val filterManager: FilterManager,
    private val repository: HomeRepository,
) : UseCase<File?, AccountCount> {
    override suspend fun invoke(input: File?): Result<AccountCount> =
        try {
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
