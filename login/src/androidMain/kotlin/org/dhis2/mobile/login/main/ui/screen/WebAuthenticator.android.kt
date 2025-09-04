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
    val redirectUri = "https://vgarciabnz.github.io"
    val oauthAuthUrl =
        "https://dev.im.dhis2.org/oauth2-android-test/oauth2/authorize?" +
            "response_type=code" +
            "&client_id=dhis2-client" +
            "&redirect_uri=$redirectUri" +
            "&scope=openid%20email" +
            "&state=abc123"

    val context = LocalContext.current
    LaunchedEffect(url) {
        val customTabsIntent = CustomTabsIntent.Builder()
            .setShowTitle(false)
            .setUrlBarHidingEnabled(true)
            .build()
        customTabsIntent.intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
        try {
            customTabsIntent.launchUrl(
                context,
                oauthAuthUrl.toUri(),
            )
        } catch (e: Exception) {
            // Handle case where a browser is not available
            onDismiss()
        }
    }
}
