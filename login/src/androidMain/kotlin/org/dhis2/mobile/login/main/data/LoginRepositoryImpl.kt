package org.dhis2.mobile.login.main.data

import org.dhis2.mobile.login.main.domain.model.ServerValidationResult
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.helpers.Result

class LoginRepositoryImpl(private val d2: D2) : LoginRepository {
    override suspend fun validateServer(server: String): ServerValidationResult {
        return when (val result = d2.serverModule().blockingCheckServerUrl(server)) {
            is Result.Success -> {
                if (result.value.isOauthEnabled()) {
                    ServerValidationResult.Oauth
                } else {
                    ServerValidationResult.Legacy
                }
            }
            is Result.Failure ->
                ServerValidationResult.Error(result.failure.errorDescription())
        }
    }
}
