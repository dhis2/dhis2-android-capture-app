package org.dhis2.mobile.login.main.domain.usecase

import org.dhis2.mobile.login.main.data.LoginRepository
import org.dhis2.mobile.login.main.domain.model.LoginResult

class LoginUserWithOAuth(
    repository: LoginRepository,
): BaseLogin(repository) {
    suspend operator fun invoke(
        serverUrl: String,
        code: String
    ): LoginResult {
        val result = repository.loginUserWithOAuth(serverUrl, code)
        when {
            result.isSuccess -> {
                val username = result.getOrNull()
                return handleResult(Result.success(Unit), serverUrl, username!!)
            }
            else -> {
                return LoginResult.Error(result.exceptionOrNull()?.message)
            }

        }
    }
}