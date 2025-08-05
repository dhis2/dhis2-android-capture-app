package org.dhis2.mobile.login.authentication.ui.state

sealed class TwoFAUiState {
    object Checking : TwoFAUiState()
    data class Enable(val errorMessage: String? = null) : TwoFAUiState()
    data class Disable(val errorMessage: String? = null) : TwoFAUiState()
    object NoConnection : TwoFAUiState()
}
