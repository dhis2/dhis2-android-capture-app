package org.dhis2.mobile.login.main.ui.screen

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.core.net.toUri

private const val RECOVERY_PATH = "/dhis-web-commons/security/recovery.action"

@Composable
actual fun WebRecovery(
    url: String,
    onDismiss: () -> Unit,
) {
    val recoveryUrl = "$url$RECOVERY_PATH"
    // Custom Tab launcher will handle the result of the Custom Tab to detect if the user closed it
    val customTabLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult(),
        ) { result ->
            if (result.resultCode == Activity.RESULT_CANCELED) {
                onDismiss()
            }
        }

    LaunchedEffect(url) {
        val customTabsIntent =
            CustomTabsIntent
                .Builder()
                .setShowTitle(false)
                .setUrlBarHidingEnabled(true)
                .build()
        customTabsIntent.intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)

        val intent =
            customTabsIntent.intent.apply {
                data = recoveryUrl.toUri()
            }

        customTabLauncher.launch(intent)
    }
}
