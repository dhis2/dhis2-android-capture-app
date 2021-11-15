package org.dhis2.usescases.searchTrackEntity

data class SearchMessageResult(
    val message: String = "",
    val canRegister: Boolean = false,
    val showButton: Boolean = false,
    val forceSearch: Boolean = true
)
