package org.dhis2.mobile.login.main.domain.usecase

import org.dhis2.mobile.commons.domain.UseCase
import org.dhis2.mobile.commons.error.DomainError
import org.dhis2.mobile.login.main.data.LoginRepository

class GetOAuthLogoutUrl(
    private val repository: LoginRepository,
) : UseCase<String, String> {
    override suspend fun invoke(input: String): Result<String> =
        try {
            // TODO: replace with a DHIS2 SDK-provided logout URL once available
            //  (e.g. d2.userModule().oauth2Handler().buildLogoutUrl(serverUrl)),
            //  routed through LoginRepository like getDeviceEnrollmentUrl.
            Result.success("$input/dhis-web-commons-security/logout.action?redirect_uri=$REDIRECT_URI")
        } catch (e: DomainError) {
            Result.failure(e)
        }

    companion object {
        private const val REDIRECT_URI = "dhis2oauth://oauth"
    }
}
