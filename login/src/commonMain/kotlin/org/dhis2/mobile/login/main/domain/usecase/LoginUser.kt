package org.dhis2.mobile.login.main.domain.usecase

import org.dhis2.mobile.login.main.data.LoginRepository
import org.dhis2.mobile.login.main.domain.model.LoginResult

class LoginUser(
    repository: LoginRepository,
) : BaseLogin(repository) {
    suspend operator fun invoke(
        serverUrl: String,
        username: String,
        password: String,
        isNetworkAvailable: Boolean,
    ): LoginResult {
        val trimmedUserName = username.trim()
        val result = repository.loginUser(serverUrl, trimmedUserName, password, isNetworkAvailable)
        return handleResult(result, serverUrl, trimmedUserName)
    }
}
