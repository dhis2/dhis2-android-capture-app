package org.dhis2.commons.filters.data

import javax.inject.Inject
import org.dhis2.commons.filters.FilterManager
import org.dhis2.commons.filters.Filters
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.event.search.EventQueryCollectionRepository
import org.hisp.dhis.android.core.program.Program

class EventProgramFilterSearchHelper @Inject constructor(
    private val filterRepository: FilterRepository,
    val filterManager: FilterManager
) : FilterHelperActions<EventQueryCollectionRepository> {

    fun getFilteredEventRepository(
        program: Program,
        textFilter: TextFilter?
    ): EventQueryCollectionRepository {
        return applyFiltersTo(
            if (textFilter != null) {
                filterRepository.eventsByProgramAndTextFilter(program.uid(), textFilter)
            } else {
                filterRepository.eventsByProgram(program.uid())
            }
        )
    }

    fun getFilteredEventRepository(
        program: Program
    ): EventQueryCollectionRepository {
        return applyFiltersTo(
            filterRepository.eventsByProgram(program.uid())
        )
    }

    override fun applyFiltersTo(
        repository: EventQueryCollectionRepository
    ): EventQueryCollectionRepository {
        return repository
            .withFilter { applyWorkingList(it) }
            .withFilter { applyDateFilter(it) }
            .withFilter { applyOrgUnitFilter(it) }
            .withFilter { applyStateFilter(it) }
            .withFilter { applyEventStatus(it) }
            .withFilter { applyAssignedToMeFilter(it) }
            .withFilter { applyCategoryOptionComboFilter(it) }
            .withFilter { applySorting(it) }
    }

    private fun applyWorkingList(
        eventRepository: EventQueryCollectionRepository
    ): EventQueryCollectionRepository {
        return if (filterManager.workingListActive()) {
            filterRepository.applyWorkingList(
                eventRepository,
                filterManager.currentWorkingList()
            ).also {
                filterManager.setWorkingListScope(
                    it.scope.mapToEventWorkingListScope(
                        filterRepository.resources
                    )
                )
            }
        } else {
            eventRepository
        }
    }

    private fun applyEventStatus(
        eventRepository: EventQueryCollectionRepository
    ): EventQueryCollectionRepository {
        return if (filterManager.eventStatusFilters.isNotEmpty()) {
            filterRepository.applyEventStatusFilter(
                eventRepository,
                filterManager.eventStatusFilters
            )
        } else {
            eventRepository
        }
    }

    private fun applyCategoryOptionComboFilter(
        eventRepository: EventQueryCollectionRepository
    ): EventQueryCollectionRepository {
        return if (filterManager.catOptComboFilters.isNotEmpty()) {
            filterRepository.applyCategoryOptionComboFilter(
                eventRepository,
                filterManager.catOptComboFilters
            )
        } else {
            eventRepository
        }
    }

    private fun applyOrgUnitFilter(
        eventRepository: EventQueryCollectionRepository
    ): EventQueryCollectionRepository {
        return if (filterManager.orgUnitUidsFilters.isNotEmpty()) {
            filterRepository.applyOrgUnitFilter(eventRepository, filterManager.orgUnitUidsFilters)
        } else {
            eventRepository
        }
    }

    private fun applyStateFilter(
        eventRepository: EventQueryCollectionRepository
    ): EventQueryCollectionRepository {
        return if (filterManager.stateFilters.isNotEmpty()) {
            filterRepository.applyStateFilter(eventRepository, filterManager.stateFilters)
        } else {
            val allStatesButRelationships = State.values().filter { it != State.RELATIONSHIP }
            filterRepository.applyStateFilter(eventRepository, allStatesButRelationships)
        }
    }

    private fun applyDateFilter(
        eventRepository: EventQueryCollectionRepository
    ): EventQueryCollectionRepository {
        return if (filterManager.periodFilters.isNotEmpty()) {
            filterRepository.applyDateFilter(eventRepository, filterManager.periodFilters.first())
        } else {
            eventRepository
        }
    }

    private fun applyAssignedToMeFilter(
        eventRepository: EventQueryCollectionRepository
    ): EventQueryCollectionRepository {
        return if (filterManager.assignedFilter) {
            filterRepository.applyAssignToMe(eventRepository)
        } else {
            eventRepository
        }
    }

    override fun applySorting(
        repository: EventQueryCollectionRepository
    ): EventQueryCollectionRepository {
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
        } ?: filterRepository.sortByEventDate(repository, RepositoryScope.OrderByDirection.DESC)
    }
}
