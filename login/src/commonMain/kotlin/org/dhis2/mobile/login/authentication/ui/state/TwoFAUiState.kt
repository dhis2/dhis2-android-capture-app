package org.dhis2.mobile.login.authentication.ui.state

sealed class TwoFAUiState {
    object Checking : TwoFAUiState()
    data class Enabled(val errorMessage: String? = null) : TwoFAUiState()
    data class Disabled(val errorMessage: String? = null) : TwoFAUiState()
    object NoConnection : TwoFAUiState()
}
