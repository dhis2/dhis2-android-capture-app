package org.dhis2.mobile.login.authentication.domain.model

sealed class TwoFAStatus {
    data class Enabled(
        val errorMessage: String? = null,
    ) : TwoFAStatus()

    data class Disabled(
        val secretCode: String,
        val errorMessage: String? = null,
    ) : TwoFAStatus()

    object NoConnection : TwoFAStatus()
}
