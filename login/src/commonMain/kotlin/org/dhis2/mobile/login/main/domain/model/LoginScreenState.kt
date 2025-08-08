package org.dhis2.mobile.login.main.domain.model

import kotlinx.serialization.Serializable

sealed interface LoginScreenState {

    @Serializable
    data object Loading : LoginScreenState

    @Serializable
    data class ServerValidation(
        val currentServer: String,
    ) : LoginScreenState

    @Serializable
    data class LegacyLogin(
        val selectedServer: String,
        val selectedUsername: String,
    ) : LoginScreenState

    @Serializable
    data object Accounts : LoginScreenState
}
