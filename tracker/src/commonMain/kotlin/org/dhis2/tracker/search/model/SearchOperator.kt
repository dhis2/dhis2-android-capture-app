package org.dhis2.tracker.search.model

import androidx.annotation.VisibleForTesting

enum class SearchOperator {
    LIKE,
    SW,
    EW,
    EQ,
}

/**
 * Method use explicitly for testing purposes. Simulating that the search operator
 * has a label assigned.
 */
@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
fun SearchOperator.hasLabel(): Boolean =
    when (this) {
        SearchOperator.EQ,
        SearchOperator.SW,
        SearchOperator.EW,
        -> true
        else -> false
    }
