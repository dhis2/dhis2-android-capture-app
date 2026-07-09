package org.dhis2.mobile.login.main.domain.usecase

import org.dhis2.mobile.commons.domain.UseCase
import org.dhis2.mobile.commons.error.DomainError
import org.dhis2.mobile.commons.logging.logDebug
import org.dhis2.mobile.login.main.data.LoginRepository

class GetOAuthLogoutUrl(
    private val repository: LoginRepository,
) : UseCase<String, String> {
    override suspend fun invoke(input: String): Result<String> =
        try {
            val hardcodedUrl = "$input/dhis-web-commons-security/logout.action?redirect_uri=$REDIRECT_URI"
            val builtUrl = repository.buildLogoutUrl(input)
            logDebug("GetOAuthLogoutUrl", "hardcodedUrl: $hardcodedUrl \n builtUrl: $builtUrl")
            Result.success(hardcodedUrl)
        } catch (e: DomainError) {
            Result.failure(e)
        }

    companion object {
        private const val REDIRECT_URI = "dhis2oauth://oauth"
    }
}
