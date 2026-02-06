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
    clearCache: Boolean,
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
        val customTabsIntent =
            CustomTabsIntent
                .Builder()
                .setShowTitle(false)
                .setUrlBarHidingEnabled(true)
                .build()
        // Custom Tab is not kept in the activity history stack and it's removed from memory.
        // It won't appear in the "Recent Apps" list
        customTabsIntent.intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)

        // Clear cache if requested (useful for OAuth consent flow)
        if (clearCache) {
            // Ensures the Custom Tab doesn't reuse a cached instance
            customTabsIntent.intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            // Works with CLEAR_TOP to ensure the entire activity stack is cleared
            customTabsIntent.intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val intent =
            customTabsIntent.intent.apply {
                data = url.toUri()
            }

        customTabLauncher.launch(intent)
    }
}
