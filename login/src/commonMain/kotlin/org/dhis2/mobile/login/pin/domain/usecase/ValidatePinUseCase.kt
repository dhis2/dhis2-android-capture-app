package org.dhis2.mobile.login.pin.domain.usecase

import org.dhis2.mobile.login.pin.data.SessionRepository
import org.dhis2.mobile.login.pin.domain.model.PinResult

/**
 * Use case for validating a PIN against the stored PIN.
 * Tracks attempts and enforces a maximum number of failed attempts.
 */
class ValidatePinUseCase(
    private val sessionRepository: SessionRepository,
) {
    companion object {
        private const val MAX_ATTEMPTS = 3
    }

    /**
     * Validates the provided PIN against the stored PIN.
     * @param pin The PIN to validate.
     * @param currentAttempts The current number of failed attempts.
     * @return PinResult indicating the validation outcome.
     */
    suspend operator fun invoke(
        pin: String,
        currentAttempts: Int,
    ): PinResult {
        val storedPin = sessionRepository.getStoredPin()

        if (storedPin == null) {
            return PinResult.NoPinStored
        }

        return if (storedPin == pin) {
            // PIN is correct - unlock session
            sessionRepository.setSessionLocked(false)
            sessionRepository.setPinEnabled(true)
            PinResult.Success
        } else {
            // PIN is incorrect
            val attemptsLeft = MAX_ATTEMPTS - (currentAttempts + 1)
            if (attemptsLeft <= 0) {
                PinResult.TooManyAttempts
            } else {
                PinResult.Failed(attemptsLeft)
            }
        }
    }
}
