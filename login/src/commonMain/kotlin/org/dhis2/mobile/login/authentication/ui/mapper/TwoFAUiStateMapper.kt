package org.dhis2.mobile.login.authentication.ui.mapper

import org.dhis2.mobile.login.authentication.domain.model.TwoFAStatus
import org.dhis2.mobile.login.authentication.ui.state.TwoFAUiState

class TwoFAUiStateMapper {
    fun mapToUiState(twoFAStatus: TwoFAStatus): TwoFAUiState {
        return when (twoFAStatus) {
            TwoFAStatus.Checking -> TwoFAUiState.Checking
            is TwoFAStatus.Disabled -> TwoFAUiState.Disabled(errorMessage = twoFAStatus.errorMessage)
            is TwoFAStatus.Enabled -> TwoFAUiState.Enabled(errorMessage = twoFAStatus.errorMessage)
            TwoFAStatus.NoConnection -> TwoFAUiState.NoConnection
        }
    }
}
