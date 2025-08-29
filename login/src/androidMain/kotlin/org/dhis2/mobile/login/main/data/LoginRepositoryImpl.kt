package org.dhis2.mobile.login.main.data

import org.dhis2.mobile.login.main.domain.model.ServerValidationResult
import org.dhis2.mobile.login.resources.Res
import org.dhis2.mobile.login.resources.server_url_error
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.helpers.Result
import org.jetbrains.compose.resources.getString

class LoginRepositoryImpl(
    private val d2: D2,
) : LoginRepository {
    override suspend fun validateServer(server: String): ServerValidationResult =
        when (val result = d2.serverModule().blockingCheckServerUrl(server)) {
            is Result.Success -> {
                if (result.value.isOauthEnabled()) {
                    ServerValidationResult.Oauth
                } else {
                    ServerValidationResult.Legacy
                }
            }

            is Result.Failure ->
                ServerValidationResult.Error(getString(Res.string.server_url_error))
        }
}
