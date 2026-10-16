package org.dhis2.tracker.search.model

import org.dhis2.mobile.commons.error.DomainError

/**
 * A single row emitted by the search paging stream. A page can partially fail (some rows load
 * fine, others don't), so failures are carried per item instead of failing the whole page.
 */
sealed interface TrackedEntitySearchPagingItem {
    data class Item(
        val result: TrackedEntitySearchItemResult,
    ) : TrackedEntitySearchPagingItem

    data class Error(
        val error: DomainError,
    ) : TrackedEntitySearchPagingItem
}
