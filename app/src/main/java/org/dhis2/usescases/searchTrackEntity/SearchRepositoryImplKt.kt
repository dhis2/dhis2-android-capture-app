package org.dhis2.usescases.searchTrackEntity

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import org.dhis2.commons.filters.FilterManager
import org.dhis2.data.search.SearchParametersModel
import org.hisp.dhis.android.core.trackedentity.search.TrackedEntitySearchCollectionRepository
import org.hisp.dhis.android.core.trackedentity.search.TrackedEntitySearchItem

class SearchRepositoryImplKt(
    private val searchRepositoryJava: SearchRepository,
) : SearchRepositoryKt {

    private lateinit var savedSearchParamenters: SearchParametersModel

    private lateinit var savedFilters: FilterManager

    private lateinit var trackedEntityInstanceQuery: TrackedEntitySearchCollectionRepository

    private val fetchedTeiUids = HashSet<String>()

    override fun searchTrackedEntities(
        searchParametersModel: SearchParametersModel,
        isOnline: Boolean,
    ): Flow<PagingData<TrackedEntitySearchItem>> {
        var allowCache = false
        savedSearchParamenters = searchParametersModel.copy()
        savedFilters = FilterManager.getInstance().copy()

        if (searchParametersModel != savedSearchParamenters || !FilterManager.getInstance().sameFilters(savedFilters)) {
            trackedEntityInstanceQuery = searchRepositoryJava.getFilteredRepository(searchParametersModel)
        } else {
            trackedEntityInstanceQuery = searchRepositoryJava.getFilteredRepository(searchParametersModel)
            allowCache = true
        }

        if (fetchedTeiUids.isNotEmpty() && searchParametersModel.selectedProgram == null) {
            trackedEntityInstanceQuery =
                trackedEntityInstanceQuery.excludeUids().`in`(fetchedTeiUids.toList())
        }

        val pagerFlow = if (isOnline && FilterManager.getInstance().stateFilters.isNotEmpty()) {
            trackedEntityInstanceQuery.allowOnlineCache().eq(allowCache).offlineFirst().getPagingData(10)
        } else {
            trackedEntityInstanceQuery.allowOnlineCache().eq(allowCache).offlineOnly().getPagingData(10)
        }

        return pagerFlow
    }
}
