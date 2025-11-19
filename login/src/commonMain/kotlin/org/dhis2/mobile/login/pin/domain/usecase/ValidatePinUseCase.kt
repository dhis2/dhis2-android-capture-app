package org.dhis2.mobile.login.pin.domain.usecase

import org.dhis2.mobile.commons.domain.UseCase
import org.dhis2.mobile.commons.error.DomainError
import org.dhis2.mobile.login.pin.data.SessionRepository
import org.dhis2.mobile.login.pin.domain.model.PinError
import org.dhis2.mobile.login.pin.domain.model.ValidatePinInput

/**
 * Use case for validating a PIN against the stored PIN.
 * Tracks attempts and enforces a maximum number of failed attempts.
 */
class ValidatePinUseCase(
    private val sessionRepository: SessionRepository,
) : UseCase<ValidatePinInput, Unit> {
    companion object {
        private const val MAX_ATTEMPTS = 3
    }

    /**
     * Validates the provided PIN against the stored PIN.
     * @param input ValidatePinInput containing the PIN to validate and current attempt count.
     * @return PinResult indicating the validation outcome.
     */
    override suspend operator fun invoke(input: ValidatePinInput): Result<Unit> {
        try {
            val storedPin = sessionRepository.getStoredPin()

            if (storedPin == null) {
                return Result.failure(PinError.NoPinStored)
            }

            return if (storedPin == input.pin) {
                Result.success(Unit)
            } else {
                val attemptsLeft = MAX_ATTEMPTS - (input.currentAttempts + 1)
                if (attemptsLeft <= 0) {
                    Result.failure(PinError.TooManyAttempts)
                } else {
                    Result.failure(PinError.Failed(attemptsLeft))
                }
            }
        } catch (e: DomainError) {
            return Result.failure(e)
        }
    }
}
