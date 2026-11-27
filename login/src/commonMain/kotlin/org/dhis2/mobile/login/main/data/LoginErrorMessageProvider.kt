package org.dhis2.mobile.login.main.data

import org.dhis2.mobile.login.resources.Res
import org.dhis2.mobile.login.resources.biometrics_permission_already_saved
import org.dhis2.mobile.login.resources.biometrics_permission_cancelled
import org.dhis2.mobile.login.resources.oauth_login_url_error
import org.jetbrains.compose.resources.getString

class LoginErrorMessageProvider {
    suspend fun oauthUrlError(): String = getString(Res.string.oauth_login_url_error)

    suspend fun biometricsAlreadySaved(): String = getString(Res.string.biometrics_permission_already_saved)

    suspend fun biometricsCancelled(): String = getString(Res.string.biometrics_permission_cancelled)
}
