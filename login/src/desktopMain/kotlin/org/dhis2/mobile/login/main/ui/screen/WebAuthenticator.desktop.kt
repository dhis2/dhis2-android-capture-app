package org.dhis2.mobile.login.main.ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import java.awt.Desktop
import java.net.URI

@Composable
actual fun WebAuthenticator(
    url: String,
    redirectScheme: String,
    onAuthCallback: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    LaunchedEffect(url) {
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().browse(URI(url))
                onDismiss()
            } catch (_: Exception) {
                // Handle case where a browser is not available or other error
                onDismiss()
            }
        } else {
            // Desktop not supported
            onDismiss()
        }
    }
}
