package org.dhis2.usescases.main.domain.model

sealed interface LockAction {
    data object CreatePin : LockAction

    data object BlockSession : LockAction
}
