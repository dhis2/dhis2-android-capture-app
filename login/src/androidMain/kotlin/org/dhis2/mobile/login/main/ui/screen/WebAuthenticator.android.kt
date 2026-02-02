package org.dhis2.mobile.login.main.ui.screen

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.core.net.toUri

@Composable
actual fun WebAuthenticator(
    url: String,
    onDismiss: () -> Unit,
) {
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
                data = url.toUri()
            }

        customTabLauncher.launch(intent)
    }
}
