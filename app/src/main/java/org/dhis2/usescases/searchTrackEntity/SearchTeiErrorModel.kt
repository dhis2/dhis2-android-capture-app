package org.dhis2.usescases.searchTrackEntity

class SearchTeiErrorModel(
    errorMessage: String,
) : SearchTeiModel() {
    init {
        onlineErrorMessage = errorMessage
    }
}
