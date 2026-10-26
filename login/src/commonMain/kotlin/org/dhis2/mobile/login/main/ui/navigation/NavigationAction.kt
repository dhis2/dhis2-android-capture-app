package org.dhis2.mobile.login.main.ui.navigation

import org.dhis2.mobile.login.main.domain.model.LoginScreenState

sealed interface NavigationAction {
    data class Navigate(
        val destination: LoginScreenState,
    ) : NavigationAction

    data object NavigateUp : NavigationAction

    data object NavigateToHome : NavigationAction

    data object NavigateToSync : NavigationAction

    data object NavigateToPrivacyPolicy : NavigationAction
}
