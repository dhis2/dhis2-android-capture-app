package org.dhis2.mobile.login.pin.domain.usecase

import org.dhis2.mobile.commons.domain.UseCase
import org.dhis2.mobile.commons.domain.resultOf
import org.dhis2.mobile.login.pin.data.SessionRepository

/**
 * Use case for saving a new PIN.
 * This is typically used when setting up PIN protection for the first time.
 */
class SavePinUseCase(
    private val sessionRepository: SessionRepository,
) : UseCase<String, Unit> {
    /**
     * Saves the provided PIN and configures session settings.
     * @param input The PIN to save.
     * @return Result indicating success or failure.
     */
    override suspend operator fun invoke(input: String): Result<Unit> =
        resultOf {
            sessionRepository.savePin(input)
            sessionRepository.setSessionLocked(true)
        }
}
