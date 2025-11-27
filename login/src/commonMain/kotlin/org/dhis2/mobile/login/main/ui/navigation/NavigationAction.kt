package org.dhis2.mobile.login.main.ui.navigation

import androidx.navigation.NavOptionsBuilder
import org.dhis2.mobile.login.main.domain.model.LoginScreenState

sealed interface NavigationAction {
    data class Navigate(
        val destination: LoginScreenState,
        val navOptions: NavOptionsBuilder.() -> Unit = {},
    ) : NavigationAction

    data object NavigateUp : NavigationAction

    data object NavigateToHome : NavigationAction

    data object NavigateToSync : NavigationAction

    data object NavigateToPrivacyPolicy : NavigationAction
}
