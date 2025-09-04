package org.dhis2.mobile.login.main.data

import org.dhis2.mobile.commons.resources.D2ErrorMessageProvider
import org.dhis2.mobile.login.main.domain.model.ServerValidationResult
import org.dhis2.mobile.login.resources.Res
import org.dhis2.mobile.login.resources.server_url_error
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.helpers.Result
import org.jetbrains.compose.resources.getString

class LoginRepositoryImpl(
    private val d2: D2,
    private val d2ErrorMessageProvider: D2ErrorMessageProvider,
) : LoginRepository {
    override suspend fun validateServer(
        server: String,
        isNetworkAvailable: Boolean,
    ): ServerValidationResult {
        return when (val result = d2.serverModule().blockingCheckServerUrl(server)) {
            is Result.Success -> {
                if (result.value.isOauthEnabled()) {
                    ServerValidationResult.Oauth
                } else {
                    ServerValidationResult.Legacy
                }
            }

            is Result.Failure -> {
                val error = d2ErrorMessageProvider.getErrorMessage(
                    throwable = result.failure,
                    isNetworkAvailable = isNetworkAvailable,
                )
                ServerValidationResult.Error(error ?: getString(Res.string.server_url_error))
            }
        }
}
