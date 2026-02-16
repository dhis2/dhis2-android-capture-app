package org.dhis2.tracker.search.model

data class FetchSearchParametersData(
    val teiTypeUid: String,
    val programUid: String? = null,
)
