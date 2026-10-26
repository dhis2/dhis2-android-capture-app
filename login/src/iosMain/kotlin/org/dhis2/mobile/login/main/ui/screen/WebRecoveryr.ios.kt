package org.dhis2.mobile.login.main.ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalUriHandler

@Composable
actual fun WebRecovery(
    url: String,
    onDismiss: () -> Unit,
) {
    val uriHandler = LocalUriHandler.current
    LaunchedEffect(url) {
        runCatching { uriHandler.openUri(url) }
        onDismiss()
    }
}
