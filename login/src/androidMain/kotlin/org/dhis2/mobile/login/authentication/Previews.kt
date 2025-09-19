package org.dhis2.mobile.login.authentication

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import org.dhis2.mobile.login.authentication.ui.screen.TwoFAToEnableScreen
import org.dhis2.mobile.login.authentication.ui.state.TwoFAUiState
import org.hisp.dhis.mobile.ui.designsystem.theme.DHIS2Theme

@Preview(showBackground = true)
@Composable
fun TwoFAToEnableScreenPreview() {
    DHIS2Theme {
        TwoFAToEnableScreen(
            enableUiState = TwoFAUiState.Enable("SECRETCODE", isEnabling = false),
            onAuthenticatorButtonClicked = {},
            onCopyCodeButtonClicked = {},
            onEnableButtonClicked = {},
        )
    }
}
