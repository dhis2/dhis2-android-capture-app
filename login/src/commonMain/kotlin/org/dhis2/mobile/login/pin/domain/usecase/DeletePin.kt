package org.dhis2.mobile.login.pin.domain.usecase

import kotlinx.coroutines.CancellationException
import org.dhis2.mobile.commons.domain.UseCase
import org.dhis2.mobile.login.pin.data.SessionRepository

/**
 * Removes the stored PIN of the current account and unlocks the session, so the app no longer
 * asks for a PIN. Unlike [ForgotPinUseCase], the user stays logged in.
 */
class DeletePin(
    private val sessionRepository: SessionRepository,
) : UseCase<Unit, Unit> {
    /**
     * Deletes the PIN and unlocks the session.
     * @return success, or the failure that stopped it. Cancellation is rethrown.
     */
    override suspend fun invoke(input: Unit): Result<Unit> =
        try {
            sessionRepository.deletePin()
            sessionRepository.setSessionLocked(false)
            Result.success(Unit)
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (e: Exception) {
            Result.failure(e)
        }
}
