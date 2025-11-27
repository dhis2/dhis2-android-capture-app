package org.dhis2.mobile.login.main.domain.usecase

import org.dhis2.mobile.login.main.data.LoginRepository
import org.dhis2.mobile.login.main.domain.model.LoginResult

class OpenIdLogin(
    repository: LoginRepository,
) : BaseLogin(repository) {
    suspend operator fun invoke(
        serverUrl: String,
        isNetworkAvailable: Boolean,
        clientId: String,
        redirectUri: String,
        discoveryUri: String?,
        authorizationUri: String?,
        tokenUrl: String?,
    ): LoginResult {
        val result =
            repository.loginWithOpenId(
                serverUrl = serverUrl,
                isNetworkAvailable = isNetworkAvailable,
                clientId = clientId,
                redirectUri = redirectUri,
                discoveryUri = discoveryUri,
                authorizationUri = authorizationUri,
                tokenUrl = tokenUrl,
            )
        val username = if (result.isSuccess) repository.getUsername() else ""
        return handleResult(result, serverUrl, username)
    }
}
