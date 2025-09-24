package org.dhis2.mobile.login.main.ui.states

data class CredentialsUiState(
    val serverInfo: ServerInfo,
    val credentialsInfo: CredentialsInfo,
    val loginState: LoginState,
    val allowRecovery: Boolean,
    val canUseBiometrics: Boolean,
    val oidcInfo: OidcInfo?,
    val errorMessage: String?,
    val afterLoginActions: List<AfterLoginAction>,
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

data class OidcInfo(
    val oidcIcon: String?,
    val oidcLoginText: String?,
    val oidcUrl: String?,
)

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
