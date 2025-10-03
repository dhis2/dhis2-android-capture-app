package org.dhis2.mobile.login.main.ui.screen

import androidx.compose.runtime.Composable
import androidx.core.net.toUri
import org.dhis2.mobile.commons.navigation.CustomTabLauncher

private const val RECOVERY_PATH = "/dhis-web-commons/security/recovery.action"

@Composable
actual fun WebRecovery(
    url: String,
    onDismiss: () -> Unit,
) {
    val recoveryUrl = "$url$RECOVERY_PATH"
    CustomTabLauncher(recoveryUrl.toUri(), onDismiss)
}
