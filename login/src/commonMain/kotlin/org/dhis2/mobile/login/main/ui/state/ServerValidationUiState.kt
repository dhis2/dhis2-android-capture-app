package org.dhis2.mobile.login.main.ui.state

data class ServerValidationUiState(
    val currentServer: String = "https://dev.im.dhis2.org/android5",
    val error: String? = null,
    val validationRunning: Boolean = false,
)
