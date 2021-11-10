package org.dhis2.usescases.searchTrackEntity

data class SearchMessageResult(
    val message: String,
    val canRegister: Boolean,
    val showButton: Boolean,
    val forceSearch: Boolean = true
)
