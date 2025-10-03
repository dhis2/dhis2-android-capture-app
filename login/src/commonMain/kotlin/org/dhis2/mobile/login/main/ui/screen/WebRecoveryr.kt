package org.dhis2.mobile.login.main.ui.screen

import androidx.compose.runtime.Composable

@Composable
expect fun WebRecovery(
    url: String,
    onDismiss: () -> Unit,
)
