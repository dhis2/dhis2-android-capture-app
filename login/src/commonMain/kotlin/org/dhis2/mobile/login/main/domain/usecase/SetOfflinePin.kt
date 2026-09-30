package org.dhis2.mobile.login.main.domain.usecase

import org.dhis2.mobile.login.main.data.LoginRepository

/**
 * Stores the mandatory offline-login PIN for a newly logged-in token-based account, whether it
 * authenticated with OAuth2 or with OpenID Connect. Delegates to [LoginRepository.setOfflinePin],
 * which picks the matching SDK handler and keeps this PIN separate from the session-lock one.
 */
class SetOfflinePin(
    private val repository: LoginRepository,
) {
    suspend operator fun invoke(pin: String): Result<Unit> = repository.setOfflinePin(pin)
}
