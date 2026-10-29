package org.dhis2.mobile.login.pin.domain.usecase

import kotlinx.coroutines.CancellationException
import org.dhis2.mobile.commons.domain.UseCase
import org.dhis2.mobile.commons.error.DomainError
import org.dhis2.mobile.login.pin.data.SessionRepository
import org.dhis2.mobile.login.pin.domain.model.PinError
import org.dhis2.mobile.login.pin.domain.model.PinVerification
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
            return when (sessionRepository.verifyPin(input.pin)) {
                PinVerification.Match -> Result.success(Unit)
                PinVerification.NoPinStored -> Result.failure(PinError.NoPinStored)
                PinVerification.Mismatch -> {
                    val attemptsLeft = MAX_ATTEMPTS - (input.currentAttempts + 1)
                    if (attemptsLeft <= 0) {
                        Result.failure(PinError.TooManyAttempts)
                    } else {
                        Result.failure(PinError.Failed(attemptsLeft))
                    }
                }
            }
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (e: DomainError) {
            return Result.failure(e)
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }
}
