package org.dhis2.mobile.login.pin.domain.usecase

import org.dhis2.mobile.commons.domain.UseCase
import org.dhis2.mobile.login.pin.data.SessionRepository

class DeletePin(
    private val sessionRepository: SessionRepository,
) : UseCase<Unit, Unit> {
    override suspend fun invoke(input: Unit): Result<Unit> =
        try {
            sessionRepository.deletePin()
            sessionRepository.setSessionLocked(false)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
}
