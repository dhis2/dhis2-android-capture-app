package org.dhis2.mobile.login.main.ui.state

data class ServerValidationUiState(
    val currentServer: String = "",
    val error: String? = null,
    val validationRunning: Boolean = false,
)
