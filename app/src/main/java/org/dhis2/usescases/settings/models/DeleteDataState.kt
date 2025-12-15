package org.dhis2.usescases.settings.models

sealed class DeleteDataState {
    data object None : DeleteDataState()

    data object Opened : DeleteDataState()

    data object Deleting : DeleteDataState()
}
