package org.dhis2.mobile.login.pin.ui.components

import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import org.dhis2.mobile.login.pin.domain.model.PinMode
import org.dhis2.mobile.login.pin.ui.state.PinUiState
import org.hisp.dhis.mobile.ui.designsystem.theme.DHIS2Theme

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun PinAskPortraitPreview() {
    val windowSizeClass = WindowSizeClass.calculateFromSize(DpSize(360.dp, 800.dp))
    DHIS2Theme {
        PinContent(
            uiState =
                PinUiState(
                    title = "Enter your PIN",
                    subtitle = "Enter your 4-digit PIN to access your account.",
                    primaryButtonText = "Unlock",
                    secondaryButtonText = "Forgot your PIN?",
                ),
            mode = PinMode.ASK,
            isLandscape = false,
            windowSizeClass = windowSizeClass,
            onPinChanged = {},
            onPrimaryClick = {},
            onSecondaryClick = {},
            onDismiss = {},
        )
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Preview(showBackground = true, widthDp = 1280, heightDp = 800)
@Composable
fun PinAskLandscapePreview() {
    val windowSizeClass = WindowSizeClass.calculateFromSize(DpSize(1280.dp, 800.dp))
    DHIS2Theme {
        PinContent(
            uiState =
                PinUiState(
                    title = "Enter your PIN",
                    subtitle = "Enter your 4-digit PIN to access your account.",
                    primaryButtonText = "Unlock",
                    secondaryButtonText = "Forgot your PIN?",
                ),
            mode = PinMode.ASK,
            isLandscape = true,
            windowSizeClass = windowSizeClass,
            onPinChanged = {},
            onPrimaryClick = {},
            onSecondaryClick = {},
            onDismiss = {},
        )
    }
}
