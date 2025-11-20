package org.dhis2.mobile.login.main.ui.state

internal sealed interface CredentialsUpdate {
    data class Username(
        val username: String,
    ) : CredentialsUpdate

    data class Password(
        val password: String,
    ) : CredentialsUpdate

    data object Complete : CredentialsUpdate
}
