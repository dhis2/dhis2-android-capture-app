package org.dhis2.mobile.login.main.domain.usecase

import org.dhis2.mobile.commons.domain.UseCase
import org.dhis2.mobile.commons.error.DomainError
import org.dhis2.mobile.login.main.data.LoginRepository

class GetOAuthLogoutUrl(
    private val repository: LoginRepository,
) : UseCase<String, String> {
    override suspend fun invoke(input: String): Result<String> =
        try {
            val builtUrl = repository.buildLogoutUrl(input)
            Result.success(builtUrl)
        } catch (e: DomainError) {
            Result.failure(e)
        }

    companion object {
        private const val REDIRECT_URI = "dhis2oauth://oauth"
    }
}
