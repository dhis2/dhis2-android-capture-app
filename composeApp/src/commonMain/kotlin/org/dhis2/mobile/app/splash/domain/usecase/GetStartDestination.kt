package org.dhis2.mobile.app.splash.domain.usecase

import org.dhis2.mobile.app.splash.domain.model.StartDestination
import org.dhis2.mobile.commons.domain.UseCase
import org.dhis2.mobile.commons.domain.invoke
import org.dhis2.mobile.commons.error.DomainError
import org.dhis2.mobile.login.main.domain.usecase.IsUserLoggedIn
import org.dhis2.mobile.login.main.domain.usecase.VerifyNeedOfflineCode
import org.dhis2.mobile.login.pin.domain.usecase.GetIsSessionLockedUseCase
import kotlin.coroutines.cancellation.CancellationException

/**
 * Decides where the app starts: home for a logged-in user with an open session, login otherwise.
 * Login handles locked sessions and offline-code entry itself.
 */
class GetStartDestination(
    private val isUserLoggedIn: IsUserLoggedIn,
    private val verifyNeedOfflineCode: VerifyNeedOfflineCode,
    private val getIsSessionLocked: GetIsSessionLockedUseCase,
) : UseCase<Unit, StartDestination> {
    override suspend fun invoke(input: Unit): Result<StartDestination> =
        try {
            val destination =
                if (isUserLoggedIn().getOrThrow() && !isSessionLocked()) {
                    StartDestination.Home
                } else {
                    StartDestination.Login
                }
            Result.success(destination)
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (error: DomainError) {
            Result.failure(error)
        } catch (error: Exception) {
            Result.failure(error)
        }

    private suspend fun isSessionLocked(): Boolean = getIsSessionLocked(requireOfflineCredentials = verifyNeedOfflineCode().getOrThrow())
}
