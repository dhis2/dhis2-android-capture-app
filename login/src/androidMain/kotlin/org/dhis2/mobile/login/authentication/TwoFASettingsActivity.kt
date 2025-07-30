package org.dhis2.mobile.login.authentication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
