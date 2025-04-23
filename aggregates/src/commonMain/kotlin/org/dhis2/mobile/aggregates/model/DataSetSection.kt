package org.dhis2.mobile.aggregates.model

internal data class DataSetSection(
    val uid: String,
    val title: String,
    val topContent: String? = null,
    val bottomContent: String? = null,
    val misconfiguredRows: List<String>,
)
