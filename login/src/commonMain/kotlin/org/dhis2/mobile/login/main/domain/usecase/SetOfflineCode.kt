package org.dhis2.mobile.login.main.domain.usecase

import org.dhis2.mobile.login.main.data.LoginRepository

/**
 * Stores the mandatory offline-login CODE for a newly logged-in token-based account, whether it
 * authenticated with OAuth2 or with OpenID Connect. Delegates to [LoginRepository.setOfflineCode],
 * which picks the matching SDK handler and keeps this CODE separate from the session-lock one.
 */
class SetOfflineCode(
    private val repository: LoginRepository,
) {
    suspend operator fun invoke(code: String): Result<Unit> = repository.setOfflineCode(code)
}
