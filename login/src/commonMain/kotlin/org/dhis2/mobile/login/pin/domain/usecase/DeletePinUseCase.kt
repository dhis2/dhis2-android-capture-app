package org.dhis2.mobile.login.pin.domain.usecase

import org.dhis2.mobile.login.pin.data.SessionRepository

class DeletePinUseCase(
    private val sessionRepository: SessionRepository,
) {
    suspend operator fun invoke(): Result<Unit> =
        try {
            sessionRepository.deletePin()
            sessionRepository.setSessionLocked(false)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
}
