package org.dhis2.mobile.login.authentication.ui.state

sealed class TwoFAUiState {
    object Checking : TwoFAUiState()
    data class Enable(
        val secretCode: String,
        val isEnabling: Boolean,
        val enableErrorMessage: String? = null,
        val errorMessage: String? = null,
    ) : TwoFAUiState()

    data class Disable(
        val isDisabling: Boolean,
        val disableErrorMessage: String? = null,
        val errorMessage: String? = null,
    ) : TwoFAUiState()
    object NoConnection : TwoFAUiState()
}
