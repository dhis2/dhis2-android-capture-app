package org.dhis2.mobile.login.main.domain.model

import kotlinx.serialization.Serializable

sealed interface LoginScreenState {
    @Serializable
    data object Loading : LoginScreenState

    @Serializable
    data class ServerValidation(
        val currentServer: String,
        val availableServers: List<String>,
        val error: String? = null,
        val validationRunning: Boolean = false,
        val hasAccounts: Boolean,
    ) : LoginScreenState

    @Serializable
    data class LegacyLogin(
        val selectedServer: String,
        val selectedUsername: String?,
        val serverName: String?,
        val selectedServerFlag: String?,
        val allowRecovery: Boolean,
    ) : LoginScreenState

    @Serializable
    data class OauthLogin(
        val selectedServer: String,
    ) : LoginScreenState

    @Serializable
    data object Accounts : LoginScreenState

    @Serializable
    data class RecoverAccount(
        val selectedServer: String,
    ) : LoginScreenState
}
