package org.dhis2.mobile.login.main.ui.navigation

import androidx.navigation.NavOptionsBuilder
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import org.dhis2.mobile.login.main.domain.model.LoginScreenState

interface Navigator {
    val navigationActions: Flow<NavigationAction>

    suspend fun navigate(
        destination: LoginScreenState,
        navOptions: NavOptionsBuilder.() -> Unit = {},
    )

    suspend fun navigateUp()

    suspend fun navigateToHome()

    suspend fun navigateToSync()

    suspend fun navigateToPrivacyPolicy()
}

class DefaultNavigator : Navigator {
    private val _navigationActions = Channel<NavigationAction>()
    override val navigationActions = _navigationActions.receiveAsFlow()

    override suspend fun navigate(
        destination: LoginScreenState,
        navOptions: NavOptionsBuilder.() -> Unit,
    ) {
        _navigationActions.send(
            NavigationAction.Navigate(
                destination = destination,
                navOptions = navOptions,
            ),
        )
    }

    override suspend fun navigateUp() {
        _navigationActions.send(NavigationAction.NavigateUp)
    }

    override suspend fun navigateToHome() {
        _navigationActions.send(NavigationAction.NavigateToHome)
    }

    override suspend fun navigateToSync() {
        _navigationActions.send(NavigationAction.NavigateToSync)
    }

    override suspend fun navigateToPrivacyPolicy() {
        _navigationActions.send(NavigationAction.NavigateToPrivacyPolicy)
    }
}
