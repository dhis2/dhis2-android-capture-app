package org.dhis2.mobile.login.main.ui.provider

import org.dhis2.mobile.login.resources.Res
import org.dhis2.mobile.login.resources.openid_error_not_configured
import org.dhis2.mobile.login.resources.pin_error_locked_countdown
import org.dhis2.mobile.login.resources.pin_error_remaining_attempts
import org.jetbrains.compose.resources.getString

class CredentialsResourceProvider {
    suspend fun getLoginErrorWithAttempts(
        message: String?,
        attemptsLeft: Int,
    ): String = "$message. ${getString(Res.string.pin_error_remaining_attempts, attemptsLeft)}"

    suspend fun getLockoutCountdownMessage(remainingSeconds: Int): String =
        getString(Res.string.pin_error_locked_countdown, remainingSeconds)

    suspend fun getMissingOidcConfigMessage(): String = getString(Res.string.openid_error_not_configured)
}
