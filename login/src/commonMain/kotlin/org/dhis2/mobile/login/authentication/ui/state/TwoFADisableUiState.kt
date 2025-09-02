package org.dhis2.mobile.login.authentication.ui.state

sealed class TwoFADisableUiState {
    object Starting : TwoFADisableUiState()

    object Failure : TwoFADisableUiState()

    object Disabling : TwoFADisableUiState()
}
