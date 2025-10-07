package org.dhis2.mobile.login.main.ui.state

sealed class DatabaseImportState {
    object OnSuccess : DatabaseImportState()

    data class OnFailure(
        val message: String,
    ) : DatabaseImportState()
}
