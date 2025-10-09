package org.dhis2.mobile.login.main.ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import java.awt.Desktop
import java.net.URI

@Composable
actual fun WebRecovery(
    url: String,
    onDismiss: () -> Unit,
) {
    LaunchedEffect(url) {
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().browse(URI(url))
                // There is no direct way to know when the browser is dismissed on desktop,
                // so we call onDismiss immediately.
                // If you need to wait for a callback, a local server approach would be needed.
                onDismiss()
            } catch (e: Exception) {
                // Handle case where a browser is not available or other error
                onDismiss()
            }
        } else {
            // Desktop not supported
            onDismiss()
        }
    }
}
