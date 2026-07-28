package org.dhis2.mobile.login.pin.domain.usecase

import org.dhis2.mobile.login.pin.data.SessionRepository

/**
 * Use case for saving a new PIN.
 * This is typically used when setting up PIN protection for the first time.
 */
class SavePinUseCase(
    private val sessionRepository: SessionRepository,
) {
    /**
     * Saves the provided PIN and configures session settings.
     * @param pin The PIN to save.
     * @return Result indicating success or failure.
     */
    suspend operator fun invoke(pin: String): Result<Unit> =
        try {
            sessionRepository.savePin(pin)
            sessionRepository.setSessionLocked(true)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
}
