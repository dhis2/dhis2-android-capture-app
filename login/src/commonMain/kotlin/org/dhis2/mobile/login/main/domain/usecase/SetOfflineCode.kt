package org.dhis2.mobile.login.main.domain.usecase

import org.dhis2.mobile.commons.domain.UseCase
import org.dhis2.mobile.login.main.data.LoginRepository

/**
 * Stores the mandatory offline-login CODE for a newly logged-in token-based account, whether it
 * authenticated with OAuth2 or with OpenID Connect. Delegates to [LoginRepository.setOfflineCode],
 * which picks the matching SDK handler and keeps this CODE separate from the session-lock one.
 */
class SetOfflineCode(
    private val repository: LoginRepository,
) : UseCase<String, Unit> {
    override suspend operator fun invoke(input: String): Result<Unit> = repository.setOfflineCode(input)
}
