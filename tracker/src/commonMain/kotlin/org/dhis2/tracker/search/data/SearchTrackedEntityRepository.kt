package org.dhis2.tracker.search.data

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import org.dhis2.tracker.search.model.SearchTrackedEntityAttribute
import org.dhis2.tracker.search.model.TrackedEntitySearchItemResult

interface SearchTrackedEntityRepository {
    suspend fun isTETypeAttribute(
        teType: String,
        dataId: String,
    ): Boolean

    suspend fun getTEAttribute(dataId: String): SearchTrackedEntityAttribute

    suspend fun addToQuery(
        dataId: String,
        dataValues: List<String>,
        isUnique: Boolean,
        isOptionSet: Boolean,
    )

    suspend fun addFiltersToQuery(
        program: String?,
        teType: String,
    )

    suspend fun excludeValuesFromQuery(excludeValues: List<String>)

    suspend fun fetchResults(
        isOnline: Boolean,
        hasStateFilters: Boolean,
        allowCache: Boolean,
    ): Flow<PagingData<TrackedEntitySearchItemResult>>
}
