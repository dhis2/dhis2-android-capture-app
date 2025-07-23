package org.dhis2.mobile.login

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import org.dhis2.mobile.login.authentication.ui.StatusCheckingSection
import org.dhis2.mobile.login.authentication.ui.TwoFASettingsScreen

@Preview
@Composable
private fun TwoFASettingsScreenPreview() {
    TwoFASettingsScreen(
        onBackClick = {},
        isCheckingStatus = true
    )
}

@Preview
@Composable
private fun StatusCheckingSectionPreview() {
    StatusCheckingSection()
}
