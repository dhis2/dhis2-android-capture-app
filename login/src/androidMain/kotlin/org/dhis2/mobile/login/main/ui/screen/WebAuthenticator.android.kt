package org.dhis2.mobile.login.main.ui.screen

import androidx.compose.runtime.Composable
import androidx.core.net.toUri
import org.dhis2.mobile.commons.navigation.CustomTabLauncher

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

    CustomTabLauncher(oauthAuthUrl.toUri(), onDismiss)
}
