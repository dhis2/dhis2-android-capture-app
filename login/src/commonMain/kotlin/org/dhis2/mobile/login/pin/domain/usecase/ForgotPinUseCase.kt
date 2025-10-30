package org.dhis2.mobile.login.pin.domain.usecase

import org.dhis2.mobile.login.pin.data.SessionRepository

/**
 * Use case for handling the "Forgot PIN" flow.
 * This logs out the user and deletes the stored PIN.
 */
class ForgotPinUseCase(
    private val sessionRepository: SessionRepository,
) {
    /**
     * Executes the forgot PIN flow by logging out and clearing PIN data.
     * @return Result indicating success or failure.
     */
    suspend operator fun invoke(): Result<Unit> =
        try {
            sessionRepository.deletePin()
            sessionRepository.logout()
            sessionRepository.setSessionLocked(false)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
}
