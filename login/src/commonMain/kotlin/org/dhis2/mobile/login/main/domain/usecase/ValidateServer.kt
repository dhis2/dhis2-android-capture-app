package org.dhis2.mobile.login.main.domain.usecase

import org.dhis2.mobile.login.main.data.LoginRepository

class ValidateServer(
    private val repository: LoginRepository,
) {
    suspend operator fun invoke(
        serverUrl: String,
        isNetworkAvailable: Boolean,
    ) = repository.validateServer(serverUrl, isNetworkAvailable)
}
