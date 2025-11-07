package org.dhis2.mobile.login.main.ui.state

import kotlinx.serialization.Serializable

data class CredentialsUiState(
    val serverInfo: ServerInfo,
    val credentialsInfo: CredentialsInfo,
    val loginState: LoginState,
    val allowRecovery: Boolean,
    val canUseBiometrics: Boolean,
    val oidcInfo: OidcInfo?,
    val errorMessage: String?,
    val afterLoginActions: List<AfterLoginAction>,
    val displayBiometricsDialog: Boolean,
    val hasOtherAccounts: Boolean,
    val isSessionLocked: Boolean,
) {
    fun username() = serverInfo.username ?: credentialsInfo.username
}

data class ServerInfo(
    val serverName: String?,
    val serverUrl: String,
    val username: String?,
)

data class CredentialsInfo(
    val username: String,
    val password: String,
    val availableUsernames: List<String>,
    val usernameCanBeEdited: Boolean,
)

@Serializable
sealed class OidcInfo(
    val serverUrl: String?,
    val buttonText: String?,
    val oidcClientId: String,
    val oidcRedirectUri: String,
) {
    @Serializable
    data class Discovery(
        val server: String,
        val loginButtonText: String?,
        val clientId: String,
        val redirectUri: String,
        val discoveryUri: String,
    ) : OidcInfo(server, loginButtonText, clientId, redirectUri)

    @Serializable
    data class Token(
        val server: String,
        val loginLabel: String?,
        val clientId: String,
        val redirectUri: String,
        val authorizationUrl: String,
        val tokenUrl: String,
    ) : OidcInfo(server, loginLabel, clientId, redirectUri)

    fun discoveryUri() =
        when (this) {
            is Discovery -> discoveryUri
            is Token -> null
        }

    fun authorizationUri() =
        when (this) {
            is Discovery -> null
            is Token -> authorizationUrl
        }

    fun tokenUrl() =
        when (this) {
            is Discovery -> null
            is Token -> tokenUrl
        }
}

sealed interface AfterLoginAction {
    data object DisplayTrackingMessage : AfterLoginAction

    data object DisplayBiometricsMessage : AfterLoginAction

    data class NavigateToNextScreen(
        val initialSyncDone: Boolean,
    ) : AfterLoginAction
}

sealed class LoginState {
    data object Disabled : LoginState()

    data object Enabled : LoginState()

    data object Running : LoginState()
}
