package org.dhis2.tracker.search.model

data class QueryData(
    val attributeId: String,
    val values: List<String>?,
    val searchOperator: SearchOperator?,
)
