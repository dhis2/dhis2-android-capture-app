package org.dhis2.data.filter

import java.util.Calendar
import javax.inject.Inject
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope
import org.hisp.dhis.android.core.common.AssignedUserMode
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.dataset.DataSetInstanceSummaryCollectionRepository
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus
import org.hisp.dhis.android.core.event.EventCollectionRepository
import org.hisp.dhis.android.core.event.EventStatus
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitMode
import org.hisp.dhis.android.core.period.DatePeriod
import org.hisp.dhis.android.core.trackedentity.search.TrackedEntityInstanceQueryCollectionRepository

class FilterRepository @Inject constructor(private val d2: D2) {

    fun trackedEntityInstanceQueryByProgram(
        programUid: String
    ): TrackedEntityInstanceQueryCollectionRepository {
        return d2.trackedEntityModule().trackedEntityInstanceQuery()
            .byProgram().eq(programUid)
    }

    fun trackedEntityInstanceQueryByType(
        trackedEntityTypeUid: String
    ): TrackedEntityInstanceQueryCollectionRepository {
        return d2.trackedEntityModule().trackedEntityInstanceQuery()
            .byTrackedEntityType().eq(trackedEntityTypeUid)
    }

    fun rootOrganisationUnitUids(): List<String> {
        return d2.organisationUnitModule().organisationUnits()
            .byRootOrganisationUnit(true)
            .blockingGetUids()
    }

    fun applyEnrollmentStatusFilter(
        repository: TrackedEntityInstanceQueryCollectionRepository,
        enrollmentStatuses: List<EnrollmentStatus>
    ): TrackedEntityInstanceQueryCollectionRepository {
        return repository.byEnrollmentStatus().`in`(enrollmentStatuses)
    }

    fun applyEventStatusFilter(
        repository: TrackedEntityInstanceQueryCollectionRepository,
        eventStatuses: List<EventStatus>
    ): TrackedEntityInstanceQueryCollectionRepository {
        val fromDate = Calendar.getInstance().apply {
            add(Calendar.YEAR, -1)
        }.time
        val toDate = Calendar.getInstance().apply {
            add(Calendar.YEAR, 1)
        }.time
        return repository.byEventStatus().`in`(eventStatuses)
            .byEventStartDate().eq(fromDate)
            .byEventEndDate().eq(toDate)
    }

    fun applyOrgUnitFilter(
        repository: TrackedEntityInstanceQueryCollectionRepository,
        ouMode: OrganisationUnitMode,
        orgUnitUis: List<String>
    ): TrackedEntityInstanceQueryCollectionRepository {
        return repository.byOrgUnitMode().eq(ouMode)
            .byOrgUnits().`in`(orgUnitUis)
    }

    fun applyStateFilter(
        repository: TrackedEntityInstanceQueryCollectionRepository,
        states: List<State>
    ): TrackedEntityInstanceQueryCollectionRepository {
        return repository.byStates().`in`(states)
    }

    fun applyDateFilter(
        repository: TrackedEntityInstanceQueryCollectionRepository,
        datePeriod: DatePeriod
    ): TrackedEntityInstanceQueryCollectionRepository {
        return repository.byEventStartDate().eq(datePeriod.startDate())
            .byEventEndDate().eq(datePeriod.endDate())
    }

    fun applyEnrollmentDateFilter(
        repository: TrackedEntityInstanceQueryCollectionRepository,
        datePeriod: DatePeriod
    ): TrackedEntityInstanceQueryCollectionRepository {
        return repository.byProgramStartDate().eq(datePeriod.startDate())
            .byProgramEndDate().eq(datePeriod.endDate())
    }

    fun applyAssignToMe(
        repository: TrackedEntityInstanceQueryCollectionRepository
    ): TrackedEntityInstanceQueryCollectionRepository {
        return repository.byAssignedUserMode().eq(AssignedUserMode.CURRENT)
    }

    fun sortByPeriod(
        repository: TrackedEntityInstanceQueryCollectionRepository,
        orderDirection: RepositoryScope.OrderByDirection
    ): TrackedEntityInstanceQueryCollectionRepository {
        return repository.orderByEventDate().eq(orderDirection)
    }

    fun sortByOrgUnit(
        repository: TrackedEntityInstanceQueryCollectionRepository,
        orderDirection: RepositoryScope.OrderByDirection
    ): TrackedEntityInstanceQueryCollectionRepository {
        return repository.orderByOrganisationUnitName().eq(orderDirection)
    }

    fun sortByEnrollmentDate(
        repository: TrackedEntityInstanceQueryCollectionRepository,
        orderDirection: RepositoryScope.OrderByDirection
    ): TrackedEntityInstanceQueryCollectionRepository {
        return repository.orderByEnrollmentDate().eq(orderDirection)
    }

    fun sortByEnrollmentStatus(
        repository: TrackedEntityInstanceQueryCollectionRepository,
        orderDirection: RepositoryScope.OrderByDirection
    ): TrackedEntityInstanceQueryCollectionRepository {
        return repository.orderByEnrollmentStatus().eq(orderDirection)
    }

    fun eventsByProgram(programUid: String): EventCollectionRepository {
        return d2.eventModule().events()
            .byDeleted().isFalse
            .byProgramUid().eq(programUid)
    }

    fun applyOrgUnitFilter(
        repository: EventCollectionRepository,
        orgUnitUis: List<String>
    ): EventCollectionRepository {
        return repository.byOrganisationUnitUid().`in`(orgUnitUis)
    }

    fun applyStateFilter(
        repository: EventCollectionRepository,
        states: List<State>
    ): EventCollectionRepository {
        return repository.byState().`in`(states)
    }

    fun applyDateFilter(
        repository: EventCollectionRepository,
        datePeriods: List<DatePeriod>
    ): EventCollectionRepository {
        return repository.byEventDate().inDatePeriods(datePeriods)
    }

    fun applyAssignToMe(repository: EventCollectionRepository): EventCollectionRepository {
        return repository.byAssignedUser().eq(currentUserUid())
    }

    private fun currentUserUid(): String {
        return d2.userModule().user().blockingGet().uid()
    }

    fun sortByEventDate(
        repository: EventCollectionRepository,
        orderDirection: RepositoryScope.OrderByDirection
    ): EventCollectionRepository {
        return repository.orderByEventDate(orderDirection)
    }

    fun sortByOrgUnit(
        repository: EventCollectionRepository,
        orderDirection: RepositoryScope.OrderByDirection
    ): EventCollectionRepository {
        return repository.orderByOrganisationUnitName(orderDirection)
    }

    fun dataSetInstanceSummaries(): DataSetInstanceSummaryCollectionRepository {
        return d2.dataSetModule().dataSetInstanceSummaries()
    }

    fun applyOrgUnitFilter(
        repository: DataSetInstanceSummaryCollectionRepository,
        orgUnitUis: List<String>
    ): DataSetInstanceSummaryCollectionRepository {
        return repository.byOrganisationUnitUid().`in`(orgUnitUis)
    }

    fun applyStateFilter(
        repository: DataSetInstanceSummaryCollectionRepository,
        states: List<State>
    ): DataSetInstanceSummaryCollectionRepository {
        return repository.byState().`in`(states)
    }

    fun applyPeriodFilter(
        repository: DataSetInstanceSummaryCollectionRepository,
        datePeriods: List<DatePeriod>
    ): DataSetInstanceSummaryCollectionRepository {
        return repository.byPeriodStartDate().inDatePeriods(datePeriods)
    }

    fun orgUnitsByName(name: String): List<OrganisationUnit> =
        d2.organisationUnitModule()
            .organisationUnits()
            .byDisplayName().like("%$name%")
            .blockingGet()
}
