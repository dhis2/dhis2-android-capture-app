package org.dhis2.mobile.login.main.ui.screen

import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.browser.auth.AuthTabIntent
import androidx.browser.customtabs.CustomTabsClient
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri

@Composable
actual fun WebAuthenticator(
    url: String,
    redirectScheme: String,
    onAuthCallback: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val currentOnAuthCallback by rememberUpdatedState(onAuthCallback)
    val currentOnDismiss by rememberUpdatedState(onDismiss)

    // Resolve the browser for the support check and the launch.
    val browserPackage = remember { CustomTabsClient.getPackageName(context, null) }
    val supportsAuthTab =
        remember(browserPackage) {
            browserPackage != null && CustomTabsClient.isAuthTabSupported(context, browserPackage)
        }

    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            currentOnDismiss()

            val redirectUri = result.data?.data
            if (supportsAuthTab && result.resultCode == AuthTabIntent.RESULT_OK && redirectUri != null) {
                currentOnAuthCallback(redirectUri.toString())
            }
        }

    // returning to this destination or rotating the device does not open a second browser.
    var launched by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (launched) return@LaunchedEffect
        launched = true

        val launchSucceeded =
            try {
                if (supportsAuthTab) {
                    val authTabIntent = AuthTabIntent.Builder().build()
                    // AuthTabIntent.launch only sets the data and the redirect scheme, so the
                    // package has to be pinned here to reach the browser we probed above.
                    browserPackage?.let(authTabIntent.intent::setPackage)
                    authTabIntent.launch(launcher, url.toUri(), redirectScheme)
                } else {
                    val intent =
                        CustomTabsIntent
                            .Builder()
                            .build()
                            .intent
                            .apply {
                                browserPackage?.let(::setPackage)
                                data = url.toUri()
                                addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                            }
                    launcher.launch(intent)
                }
                true
            } catch (_: ActivityNotFoundException) {
                // No browser could handle the launch; there is nothing to come back from.
                false
            }

        if (!launchSucceeded) currentOnDismiss()
    }
}
