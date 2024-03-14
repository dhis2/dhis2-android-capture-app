package org.dhis2.commons.filters.data

import org.dhis2.commons.filters.FilterManager
import org.dhis2.commons.filters.sorting.FilteredOrgUnitResult
import org.hisp.dhis.android.core.dataset.DataSetInstanceSummaryCollectionRepository
import org.hisp.dhis.android.core.event.search.EventQueryCollectionRepository
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.trackedentity.search.TrackedEntitySearchCollectionRepository
import javax.inject.Inject

const val ORG_UNT_FILTER_MIN_CHAR = 3

class FilterPresenter @Inject constructor(
    private val filterRepository: FilterRepository,
    val filterManager: FilterManager,
) {

    private val dataSetFilterSearchHelper: DataSetFilterSearchHelper by lazy {
        DataSetFilterSearchHelper(filterRepository, filterManager)
    }
    private val trackerFilterSearchHelper: TrackerFilterSearchHelper by lazy {
        TrackerFilterSearchHelper(filterRepository, filterManager)
    }
    private val eventProgramFilterSearchHelper: EventProgramFilterSearchHelper by lazy {
        EventProgramFilterSearchHelper(filterRepository, filterManager)
    }

    private var filteredOrgUnitResult: FilteredOrgUnitResult? = null

    fun filteredDataSetInstances(): DataSetInstanceSummaryCollectionRepository {
        return dataSetFilterSearchHelper.getFilteredDataSetSearchRepository()
    }

    fun filteredEventProgram(
        program: Program,
        textFilter: TextFilter?
    ): EventQueryCollectionRepository {
        return eventProgramFilterSearchHelper.getFilteredEventRepository(program, textFilter)
    }

    fun filteredEventProgram(
        program: Program
    ): EventQueryCollectionRepository {
        return eventProgramFilterSearchHelper.getFilteredEventRepository(program)
    }

    fun filteredTrackerProgram(program: Program): TrackedEntitySearchCollectionRepository {
        return trackerFilterSearchHelper.getFilteredProgramRepository(program.uid())
    }

    fun filteredTrackedEntityTypes(
        trackedEntityTypeUid: String,
    ): TrackedEntitySearchCollectionRepository {
        return trackerFilterSearchHelper
            .getFilteredTrackedEntityTypeRepository(trackedEntityTypeUid)
    }

    fun filteredTrackedEntityInstances(
        program: Program?,
        trackedEntityTypeUid: String,
    ): TrackedEntitySearchCollectionRepository {
        return program?.let { filteredTrackerProgram(program) }
            ?: filteredTrackedEntityTypes(trackedEntityTypeUid)
    }

    fun isAssignedToMeApplied(): Boolean {
        return filterManager.assignedFilter
    }

    fun areFiltersActive(): Boolean {
        return filterManager.totalFilters != 0
    }

    fun getOrgUnitsByName(name: String): FilteredOrgUnitResult {
        filteredOrgUnitResult = FilteredOrgUnitResult(
            if (name.length > ORG_UNT_FILTER_MIN_CHAR) {
                filterRepository.orgUnitsByName(name)
            } else {
                emptyList()
            },
        )
        return filteredOrgUnitResult!!
    }

    fun addOrgUnitToFilter(callback: () -> Unit) {
        if (filteredOrgUnitResult?.firstResult() != null) {
            filterManager.addOrgUnit(filteredOrgUnitResult!!.firstResult())
            callback.invoke()
        }
    }

    fun onOpenOrgUnitTreeSelector() {
        filterManager.ouTreeProcessor.onNext(true)
    }
}
