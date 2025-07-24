package org.dhis2.mobile.login.authentication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import org.dhis2.mobile.login.authentication.ui.StatusCheckingSection
import org.dhis2.mobile.login.authentication.ui.TwoFASettingsScreen
import org.hisp.dhis.mobile.ui.designsystem.theme.DHIS2Theme

class TwoFASettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DHIS2Theme {
                TwoFASettingsScreen(
                    onBackClick = { finish() },
                    isCheckingStatus = true,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TwoFASettingsScreenPreview() {
    TwoFASettingsScreen(
        onBackClick = {},
        isCheckingStatus = true,
    )
}

@Preview
@Composable
private fun StatusCheckingSectionPreview() {
    StatusCheckingSection()
}
