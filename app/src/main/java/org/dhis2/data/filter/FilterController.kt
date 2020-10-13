package org.dhis2.data.filter

import javax.inject.Inject
import org.dhis2.utils.filters.FilterManager
import org.hisp.dhis.android.core.dataset.DataSetInstanceSummaryCollectionRepository
import org.hisp.dhis.android.core.event.EventCollectionRepository
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.trackedentity.search.TrackedEntityInstanceQueryCollectionRepository

class FilterController @Inject constructor(
    private val filterRepository: FilterRepository,
    val filterManager: FilterManager
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

    fun filteredDataSetInstances(): DataSetInstanceSummaryCollectionRepository {
        return dataSetFilterSearchHelper.getFilteredDataSetSearchRepository()
    }

    fun filteredEventProgram(program: Program): EventCollectionRepository {
        return eventProgramFilterSearchHelper.getFilteredEventRepository(program)
    }

    fun filteredTrackerProgram(program: Program): TrackedEntityInstanceQueryCollectionRepository {
        return trackerFilterSearchHelper.getFilteredProgramRepository(program.uid())
    }

    fun filteredTrackedEntityTypes(
        trackedEntityTypeUid: String
    ): TrackedEntityInstanceQueryCollectionRepository {
        return trackerFilterSearchHelper
            .getFilteredTrackedEntityTypeRepository(trackedEntityTypeUid)
    }

    fun filteredTrackedEntityInstances(
        program: Program?,
        trackedEntityTypeUid: String
    ): TrackedEntityInstanceQueryCollectionRepository {
        return program?.let { filteredTrackerProgram(program) }
            ?: filteredTrackedEntityTypes(trackedEntityTypeUid)
    }

    fun isAssignedToMeApplied(): Boolean {
        return filterManager.assignedFilter
    }

    fun areFiltersActive(): Boolean {
        return filterManager.totalFilters != 0
    }
}
