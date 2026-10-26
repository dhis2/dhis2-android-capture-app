package org.dhis2.mobile.app.splash.domain.model

/**
 * Screen the app opens on once the splash has checked the session.
 */
sealed interface StartDestination {
    data object Login : StartDestination

    data object Home : StartDestination
}
