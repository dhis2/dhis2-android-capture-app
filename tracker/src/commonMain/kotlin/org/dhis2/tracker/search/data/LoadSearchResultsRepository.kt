package org.dhis2.tracker.search.data

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import org.dhis2.tracker.search.model.SearchTrackedEntityAttribute
import org.dhis2.tracker.search.model.SearchTrackerParameterResult

interface LoadSearchResultsRepository {

    suspend fun isTETypeAttribute(teType: String, dataId: String): Boolean
    suspend fun getTEAttribute(dataId: String): SearchTrackedEntityAttribute

    suspend fun addToQuery(
        dataId: String,
        dataValues: List<String>,
        isUnique: Boolean,
        isOptionSet: Boolean,
    )

    suspend fun addFiltersToQuery(program: String?, teType: String)
    suspend fun getResults() : Flow<PagingData<SearchTrackerParameterResult>>
}