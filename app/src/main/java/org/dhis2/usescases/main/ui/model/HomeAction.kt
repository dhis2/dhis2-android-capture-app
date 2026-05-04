package org.dhis2.usescases.main.ui.model

import org.dhis2.usescases.main.MainScreenType

/**
 * Actions sent to the ViewModel from the UI.
 */
sealed interface HomeAction {
    data object BackPressed : HomeAction
    data object MenuClicked : HomeAction
    data object SyncClicked : HomeAction
    data object FilterClicked : HomeAction
    data class ScreenChanged(val screen: MainScreenType) : HomeAction
    data object PinSet : HomeAction
}
