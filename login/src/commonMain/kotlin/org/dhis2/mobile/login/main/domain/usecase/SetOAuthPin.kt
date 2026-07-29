package org.dhis2.mobile.login.main.domain.usecase

import org.dhis2.mobile.login.main.data.LoginRepository

/**
 * Stores the mandatory offline-login PIN for a newly logged-in OAuth2 account.
 * Delegates to [LoginRepository.setOfflinePin], which the SDK keeps separate from the
 * regular session-lock PIN.
 */
class SetOAuthPin(
    private val repository: LoginRepository,
) {
    suspend operator fun invoke(pin: String): Result<Unit> = repository.setOfflinePin(pin)
}
