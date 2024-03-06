package org.dhis2.commons.filters.data

import java.util.Calendar
import javax.inject.Inject
import org.dhis2.commons.filters.FilterManager
import org.dhis2.commons.filters.Filters
import org.hisp.dhis.android.core.event.EventStatus
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitMode
import org.hisp.dhis.android.core.period.DatePeriod
import org.hisp.dhis.android.core.trackedentity.search.TrackedEntityInstanceQueryCollectionRepository

class TrackerFilterSearchHelper @Inject constructor(
    private val filterRepository: FilterRepository,
    val filterManager: FilterManager
) : FilterHelperActions<TrackedEntityInstanceQueryCollectionRepository> {

    fun getFilteredProgramRepository(
        programUid: String
    ): TrackedEntityInstanceQueryCollectionRepository {
        return applyFiltersTo(
            filterRepository.trackedEntityInstanceQueryByProgram(programUid)
        )
    }

    fun getFilteredTrackedEntityTypeRepository(
        trackedEntityTypeUid: String
    ): TrackedEntityInstanceQueryCollectionRepository {
        return applyFiltersTo(
            filterRepository.trackedEntityInstanceQueryByType(trackedEntityTypeUid)
        )
    }

    override fun applyFiltersTo(
        repository: TrackedEntityInstanceQueryCollectionRepository
    ): TrackedEntityInstanceQueryCollectionRepository {
        return repository
            .withFilter { applyWorkingList(it) }
            .withFilter { applyEnrollmentStatusFilter(it) }
            .withFilter { applyEventStatusFilter(it) }
            .withFilter { applyOrgUnitFilter(it) }
            .withFilter { applyStateFilter(it) }
            .withFilter { applyDateFilter(it) }
            .withFilter { applyEnrollmentDateFilter(it) }
            .withFilter { applyAssignedToMeFilter(it) }
            .withFilter { applyFollowUpFilter(it) }
            .withFilter { applySorting(it) }
    }

    private fun applyWorkingList(
        teiQuery: TrackedEntityInstanceQueryCollectionRepository
    ): TrackedEntityInstanceQueryCollectionRepository {
        return if (filterManager.workingListActive()) {
            filterRepository.applyWorkingList(
                teiQuery,
                filterManager.currentWorkingList()
            ).also {
                filterManager.setWorkingListScope(
                    it.scope.mapToWorkingListScope(filterRepository.resources)
                )
            }
        } else {
            teiQuery
        }
    }

    private fun applyEnrollmentStatusFilter(
        teiQuery: TrackedEntityInstanceQueryCollectionRepository
    ): TrackedEntityInstanceQueryCollectionRepository {
        return if (filterManager.enrollmentStatusFilters.isNotEmpty()) {
            filterRepository.applyEnrollmentStatusFilter(
                teiQuery,
                filterManager.enrollmentStatusFilters
            )
        } else {
            teiQuery
        }
    }

    private fun applyEventStatusFilter(
        teiQuery: TrackedEntityInstanceQueryCollectionRepository
    ): TrackedEntityInstanceQueryCollectionRepository {
        return if (filterManager.eventStatusFilters.isNotEmpty()) {
            filterRepository.applyEventStatusFilter(
                teiQuery,
                filterManager.eventStatusFilters
            ).let {
                if (filterManager.periodFilters.isEmpty()) {
                    val datePeriod = DatePeriod.create(
                        Calendar.getInstance().apply { add(Calendar.YEAR, -1) }.time,
                        Calendar.getInstance().apply { add(Calendar.YEAR, 1) }.time
                    )
                    filterRepository.applyDateFilter(it, datePeriod)
                } else {
                    it
                }
            }
        } else {
            teiQuery
        }
    }

    private fun applyOrgUnitFilter(
        teiQuery: TrackedEntityInstanceQueryCollectionRepository
    ): TrackedEntityInstanceQueryCollectionRepository {
        val orgUnits: MutableList<String> = mutableListOf()
        val ouMode = if (filterManager.orgUnitUidsFilters.isEmpty()) {
            orgUnits.addAll(
                filterRepository.rootOrganisationUnitUids()
            )
            OrganisationUnitMode.DESCENDANTS
        } else {
            orgUnits.addAll(filterManager.orgUnitUidsFilters)
            OrganisationUnitMode.SELECTED
        }
        return filterRepository.applyOrgUnitFilter(teiQuery, ouMode, orgUnits)
    }

    private fun applyStateFilter(
        teiQuery: TrackedEntityInstanceQueryCollectionRepository
    ): TrackedEntityInstanceQueryCollectionRepository {
        return if (filterManager.stateFilters.isNotEmpty()) {
            filterRepository.applyStateFilter(teiQuery, filterManager.stateFilters)
        } else {
            teiQuery
        }
    }

    private fun applyDateFilter(
        teiQuery: TrackedEntityInstanceQueryCollectionRepository
    ): TrackedEntityInstanceQueryCollectionRepository {
        return if (filterManager.periodFilters.isNotEmpty()) {
            filterRepository.applyDateFilter(teiQuery, filterManager.periodFilters[0]).let {
                if (filterManager.eventStatusFilters.isEmpty()) {
                    filterRepository.applyEventStatusFilter(
                        it,
                        EventStatus.values().toMutableList()
                    )
                } else {
                    it
                }
            }
        } else {
            teiQuery
        }
    }

    private fun applyEnrollmentDateFilter(
        teiQuery: TrackedEntityInstanceQueryCollectionRepository
    ): TrackedEntityInstanceQueryCollectionRepository {
        return if (filterManager.enrollmentPeriodFilters.isNotEmpty()) {
            filterRepository.applyEnrollmentDateFilter(
                teiQuery,
                filterManager.enrollmentPeriodFilters[0]
            )
        } else {
            teiQuery
        }
    }

    private fun applyAssignedToMeFilter(
        teiQuery: TrackedEntityInstanceQueryCollectionRepository
    ): TrackedEntityInstanceQueryCollectionRepository {
        return if (filterManager.assignedFilter) {
            filterRepository.applyAssignToMe(teiQuery)
        } else {
            teiQuery
        }
    }

    private fun applyFollowUpFilter(
        teiQuery: TrackedEntityInstanceQueryCollectionRepository
    ): TrackedEntityInstanceQueryCollectionRepository {
        return if (filterManager.followUpFilter) {
            filterRepository.applyFollowUp(teiQuery)
        } else {
            teiQuery
        }
    }

    override fun applySorting(
        repository: TrackedEntityInstanceQueryCollectionRepository
    ): TrackedEntityInstanceQueryCollectionRepository {
        return filterManager.sortingItem?.let { sortingItem ->
            val orderDirection = getSortingDirection(
                filterManager.sortingItem.sortingStatus
            )
            orderDirection?.let {
                when (sortingItem.filterSelectedForSorting) {
                    Filters.PERIOD -> filterRepository.sortByPeriod(repository, orderDirection)
                    Filters.ORG_UNIT -> filterRepository.sortByOrgUnit(repository, orderDirection)
                    Filters.ENROLLMENT_DATE -> filterRepository.sortByEnrollmentDate(
                        repository,
                        orderDirection
                    )
                    Filters.ENROLLMENT_STATUS -> filterRepository.sortByEnrollmentStatus(
                        repository,
                        orderDirection
                    )
                    else -> repository
                }
            } ?: repository
        } ?: repository
    }
}
