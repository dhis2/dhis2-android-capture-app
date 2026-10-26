package org.dhis2.mobile.login.main.ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalUriHandler

// TODO: Receive the OAuth redirect (ASWebAuthenticationSession) instead of only opening the url
@Composable
actual fun WebAuthenticator(
    url: String,
    redirectScheme: String,
    onAuthCallback: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val uriHandler = LocalUriHandler.current
    LaunchedEffect(url) {
        runCatching { uriHandler.openUri(url) }
        onDismiss()
    }
}
