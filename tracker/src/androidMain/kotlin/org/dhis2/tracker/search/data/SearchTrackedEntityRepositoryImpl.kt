package org.dhis2.tracker.search.data

import androidx.paging.PagingData
import androidx.paging.map
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.dhis2.commons.filters.data.FilterPresenter
import org.dhis2.tracker.search.model.SearchOperator
import org.dhis2.tracker.search.model.SearchTrackedEntityAttribute
import org.dhis2.tracker.search.model.TrackedEntitySearchItemResult
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.trackedentity.search.TrackedEntitySearchCollectionRepository

class SearchTrackedEntityRepositoryImpl(
    private val d2: D2,
    private val filterPresenter: FilterPresenter,
) : SearchTrackedEntityRepository {
    private var trackedEntityInstanceQuery: TrackedEntitySearchCollectionRepository? = null

    // Checks whether the dataId is an attribute of the teType
    override suspend fun isTETypeAttribute(
        teType: String,
        dataId: String,
    ): Boolean =
        d2
            .trackedEntityModule()
            .trackedEntityTypeAttributes()
            .byTrackedEntityTypeUid()
            .eq(teType)
            .byTrackedEntityAttributeUid()
            .eq(dataId)
            .one()
            .blockingExists()

    override suspend fun getTEAttribute(dataId: String): SearchTrackedEntityAttribute {
        val attribute =
            d2
                .trackedEntityModule()
                .trackedEntityAttributes()
                .uid(dataId)
                .blockingGet()
        return SearchTrackedEntityAttribute(
            isUnique = attribute?.unique() == true,
            isOptionSet = (attribute?.optionSet() != null),
        )
    }

    override suspend fun addToQuery(
        dataId: String,
        dataValues: List<String>?,
        searchOperator: SearchOperator?,
    ) {
        trackedEntityInstanceQuery =
            if (!dataValues.isNullOrEmpty() && dataValues.size > 1) {
                // return any tracked entities with attributes that match the values in the list
                trackedEntityInstanceQuery?.byFilter(dataId)?.`in`(dataValues)
            } else {
                dataValues?.let { value ->
                    when (searchOperator) {
                        SearchOperator.LIKE -> {
                            trackedEntityInstanceQuery?.byFilter(dataId)?.like(value[0])
                        }
                        SearchOperator.SW -> {
                            trackedEntityInstanceQuery?.byFilter(dataId)?.sw(value[0])
                        }
                        SearchOperator.EW -> {
                            trackedEntityInstanceQuery?.byFilter(dataId)?.ew(value[0])
                        }
                        SearchOperator.EQ -> {
                            trackedEntityInstanceQuery?.byFilter(dataId)?.eq(value[0])
                        }
                        else -> {
                            trackedEntityInstanceQuery?.byFilter(dataId)?.like(value[0])
                        }
                    }
                }
            }
    }

    override suspend fun addFiltersToQuery(
        program: String?,
        teType: String,
    ) {
        trackedEntityInstanceQuery =
            filterPresenter.filteredTrackedEntityInstances(
                program,
                teType,
            )
    }

    override suspend fun excludeValuesFromQuery(excludeValues: List<String>) {
        trackedEntityInstanceQuery = trackedEntityInstanceQuery?.excludeUids()?.`in`(excludeValues)
    }

    override fun fetchResults(
        isOnline: Boolean,
        hasStateFilters: Boolean,
        allowCache: Boolean,
    ): Flow<PagingData<TrackedEntitySearchItemResult>> {
        // if the device is online and there are no state filters, we can use online cache
        val pagerFlow =
            if (isOnline && !hasStateFilters) {
                trackedEntityInstanceQuery?.allowOnlineCache()?.eq(allowCache)?.offlineFirst()
            } else {
                // otherwise we use offline only
                trackedEntityInstanceQuery?.allowOnlineCache()?.eq(allowCache)?.offlineOnly()
            }

        // map the paging data to TrackedEntitySearchItemResult
        return pagerFlow?.getPagingData(10)?.map { pagingData ->
            pagingData.map { item ->
                item.toTrackedEntitySearchItemResult()
            }
        } ?: throw IllegalStateException("TrackedEntityInstanceQuery is not initialized")
    }

    override suspend fun fetchImmediateResults(
        isOnline: Boolean,
        hasStateFilters: Boolean,
    ): List<TrackedEntitySearchItemResult> {
        // if the device is online and there are no state filters, we can use online cache
        val results =
            if (isOnline && !hasStateFilters) {
                trackedEntityInstanceQuery?.offlineFirst()?.blockingGet()
            } else {
                // otherwise we use offline only
                trackedEntityInstanceQuery?.offlineOnly()?.blockingGet()
            }

        return results?.map { item ->
            item.toTrackedEntitySearchItemResult()
        } ?: throw IllegalStateException("TrackedEntityInstanceQuery is not initialized")
    }
}
