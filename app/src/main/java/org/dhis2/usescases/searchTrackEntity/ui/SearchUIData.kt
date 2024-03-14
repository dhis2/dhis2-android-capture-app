package org.dhis2.usescases.searchTrackEntity.ui

sealed class SearchUIData
data class UnableToSearchOutsideData(
    val trackedEntityTypeAttributes: List<String>,
    val trackedEntityTypeName: String,
) : SearchUIData()
