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
    // TODO Use the url parameter when the backend supports dynamic redirect URIs
    val redirectUri = "https://vgarciabnz.github.io"
    val oauthAuthUrl =
        "https://dev.im.dhis2.org/oauth2-android-test/oauth2/authorize?" +
            "response_type=code" +
            "&client_id=dhis2-client" +
            "&redirect_uri=$redirectUri" +
            "&scope=openid%20email" +
            "&state=abc123"

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
                data = oauthAuthUrl.toUri()
            }

        customTabLauncher.launch(intent)
    }
}
