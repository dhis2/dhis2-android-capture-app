package org.dhis2.data.filter

import javax.inject.Inject
import org.dhis2.utils.filters.FilterManager
import org.dhis2.utils.filters.Filters
import org.hisp.dhis.android.core.event.EventCollectionRepository
import org.hisp.dhis.android.core.program.Program

class EventProgramFilterSearchHelper @Inject constructor(
    private val filterRepository: FilterRepository,
    val filterManager: FilterManager
) : FilterHelperActions<EventCollectionRepository> {

    fun getFilteredEventRepository(
        program: Program
    ): EventCollectionRepository {
        return applyFiltersTo(
            filterRepository.eventsByProgram(program.uid())
        )
    }

    override fun applyFiltersTo(
        repository: EventCollectionRepository
    ): EventCollectionRepository {
        return repository
            .withFilter { applyOrgUnitFilter(it) }
            .withFilter { applyStateFilter(it) }
            .withFilter { applyDateFilter(it) }
            .withFilter { applyAssignedToMeFilter(it) }
            .withFilter { applySorting(it) }
    }

    private fun applyOrgUnitFilter(
        eventRepository: EventCollectionRepository
    ): EventCollectionRepository {
        return if (filterManager.orgUnitUidsFilters.isNotEmpty()) {
            filterRepository.applyOrgUnitFilter(eventRepository, filterManager.orgUnitUidsFilters)
        } else {
            eventRepository
        }
    }

    private fun applyStateFilter(
        eventRepository: EventCollectionRepository
    ): EventCollectionRepository {
        return if (filterManager.stateFilters.isNotEmpty()) {
            filterRepository.applyStateFilter(eventRepository, filterManager.stateFilters)
        } else {
            eventRepository
        }
    }

    private fun applyDateFilter(
        eventRepository: EventCollectionRepository
    ): EventCollectionRepository {
        return if (filterManager.periodFilters.isNotEmpty()) {
            filterRepository.applyDateFilter(eventRepository, filterManager.periodFilters)
        } else {
            eventRepository
        }
    }

    private fun applyAssignedToMeFilter(
        eventRepository: EventCollectionRepository
    ): EventCollectionRepository {
        return if (filterManager.assignedFilter) {
            filterRepository.applyAssignToMe(eventRepository)
        } else {
            eventRepository
        }
    }

    override fun applySorting(
        repository: EventCollectionRepository
    ): EventCollectionRepository {
        return filterManager.sortingItem?.let { sortingItem ->
            val orderDirection = getSortingDirection(
                filterManager.sortingItem.sortingStatus
            )
            orderDirection?.let {
                when (sortingItem.filterSelectedForSorting) {
                    Filters.PERIOD -> filterRepository.sortByEventDate(
                        repository,
                        orderDirection
                    )
                    Filters.ORG_UNIT -> filterRepository.sortByOrgUnit(
                        repository,
                        orderDirection
                    )
                    else -> repository
                }
            } ?: repository
        } ?: repository
    }
}
