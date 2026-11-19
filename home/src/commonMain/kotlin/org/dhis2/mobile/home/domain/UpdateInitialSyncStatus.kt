package org.dhis2.mobile.home.domain

import org.dhis2.mobile.commons.domain.UseCase
import org.dhis2.mobile.commons.error.DomainError
import org.dhis2.mobile.home.data.HomeRepository

class UpdateInitialSyncStatus(
    val repository: HomeRepository,
) : UseCase<Unit, Unit> {
    override suspend fun invoke(input: Unit): Result<Unit> =
        try {
            repository.setInitialSyncDone()
            Result.success(Unit)
        } catch (domainError: DomainError) {
            Result.failure(domainError)
        }
}
