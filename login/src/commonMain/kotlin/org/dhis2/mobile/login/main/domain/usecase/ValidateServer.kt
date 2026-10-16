package org.dhis2.mobile.login.main.domain.usecase

import org.dhis2.mobile.login.main.data.LoginRepository
import org.dhis2.mobile.login.main.domain.model.ServerValidationResult

class ValidateServer(
    private val repository: LoginRepository,
) {
    suspend operator fun invoke(
        serverUrl: String,
        isNetworkAvailable: Boolean,
    ): ServerValidationResult {
        val urlWithScheme =
            if (serverUrl.startsWith("https://") || serverUrl.startsWith("http://")) {
                serverUrl
            } else {
                "https://$serverUrl"
            }
        return repository.validateServer(urlWithScheme, isNetworkAvailable)
    }
}
