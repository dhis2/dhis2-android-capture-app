package org.dhis2.mobile.login.main.ui.screen

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.browser.auth.AuthTabIntent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.core.net.toUri

@Composable
actual fun WebAuthenticator(
    url: String,
    onDismiss: () -> Unit,
) {
    val customTabLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult(),
        ) { result ->
            if (result.resultCode == Activity.RESULT_CANCELED) {
                onDismiss()
            }
        }

    LaunchedEffect(url) {
        val authTabIntent =
            AuthTabIntent
                .Builder()
                .setEphemeralBrowsingEnabled(true)
                .build()
        // Custom Tab is not kept in the activity history stack and it's removed from memory.
        // It won't appear in the "Recent Apps" list
        authTabIntent.intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)

        val intent =
            authTabIntent.intent.apply {
                data = url.toUri()
            }

        customTabLauncher.launch(intent)
    }
}
