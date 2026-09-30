package org.dhis2.mobile.login.main.ui.screen

import androidx.compose.runtime.Composable

/**
 * Opens the account recovery page at [url] in the browser.
 */
@Composable
expect fun WebRecovery(
    url: String,
    onDismiss: () -> Unit,
)
