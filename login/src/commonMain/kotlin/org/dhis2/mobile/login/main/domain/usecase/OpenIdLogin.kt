package org.dhis2.mobile.login.main.domain.usecase

import org.dhis2.mobile.login.main.data.LoginRepository
import org.dhis2.mobile.login.main.domain.model.LoginResult
import org.dhis2.mobile.login.main.domain.model.OpenIdLoginConfiguration

class OpenIdLogin(
    repository: LoginRepository,
) : BaseLogin(repository) {
    suspend operator fun invoke(openIdLoginConfiguration: OpenIdLoginConfiguration): LoginResult {
        val result = repository.loginWithOpenId(openIdLoginConfiguration)
        val username = if (result.isSuccess) repository.getUsername() else ""
        return handleResult(result, openIdLoginConfiguration.serverUrl, username)
    }
}
