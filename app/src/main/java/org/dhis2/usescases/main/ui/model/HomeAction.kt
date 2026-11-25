package org.dhis2.usescases.main.ui.model

import org.dhis2.usescases.main.MainScreenType

/**
 * Actions sent to the ViewModel from the UI.
 */
sealed interface HomeAction {
    data object BackPressed : HomeAction

    data object SyncClicked : HomeAction

    data object FilterClicked : HomeAction

    data class ScreenChanged(
        val screen: MainScreenType,
    ) : HomeAction

    data object PinSet : HomeAction

    data object DeletePin : HomeAction

    data object ForgotPin : HomeAction

    /** Navigation menu option that goes back to the last home tab the user was on. */
    data object GoToHome : HomeAction

    /** Navigation menu option that locks the session, asking to create a PIN if there is none. */
    data object BlockSession : HomeAction

    data object LogOut : HomeAction

    /** Navigation menu option that removes the account; it asks for confirmation first. */
    data object DeleteAccount : HomeAction

    data object OpenDeveloperOptions : HomeAction
}
