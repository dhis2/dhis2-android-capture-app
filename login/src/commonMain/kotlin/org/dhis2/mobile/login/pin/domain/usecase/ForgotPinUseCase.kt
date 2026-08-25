package org.dhis2.mobile.login.pin.domain.usecase

import org.dhis2.mobile.commons.domain.UseCase
import org.dhis2.mobile.commons.domain.resultOf
import org.dhis2.mobile.login.pin.data.SessionRepository

/**
 * Use case for handling the "Forgot PIN" flow.
 * This logs out the user and deletes the stored PIN.
 */
class ForgotPinUseCase(
    private val sessionRepository: SessionRepository,
) : UseCase<Unit, Unit> {
    /**
     * Executes the forgot PIN flow by logging out and clearing PIN data.
     * @return Result indicating success or failure.
     */
    override suspend operator fun invoke(input: Unit): Result<Unit> =
        resultOf {
            sessionRepository.deletePin()
            sessionRepository.logout()
            sessionRepository.setSessionLocked(false)
        }
}
