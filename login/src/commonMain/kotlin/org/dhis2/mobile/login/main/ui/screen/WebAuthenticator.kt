package org.dhis2.mobile.login.main.ui.screen

import androidx.compose.runtime.Composable

/**
 * Opens [url] in a browser for one leg of an OAuth flow.
 *
 * @param url the authorization, consent or logout URL to open.
 * @param redirectScheme the scheme the provider redirects back to. Browsers that support Auth Tab
 *   intercept that redirect and return it through [onAuthCallback] instead of firing an intent.
 * @param onAuthCallback invoked with the full redirect URI when the browser reports it directly,
 *   always after [onDismiss]. Browsers without Auth Tab support never call this: their redirect
 *   arrives as an app link, which the framework delivers after the tab's activity result.
 * @param onDismiss invoked when the browser leg ends, whether it redirected or the user backed out.
 *   It closes the leg's destination; the flow continues from the redirect itself.
 */
@Composable
expect fun WebAuthenticator(
    url: String,
    redirectScheme: String,
    onAuthCallback: (String) -> Unit,
    onDismiss: () -> Unit,
)
