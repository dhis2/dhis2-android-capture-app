package org.dhis2.mobile.login.authentication.ui.mapper

import org.dhis2.mobile.login.authentication.domain.model.TwoFAStatus
import org.dhis2.mobile.login.authentication.ui.state.TwoFAUiState
import org.hisp.dhis.mobile.ui.designsystem.component.InputShellState

class TwoFAUiStateMapper {
    fun mapToUiState(twoFAStatus: TwoFAStatus): TwoFAUiState =
        when (twoFAStatus) {
            // When 2FA is disabled in the system, show the Enable screen
            is TwoFAStatus.Disabled ->
                TwoFAUiState.Enable(
                    secretCode = twoFAStatus.secretCode,
                    isEnabling = false,
                    enableErrorMessage = null,
                    errorMessage = twoFAStatus.errorMessage,
                )
            // When 2FA is enabled in the system, show the Disable screen
            is TwoFAStatus.Enabled ->
                TwoFAUiState.Disable(
                    state = InputShellState.UNFOCUSED,
                    isDisabling = false,
                    disableErrorMessage = null,
                    errorMessage = twoFAStatus.errorMessage,
                )
            TwoFAStatus.NoConnection -> TwoFAUiState.NoConnection
        }
}
