package org.dhis2.mobile.login.authentication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import org.dhis2.mobile.login.authentication.ui.screen.TwoFADisableScreen
import org.dhis2.mobile.login.authentication.ui.screen.TwoFANoConnectionScreen
import org.dhis2.mobile.login.authentication.ui.screen.TwoFASettingsScreen
import org.hisp.dhis.mobile.ui.designsystem.theme.DHIS2Theme

class TwoFASettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DHIS2Theme {
                TwoFASettingsScreen(
                    onBackClick = { finish() },
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TwoFANoConnectionScreenPreview() {
    DHIS2Theme {
        TwoFADisableScreen {}
    }
}
