package org.dhis2.mobile.login.main.domain.usecase

import org.dhis2.mobile.login.main.data.LoginRepository
import org.dhis2.mobile.login.main.domain.model.LoginResult
import org.dhis2.mobile.login.resources.Res
import org.dhis2.mobile.login.resources.missing_username_from_oauth_login_response
import org.jetbrains.compose.resources.getString

class LoginUserWithOAuth(
    repository: LoginRepository,
) : BaseLogin(repository) {
    suspend operator fun invoke(
        serverUrl: String,
        code: String,
    ): LoginResult {
        val result = repository.loginUserWithOAuth(serverUrl, code)
        return when {
            result.isSuccess -> {
                val username = result.getOrNull()
                if (username != null) {
                    handleResult(Result.success(Unit), serverUrl, username)
                } else {
                    LoginResult.Error(getString(Res.string.missing_username_from_oauth_login_response))
                }
            }

            else -> {
                LoginResult.Error(result.exceptionOrNull()?.message)
            }
        }
    }
}
