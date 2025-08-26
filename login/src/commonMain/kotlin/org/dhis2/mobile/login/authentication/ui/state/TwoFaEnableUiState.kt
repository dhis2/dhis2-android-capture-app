package org.dhis2.mobile.login.authentication.ui.state

sealed class TwoFaEnableUiState {
    object Starting : TwoFaEnableUiState()
    object Success : TwoFaEnableUiState()
    object Failure : TwoFaEnableUiState()
    object Enabling : TwoFaEnableUiState()
}
