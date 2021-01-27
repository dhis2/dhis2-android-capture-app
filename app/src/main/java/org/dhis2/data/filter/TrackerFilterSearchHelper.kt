package org.dhis2.data.filter

import javax.inject.Inject
import org.dhis2.Bindings.toDate
import org.dhis2.utils.filters.FilterManager
import org.dhis2.utils.filters.Filters
import org.hisp.dhis.android.core.arch.repositories.scope.internal.RepositoryMode
import org.hisp.dhis.android.core.common.AssignedUserMode
import org.hisp.dhis.android.core.common.DateFilterPeriod
import org.hisp.dhis.android.core.common.DatePeriodType
import org.hisp.dhis.android.core.common.RelativePeriod
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus
import org.hisp.dhis.android.core.event.EventStatus
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitMode
import org.hisp.dhis.android.core.trackedentity.search.TrackedEntityInstanceQueryCollectionRepository
import org.hisp.dhis.android.core.trackedentity.search.TrackedEntityInstanceQueryEventFilter
import org.hisp.dhis.android.core.trackedentity.search.TrackedEntityInstanceQueryRepositoryScope

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
//                    testingScope().mapToWorkingListScope(filterRepository.resources)
                )
            }
        } else {
            teiQuery
        }
    }

    // TODO: FOR TESTING. Uncomment code and set required methods public
    /*private fun testingScope(): TrackedEntityInstanceQueryRepositoryScope {
        return TrackedEntityInstanceQueryRepositoryScope.builder()
            .mode(RepositoryMode.OFFLINE_FIRST)
            .orgUnits(arrayListOf("org_unit_1"))
            .programDate(
                DateFilterPeriod.builder().type(DatePeriodType.ABSOLUTE)
                    .startDate("2020-01-01".toDate())
                    .endDate("2021-01-01".toDate())
                    .build()
            )
            .enrollmentStatus(arrayListOf(EnrollmentStatus.COMPLETED, EnrollmentStatus.CANCELLED))
            .eventFilters(
                arrayListOf(
                    TrackedEntityInstanceQueryEventFilter.builder()
                        .eventDate(
                            DateFilterPeriod.builder().type(DatePeriodType.RELATIVE)
                                .period(RelativePeriod.LAST_3_DAYS).build()
                        )
                        .assignedUserMode(AssignedUserMode.CURRENT)
                        .eventStatus(
                            arrayListOf(
                                EventStatus.OVERDUE,
                                EventStatus.SCHEDULE
                            )
                        )
                        .build()
                )
            )
            .build()
    }*/

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
            )
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
            filterRepository.applyDateFilter(teiQuery, filterManager.periodFilters[0])
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
