package org.dhis2.usescases.main.domain

import org.dhis2.mobile.commons.domain.UseCase
import org.dhis2.mobile.commons.error.DomainError
import org.dhis2.usescases.main.data.HomeRepository
import org.dhis2.usescases.main.domain.model.LockAction

class GetLockAction(
    private val repository: HomeRepository,
) : UseCase<Unit, LockAction> {
    override suspend fun invoke(input: Unit): Result<LockAction> =
        try {
            val isPinSet = repository.isPinStored()
            if (isPinSet) {
                Result.success(LockAction.BlockSession)
            } else {
                Result.success(LockAction.CreatePin)
            }
        } catch (domainError: DomainError) {
            Result.failure(domainError)
        }
}
