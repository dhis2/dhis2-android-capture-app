package org.dhis2.mobile.login.main.domain.usecase

import org.dhis2.mobile.commons.error.DomainError
import org.dhis2.mobile.login.main.data.LoginRepository
import org.dhis2.mobile.login.main.domain.model.LoginResult

class LoginUserOffline(
    repository: LoginRepository,
) : BaseLogin(repository) {
    companion object {
        private const val MAX_ATTEMPTS = 3
        private const val LOCKOUT_SECONDS = 60
    }

    private var pinAttempts = 0

    suspend operator fun invoke(
        serverUrl: String,
        username: String,
        code: String,
    ): LoginResult {
        val result = repository.loginUser(serverUrl, username, code)

        return when {
            result.exceptionOrNull() is DomainError.AuthenticationError -> {
                val attemptsLeft = MAX_ATTEMPTS - (pinAttempts + 1)
                if (attemptsLeft <= 0) {
                    pinAttempts = 0
                    LoginResult.LockOut(LOCKOUT_SECONDS)
                } else {
                    pinAttempts++
                    LoginResult.Error(
                        message = result.exceptionOrNull()?.message,
                        attemptsLeft = attemptsLeft,
                    )
                }
            }

            else -> handleResult(result, serverUrl, username)
        }
    }
}
