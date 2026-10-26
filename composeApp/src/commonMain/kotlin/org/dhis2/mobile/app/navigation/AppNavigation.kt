package org.dhis2.mobile.app.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import org.dhis2.mobile.app.home.ui.screen.HomeScreen
import org.dhis2.mobile.app.splash.domain.model.StartDestination
import org.dhis2.mobile.app.splash.ui.screen.SplashScreen
import org.dhis2.mobile.login.main.ui.screen.LoginScreen

@Composable
fun AppNavigation() {
    val backStack = rememberNavBackStack(appRouteSavedStateConfiguration, AppRoute.Splash)

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryDecorators =
            listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
        entryProvider =
            entryProvider {
                entry<AppRoute.Splash> {
                    SplashScreen(
                        onNavigate = { destination -> backStack.replaceAll(destination.toRoute()) },
                    )
                }
                entry<AppRoute.Login> {
                    // TODO: Wire version name, sync, privacy policy and finish once they are migrated
                    LoginScreen(
                        versionName = "",
                        onNavigateToSync = { backStack.replaceAll(AppRoute.Home) },
                        onNavigateToHome = { backStack.replaceAll(AppRoute.Home) },
                        onNavigateToPrivacyPolicy = {},
                        onFinish = {},
                    )
                }
                entry<AppRoute.Home> {
                    HomeScreen()
                }
            },
    )
}

private fun StartDestination.toRoute(): AppRoute =
    when (this) {
        StartDestination.Login -> AppRoute.Login
        StartDestination.Home -> AppRoute.Home
    }

/**
 * Makes [route] the only entry, so back does not return to the screen being left.
 */
private fun NavBackStack<NavKey>.replaceAll(route: AppRoute) {
    add(route)
    retainAll { it == route }
}
