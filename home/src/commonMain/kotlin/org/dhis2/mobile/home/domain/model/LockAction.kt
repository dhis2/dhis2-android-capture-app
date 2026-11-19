package org.dhis2.mobile.home.domain.model

sealed interface LockAction {
    data object CreatePin : LockAction

    data object PinAlreadyCreated : LockAction
}
