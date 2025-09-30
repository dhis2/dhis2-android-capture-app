package org.dhis2.mobile.login.main.domain.usecase

import org.dhis2.mobile.login.main.data.LoginRepository
import org.dhis2.mobile.login.main.domain.model.LoginResult

class LoginUser(
    private val repository: LoginRepository,
) {
    suspend operator fun invoke(
        serverUrl: String,
        username: String,
        password: String,
        isNetworkAvailable: Boolean,
    ): LoginResult {
        val result = repository.loginUser(serverUrl, username, password, isNetworkAvailable)
        return when {
            result.isSuccess -> {
                repository.unlockSession()
                repository.updateAvailableUsers(username)
                LoginResult.Success(
                    displayTrackingMessage = repository.displayTrackingMessage(),
                    initialSyncDone =
                        repository.initialSyncDone(
                            serverUrl,
                            username,
                        ),
                )
            }

            else -> LoginResult.Error(result.exceptionOrNull()?.message)
        }
    }
}
