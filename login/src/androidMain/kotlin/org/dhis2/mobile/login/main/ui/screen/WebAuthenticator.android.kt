package org.dhis2.mobile.login.main.ui.screen

import android.content.Intent
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri

@Composable
actual fun WebAuthenticator(
    url: String,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    LaunchedEffect(url) {
        val customTabsIntent = CustomTabsIntent.Builder()
            .setShowTitle(false)
            .setUrlBarHidingEnabled(true)
            .build()
        customTabsIntent.intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
        try {
            customTabsIntent.launchUrl(context, url.toUri())
        } catch (e: Exception) {
            // Handle case where a browser is not available
            onDismiss()
        }
    }
}
