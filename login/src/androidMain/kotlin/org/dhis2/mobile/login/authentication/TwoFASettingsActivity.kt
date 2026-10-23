package org.dhis2.mobile.login.authentication

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.net.toUri
import org.dhis2.mobile.login.authentication.ui.screen.TwoFANoConnectionScreen
import org.dhis2.mobile.login.authentication.ui.screen.TwoFASettingsScreen
import org.dhis2.mobile.login.authentication.ui.viewmodel.TwoFASettingsViewModel
import org.hisp.dhis.mobile.ui.designsystem.theme.DHIS2Theme
import org.koin.androidx.viewmodel.ext.android.viewModel

class TwoFASettingsActivity : ComponentActivity() {
    private val viewModel: TwoFASettingsViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DHIS2Theme {
                TwoFASettingsScreen(
                    onBackClick = { finish() },
                    onOpenStore = {
                        openPlayStore(this, "com.google.android.apps.authenticator2")
                    },
                    viewModel = viewModel,
                )
            }
        }
    }

    override fun finish() {
        viewModel.onLeaveScreen()
        super.finish()
    }
}

@Preview(showBackground = true)
@Composable
private fun TwoFANoConnectionScreenPreview() {
    DHIS2Theme {
        TwoFANoConnectionScreen()
    }
}

fun openPlayStore(
    context: Context,
    packageName: String,
) {
    try {
        val intent =
            Intent(Intent.ACTION_VIEW).apply {
                data = "market://details?id=$packageName".toUri()
                setPackage("com.android.vending")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        // Fallback for when Play Store app is not installed
        // play.google.com/store/apps/details?id=com.google.android.apps.authenticator2
        val intent =
            Intent(Intent.ACTION_VIEW).apply {
                data = "https://play.google.com/store/apps/details?id=$packageName".toUri()
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        context.startActivity(intent)
    }
}
