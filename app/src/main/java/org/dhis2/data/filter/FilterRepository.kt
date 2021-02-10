package org.dhis2.data.filter

import androidx.databinding.ObservableField
import java.util.Calendar
import javax.inject.Inject
import org.dhis2.utils.filters.AssignedFilter
import org.dhis2.utils.filters.CatOptionComboFilter
import org.dhis2.utils.filters.EnrollmentDateFilter
import org.dhis2.utils.filters.EnrollmentStatusFilter
import org.dhis2.utils.filters.EventStatusFilter
import org.dhis2.utils.filters.FilterItem
import org.dhis2.utils.filters.FilterManager
import org.dhis2.utils.filters.Filters
import org.dhis2.utils.filters.OrgUnitFilter
import org.dhis2.utils.filters.PeriodFilter
import org.dhis2.utils.filters.SyncStateFilter
import org.dhis2.utils.filters.WorkingListFilter
import org.dhis2.utils.filters.sorting.SortingItem
import org.dhis2.utils.filters.workingLists.WorkingListItem
import org.dhis2.utils.resources.ResourceManager
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope
import org.hisp.dhis.android.core.category.CategoryOptionCombo
import org.hisp.dhis.android.core.common.AssignedUserMode
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.dataset.DataSetInstanceSummaryCollectionRepository
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus
import org.hisp.dhis.android.core.event.EventCollectionRepository
import org.hisp.dhis.android.core.event.EventStatus
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitMode
import org.hisp.dhis.android.core.period.DatePeriod
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.program.ProgramType
import org.hisp.dhis.android.core.settings.FilterConfig
import org.hisp.dhis.android.core.settings.HomeFilter
import org.hisp.dhis.android.core.trackedentity.search.TrackedEntityInstanceQueryCollectionRepository

class FilterRepository @Inject constructor(
    private val d2: D2,
    val resources: ResourceManager
) {

    private val observableSortingInject = ObservableField<SortingItem>()
    private val observableOpenFilter = ObservableField<Filters>()

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

    fun applyEventStatusFilter(
        repository: EventCollectionRepository,
        eventStatuses: List<EventStatus>
    ): EventCollectionRepository {
        return repository.byStatus().`in`(eventStatuses)
    }

    fun applyCategoryOptionComboFilter(
        repository: EventCollectionRepository,
        categoryOptionCombos: List<CategoryOptionCombo>
    ): EventCollectionRepository {
        return repository.byAttributeOptionComboUid().`in`(
            categoryOptionCombos.map { it.uid() }
        )
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

    fun programFilters(programUid: String): List<FilterItem> {
        return d2.programModule().programs().uid(programUid).get()
            .map {
                if (it.programType() == ProgramType.WITH_REGISTRATION) {
                    getTrackerFilters(it)
                } else {
                    getEventFilters(it)
                }
            }.blockingGet()
    }

    fun trackedEntityFilters(): List<FilterItem> {
        return mutableListOf<FilterItem>().apply {
            add(
                PeriodFilter(
                    org.dhis2.utils.filters.ProgramType.TRACKER,
                    observableSortingInject,
                    observableOpenFilter,
                    resources.filterResources.filterEventDateLabel()
                )
            )
            add(
                OrgUnitFilter(
                    FilterManager.getInstance().observeOrgUnitFilters(),
                    org.dhis2.utils.filters.ProgramType.TRACKER,
                    observableSortingInject,
                    observableOpenFilter,
                    resources.filterResources.filterOrgUnitLabel()
                )
            )
            add(
                SyncStateFilter(
                    org.dhis2.utils.filters.ProgramType.TRACKER,
                    observableSortingInject,
                    observableOpenFilter,
                    resources.filterResources.filterSyncLabel()
                )
            )
            add(
                EnrollmentStatusFilter(
                    org.dhis2.utils.filters.ProgramType.TRACKER,
                    observableSortingInject,
                    observableOpenFilter,
                    resources.filterResources.filterEnrollmentStatusLabel()
                )
            )
            add(
                EventStatusFilter(
                    org.dhis2.utils.filters.ProgramType.TRACKER,
                    observableSortingInject,
                    observableOpenFilter,
                    resources.filterResources.filterEventStatusLabel()
                )
            )
        }
    }

    fun dataSetFilters(dataSetUid: String): List<FilterItem> {
        return mutableListOf<FilterItem>().apply {
            add(
                PeriodFilter(
                    org.dhis2.utils.filters.ProgramType.DATASET,
                    observableSortingInject,
                    observableOpenFilter,
                    resources.filterResources.filterPeriodLabel()
                )
            )
            add(
                OrgUnitFilter(
                    FilterManager.getInstance().observeOrgUnitFilters(),
                    org.dhis2.utils.filters.ProgramType.DATASET,
                    observableSortingInject,
                    observableOpenFilter,
                    resources.filterResources.filterOrgUnitLabel()
                )
            )
            add(
                SyncStateFilter(
                    org.dhis2.utils.filters.ProgramType.DATASET,
                    observableSortingInject,
                    observableOpenFilter,
                    resources.filterResources.filterSyncLabel()
                )
            )
            val dataSet = d2.dataSetModule().dataSets().uid(dataSetUid).blockingGet()
            val categoryCombo =
                d2.categoryModule().categoryCombos().uid(dataSet.categoryCombo()?.uid())
                    .blockingGet()
            if (categoryCombo.isDefault == false) {
                add(
                    CatOptionComboFilter(
                        categoryCombo,
                        d2.categoryModule().categoryOptionCombos().byCategoryComboUid()
                            .eq(categoryCombo.uid()).blockingGet(),
                        org.dhis2.utils.filters.ProgramType.DATASET,
                        observableSortingInject,
                        observableOpenFilter,
                        categoryCombo.displayName() ?: ""
                    )
                )
            }
        }
    }

    fun homeFilters(): List<FilterItem> {
        val defaultFilters = createDefaultHomeFilters();

        if (webAppIsNotConfigured()) return defaultFilters.values.toList();

        //Fetch Home filters from SDK webapp
        val homeFiltersWebApp: Map<HomeFilter, FilterConfig> = mapOf()
        val homeFiltersWebAppKeys = homeFiltersWebApp.filterValues { it.filter() }.keys.toList()
        val filtersToShow = defaultFilters.filter { homeFiltersWebAppKeys.contains(it.key) }

        if (filtersToShow.isEmpty()) return mutableListOf()

        return filtersToShow.values.toList()
    }

    private fun mockHomeFilterSDK(): Map<HomeFilter, FilterConfig> {
        return mapOf()
    }

    private fun createDefaultHomeFilters(): LinkedHashMap<HomeFilter, FilterItem> {

        val homeFilter = linkedMapOf(
            HomeFilter.DATE to PeriodFilter(
                org.dhis2.utils.filters.ProgramType.ALL,
                observableSortingInject, observableOpenFilter,
                resources.filterResources.filterDateLabel()
            )
            , HomeFilter.ORG_UNIT to OrgUnitFilter(
                FilterManager.getInstance().observeOrgUnitFilters(),
                org.dhis2.utils.filters.ProgramType.ALL,
                observableSortingInject,
                observableOpenFilter,
                resources.filterResources.filterOrgUnitLabel()
            )
            , HomeFilter.SYN_STATUS to SyncStateFilter(
                org.dhis2.utils.filters.ProgramType.ALL,
                observableSortingInject, observableOpenFilter,
                resources.filterResources.filterSyncLabel()
            )
        )

        if (!d2.programModule().programStages()
                .byEnableUserAssignment().eq(true).blockingIsEmpty()
        ) {
            val assignToMeFilter = AssignedFilter(
                programType = org.dhis2.utils.filters.ProgramType.ALL,
                sortingItem = observableSortingInject,
                openFilter = observableOpenFilter,
                filterLabel = resources.filterResources.filterAssignedToMeLabel()
            )
            homeFilter[HomeFilter.ASSIGNED_TO_ME] = assignToMeFilter
        }
        return homeFilter
    }

    private fun webAppIsNotConfigured(): Boolean {
        return true
    }

    private fun getTrackerFilters(program: Program): List<FilterItem> {
        return mutableListOf<FilterItem>().apply {
            val workingLists = d2.trackedEntityModule().trackedEntityInstanceFilters()
                .byProgram().eq(program.uid())
                .withTrackedEntityInstanceEventFilters()
                .blockingGet()
                .map {
                    WorkingListItem(
                        it.uid(),
                        it.displayName() ?: ""
                    )
                }
            if (workingLists.isNotEmpty()) {
                add(
                    WorkingListFilter(
                        workingLists,
                        org.dhis2.utils.filters.ProgramType.TRACKER,
                        observableSortingInject, observableOpenFilter,
                        ""
                    )
                )
            }
            add(
                PeriodFilter(
                    org.dhis2.utils.filters.ProgramType.TRACKER,
                    observableSortingInject, observableOpenFilter,
                    resources.filterResources.filterEventDateLabel()
                )
            )
            add(
                EnrollmentDateFilter(
                    org.dhis2.utils.filters.ProgramType.TRACKER,
                    observableSortingInject, observableOpenFilter,
                    program.enrollmentDateLabel() ?: resources.filterResources
                        .filterEnrollmentDateLabel()
                )
            )
            add(
                OrgUnitFilter(
                    FilterManager.getInstance().observeOrgUnitFilters(),
                    org.dhis2.utils.filters.ProgramType.TRACKER,
                    observableSortingInject,
                    observableOpenFilter,
                    resources.filterResources.filterOrgUnitLabel()
                )
            )
            add(
                SyncStateFilter(
                    org.dhis2.utils.filters.ProgramType.TRACKER,
                    observableSortingInject, observableOpenFilter,
                    resources.filterResources.filterSyncLabel()
                )
            )
            add(
                EnrollmentStatusFilter(
                    org.dhis2.utils.filters.ProgramType.TRACKER,
                    observableSortingInject, observableOpenFilter,
                    resources.filterResources.filterEnrollmentStatusLabel()
                )
            )
            add(
                EventStatusFilter(
                    org.dhis2.utils.filters.ProgramType.TRACKER,
                    observableSortingInject, observableOpenFilter,
                    resources.filterResources.filterEventStatusLabel()
                )
            )
            if (!d2.programModule().programStages().byProgramUid().eq(program.uid())
                    .byEnableUserAssignment().eq(true).blockingIsEmpty()
            ) {
                add(
                    AssignedFilter(
                        programType = org.dhis2.utils.filters.ProgramType.TRACKER,
                        sortingItem = observableSortingInject,
                        openFilter = observableOpenFilter,
                        filterLabel = resources.filterResources.filterAssignedToMeLabel()
                    )
                )
            }
        }
    }

    private fun getEventFilters(program: Program): List<FilterItem> {
        return mutableListOf<FilterItem>().apply {
            val workingLists =
                d2.eventModule().eventFilters().byProgram().eq(program.uid()).blockingGet().map {
                    WorkingListItem(
                        it.uid(),
                        it.displayName() ?: ""
                    )
                }
            if (workingLists.isNotEmpty()) {
                add(
                    WorkingListFilter(
                        workingLists,
                        org.dhis2.utils.filters.ProgramType.EVENT,
                        observableSortingInject, observableOpenFilter,
                        ""
                    )
                )
            }
            add(
                PeriodFilter(
                    org.dhis2.utils.filters.ProgramType.EVENT,
                    observableSortingInject,
                    observableOpenFilter,
                    resources.filterResources.filterDateLabel()
                )
            )
            add(
                OrgUnitFilter(
                    FilterManager.getInstance().observeOrgUnitFilters(),
                    org.dhis2.utils.filters.ProgramType.EVENT,
                    observableSortingInject,
                    observableOpenFilter,
                    resources.filterResources.filterOrgUnitLabel()
                )
            )
            add(
                SyncStateFilter(
                    org.dhis2.utils.filters.ProgramType.EVENT,
                    observableSortingInject, observableOpenFilter,
                    resources.filterResources.filterSyncLabel()
                )
            )
            add(
                EventStatusFilter(
                    org.dhis2.utils.filters.ProgramType.EVENT,
                    observableSortingInject, observableOpenFilter,
                    resources.filterResources.filterEventStatusLabel()
                )
            )
            if (!d2.programModule().programStages().byProgramUid().eq(program.uid())
                    .byEnableUserAssignment().eq(true).blockingIsEmpty()
            ) {
                add(
                    AssignedFilter(
                        programType = org.dhis2.utils.filters.ProgramType.EVENT,
                        sortingItem = observableSortingInject,
                        openFilter = observableOpenFilter,
                        filterLabel = resources.filterResources.filterAssignedToMeLabel()
                    )
                )
            }
            val categoryCombo =
                d2.categoryModule().categoryCombos().uid(program.categoryComboUid()).blockingGet()
            if (categoryCombo.isDefault == false) {
                add(
                    CatOptionComboFilter(
                        categoryCombo,
                        d2.categoryModule().categoryOptionCombos().byCategoryComboUid()
                            .eq(categoryCombo.uid()).blockingGet(),
                        org.dhis2.utils.filters.ProgramType.EVENT,
                        observableSortingInject,
                        observableOpenFilter,
                        categoryCombo.displayName() ?: ""
                    )
                )
            }
        }
    }

    fun applyWorkingList(
        teiQuery: TrackedEntityInstanceQueryCollectionRepository,
        currentWorkingList: WorkingListItem?
    ): TrackedEntityInstanceQueryCollectionRepository {
        return currentWorkingList?.let {
            teiQuery.byTrackedEntityInstanceFilter().eq(it.uid)
        } ?: teiQuery
    }

    fun applyWorkingList(
        eventQuery: EventCollectionRepository,
        currentWorkingList: WorkingListItem?
    ): EventCollectionRepository {
        return currentWorkingList?.let {
            // TODO: Should be EventQueryCollectionRepository?
            eventQuery
        } ?: eventQuery
    }
}
