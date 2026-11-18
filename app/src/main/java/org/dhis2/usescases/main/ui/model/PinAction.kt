package org.dhis2.usescases.main.ui.model

sealed interface PinAction {
    data object SetPin : PinAction

    data object AskPin : PinAction

    data object ManagePin : PinAction
}
