package org.dhis2.commons.filters.data

import androidx.databinding.ObservableField
import org.dhis2.commons.filters.AssignedFilter
import org.dhis2.commons.filters.CatOptionComboFilter
import org.dhis2.commons.filters.EnrollmentDateFilter
import org.dhis2.commons.filters.EnrollmentStatusFilter
import org.dhis2.commons.filters.EventStatusFilter
import org.dhis2.commons.filters.FilterItem
import org.dhis2.commons.filters.FilterManager
import org.dhis2.commons.filters.FilterResources
import org.dhis2.commons.filters.Filters
import org.dhis2.commons.filters.FollowUpFilter
import org.dhis2.commons.filters.OrgUnitFilter
import org.dhis2.commons.filters.PeriodFilter
import org.dhis2.commons.filters.ProgramType
import org.dhis2.commons.filters.SyncStateFilter
import org.dhis2.commons.filters.WorkingListFilter
import org.dhis2.commons.filters.sorting.SortingItem
import org.dhis2.commons.filters.workingLists.EventFilterToWorkingListItemMapper
import org.dhis2.commons.filters.workingLists.EventWorkingList
import org.dhis2.commons.filters.workingLists.ProgramStageToWorkingListItemMapper
import org.dhis2.commons.filters.workingLists.ProgramStageWorkingList
import org.dhis2.commons.filters.workingLists.TeiFilterToWorkingListItemMapper
import org.dhis2.commons.filters.workingLists.TrackedEntityInstanceWorkingList
import org.dhis2.commons.filters.workingLists.WorkingListItem
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope
import org.hisp.dhis.android.core.category.CategoryOptionCombo
import org.hisp.dhis.android.core.common.AssignedUserMode
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.dataset.DataSetInstanceSummaryCollectionRepository
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus
import org.hisp.dhis.android.core.event.EventStatus
import org.hisp.dhis.android.core.event.search.EventQueryCollectionRepository
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitMode
import org.hisp.dhis.android.core.period.DatePeriod
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.settings.DataSetFilter
import org.hisp.dhis.android.core.settings.HomeFilter
import org.hisp.dhis.android.core.settings.ProgramFilter
import org.hisp.dhis.android.core.trackedentity.search.TrackedEntitySearchCollectionRepository
import javax.inject.Inject

class FilterRepository @Inject constructor(
    private val d2: D2,
    val resources: FilterResources,
    private val getFiltersApplyingWebAppConfig: GetFiltersApplyingWebAppConfig,
    private val eventFilterToWorkingListItemMapper: EventFilterToWorkingListItemMapper,
    private val teiFilterToWorkingListItemMapper: TeiFilterToWorkingListItemMapper,
    private val programStageToWorkingListItemMapper: ProgramStageToWorkingListItemMapper,
) {

    private val observableSortingInject = ObservableField<SortingItem>()
    private val observableOpenFilter = ObservableField<Filters>()
    private var orgUnitsCount: Int = -1

    init {
        orgUnitsCount =
            d2.organisationUnitModule()
                .organisationUnits()
                .byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_TEI_SEARCH)
                .blockingCount()
    }

    fun trackedEntityInstanceQueryByProgram(
        programUid: String,
    ): TrackedEntitySearchCollectionRepository {
        return d2.trackedEntityModule().trackedEntitySearch()
            .byProgram().eq(programUid)
    }

    fun trackedEntityInstanceQueryByType(
        trackedEntityTypeUid: String,
    ): TrackedEntitySearchCollectionRepository {
        return d2.trackedEntityModule().trackedEntitySearch()
            .byTrackedEntityType().eq(trackedEntityTypeUid)
    }

    fun rootOrganisationUnitUids(): List<String> {
        return d2.organisationUnitModule().organisationUnits()
            .byRootOrganisationUnit(true)
            .blockingGetUids()
    }

    fun applyEnrollmentStatusFilter(
        repository: TrackedEntitySearchCollectionRepository,
        enrollmentStatuses: List<EnrollmentStatus>,
    ): TrackedEntitySearchCollectionRepository {
        return repository.byEnrollmentStatus().`in`(enrollmentStatuses)
    }

    fun applyEventStatusFilter(
        repository: TrackedEntitySearchCollectionRepository,
        eventStatuses: List<EventStatus>,
    ): TrackedEntitySearchCollectionRepository {
        return repository.byEventStatus().`in`(eventStatuses)
    }

    fun applyEventStatusFilter(
        repository: EventQueryCollectionRepository,
        eventStatuses: List<EventStatus>,
    ): EventQueryCollectionRepository {
        return repository.byStatus().`in`(eventStatuses)
    }

    fun applyCategoryOptionComboFilter(
        repository: EventQueryCollectionRepository,
        categoryOptionCombos: List<CategoryOptionCombo>,
    ): EventQueryCollectionRepository {
        return repository.byAttributeOptionCombo().`in`(
            categoryOptionCombos.map { it.uid() },
        )
    }

    fun applyOrgUnitFilter(
        repository: TrackedEntitySearchCollectionRepository,
        ouMode: OrganisationUnitMode,
        orgUnitUis: List<String>,
    ): TrackedEntitySearchCollectionRepository {
        return repository.byOrgUnitMode().eq(ouMode)
            .byOrgUnits().`in`(orgUnitUis)
    }

    fun applyStateFilter(
        repository: TrackedEntitySearchCollectionRepository,
        states: List<State>,
    ): TrackedEntitySearchCollectionRepository {
        return repository.byStates().`in`(states)
    }

    fun applyDateFilter(
        repository: TrackedEntitySearchCollectionRepository,
        datePeriod: DatePeriod,
    ): TrackedEntitySearchCollectionRepository {
        return repository.byEventDate().inDatePeriod(datePeriod)
    }

    fun applyEnrollmentDateFilter(
        repository: TrackedEntitySearchCollectionRepository,
        datePeriod: DatePeriod,
    ): TrackedEntitySearchCollectionRepository {
        return repository.byProgramDate().inDatePeriod(datePeriod)
    }

    fun applyAssignToMe(
        repository: TrackedEntitySearchCollectionRepository,
    ): TrackedEntitySearchCollectionRepository {
        return repository.byAssignedUserMode().eq(AssignedUserMode.CURRENT)
    }

    fun applyFollowUp(
        repository: TrackedEntitySearchCollectionRepository,
    ): TrackedEntitySearchCollectionRepository {
        return repository.byFollowUp().isTrue
    }

    fun sortByPeriod(
        repository: TrackedEntitySearchCollectionRepository,
        orderDirection: RepositoryScope.OrderByDirection,
    ): TrackedEntitySearchCollectionRepository {
        return repository.orderByEventDate().eq(orderDirection)
    }

    fun sortByOrgUnit(
        repository: TrackedEntitySearchCollectionRepository,
        orderDirection: RepositoryScope.OrderByDirection,
    ): TrackedEntitySearchCollectionRepository {
        return repository.orderByOrganisationUnitName().eq(orderDirection)
    }

    fun sortByEnrollmentDate(
        repository: TrackedEntitySearchCollectionRepository,
        orderDirection: RepositoryScope.OrderByDirection,
    ): TrackedEntitySearchCollectionRepository {
        return repository.orderByEnrollmentDate().eq(orderDirection)
    }

    fun sortByEnrollmentStatus(
        repository: TrackedEntitySearchCollectionRepository,
        orderDirection: RepositoryScope.OrderByDirection,
    ): TrackedEntitySearchCollectionRepository {
        return repository.orderByEnrollmentStatus().eq(orderDirection)
    }

    fun eventsByProgram(programUid: String): EventQueryCollectionRepository {
        return d2.eventModule()
            .eventQuery()
            .byIncludeDeleted().eq(false)
            .byProgram().eq(programUid)
    }

    fun applyOrgUnitFilter(
        repository: EventQueryCollectionRepository,
        orgUnitUis: List<String>,
    ): EventQueryCollectionRepository {
        return repository.byOrgUnits().`in`(orgUnitUis)
    }

    fun applyStateFilter(
        repository: EventQueryCollectionRepository,
        states: List<State>,
    ): EventQueryCollectionRepository {
        return repository.byStates().`in`(states)
    }

    fun applyDateFilter(
        repository: EventQueryCollectionRepository,
        datePeriod: DatePeriod,
    ): EventQueryCollectionRepository {
        return repository.byEventDate().inDatePeriod(datePeriod)
    }

    fun applyAssignToMe(
        repository: EventQueryCollectionRepository,
    ): EventQueryCollectionRepository {
        return repository.byAssignedUser().eq(AssignedUserMode.CURRENT)
    }

    fun sortByEventDate(
        repository: EventQueryCollectionRepository,
        orderDirection: RepositoryScope.OrderByDirection,
    ): EventQueryCollectionRepository {
        return repository.orderByEventDate().eq(orderDirection)
    }

    fun sortByOrgUnit(
        repository: EventQueryCollectionRepository,
        orderDirection: RepositoryScope.OrderByDirection,
    ): EventQueryCollectionRepository {
        return repository.orderByOrganisationUnitName().eq(orderDirection)
    }

    fun dataSetInstanceSummaries(): DataSetInstanceSummaryCollectionRepository {
        return d2.dataSetModule().dataSetInstanceSummaries()
    }

    fun applyOrgUnitFilter(
        repository: DataSetInstanceSummaryCollectionRepository,
        orgUnitUis: List<String>,
    ): DataSetInstanceSummaryCollectionRepository {
        return repository.byOrganisationUnitUid().`in`(orgUnitUis)
    }

    fun applyStateFilter(
        repository: DataSetInstanceSummaryCollectionRepository,
        states: List<State>,
    ): DataSetInstanceSummaryCollectionRepository {
        return repository.byState().`in`(states)
    }

    fun applyPeriodFilter(
        repository: DataSetInstanceSummaryCollectionRepository,
        datePeriods: List<DatePeriod>,
    ): DataSetInstanceSummaryCollectionRepository {
        return repository.byPeriodStartDate().inDatePeriods(datePeriods)
    }

    fun orgUnitsByName(name: String): List<OrganisationUnit> = d2.organisationUnitModule()
        .organisationUnits()
        .byDisplayName().like("%$name%")
        .blockingGet()

    fun programFilters(programUid: String): List<FilterItem> {
        return d2.programModule().programs().uid(programUid).get()
            .map {
                if (it.programType() ==
                    org.hisp.dhis.android.core.program.ProgramType.WITH_REGISTRATION
                ) {
                    getTrackerFilters(it)
                } else {
                    getEventFilters(it, ProgramType.EVENT)
                }
            }.blockingGet()
    }

    fun workingListFilter(programUid: String): WorkingListFilter? = try {
        d2.programModule().programs().uid(programUid).get()
            .map {
                if (it.programType() ==
                    org.hisp.dhis.android.core.program.ProgramType.WITH_REGISTRATION
                ) {
                    getTrackerWorkingList(it)
                } else {
                    getEventWorkingList(it)
                }
            }.blockingGet()
    } catch (e: Exception) {
        null
    }

    fun dashboardFilters(programUid: String): List<FilterItem> {
        return d2.programModule().programs().uid(programUid).get().map {
            getEventFilters(it, ProgramType.TRACKER)
        }.blockingGet()
    }

    fun globalTrackedEntityFilters(): List<FilterItem> {
        val defaultFilters = createDefaultTrackedEntityFilters()

        if (webAppIsNotConfigured()) {
            if (orgUnitsCount == 1) {
                defaultFilters.remove(ProgramFilter.ORG_UNIT)
            }
            return defaultFilters.values.toList()
        }

        val globalTrackedEntityTypeFiltersWebApp =
            d2.settingModule().appearanceSettings().getTrackedEntityTypeFilters()
                ?.toMutableMap() ?: mutableMapOf()

        globalTrackedEntityTypeFiltersWebApp.remove(ProgramFilter.ASSIGNED_TO_ME)
        globalTrackedEntityTypeFiltersWebApp.remove(ProgramFilter.ENROLLMENT_DATE)

        if (orgUnitsCount == 1) {
            globalTrackedEntityTypeFiltersWebApp.remove(ProgramFilter.ORG_UNIT)
        }

        return getFiltersApplyingWebAppConfig.execute(
            defaultFilters,
            globalTrackedEntityTypeFiltersWebApp,
        )
    }

    private fun createDefaultTrackedEntityFilters(): LinkedHashMap<ProgramFilter, FilterItem> {
        return linkedMapOf(
            ProgramFilter.ORG_UNIT to OrgUnitFilter(
                FilterManager.getInstance().observeOrgUnitFilters(),
                ProgramType.TRACKER,
                observableSortingInject,
                observableOpenFilter,
                resources.filterOrgUnitLabel(),
            ),
            ProgramFilter.SYNC_STATUS to SyncStateFilter(
                ProgramType.TRACKER,
                observableSortingInject,
                observableOpenFilter,
                resources.filterSyncLabel(),
            ),
        )
    }

    fun dataSetFilters(dataSetUid: String): List<FilterItem> {
        val defaultFilters = createDefaultDatasetFilters(dataSetUid)

        if (webAppIsNotConfigured()) {
            if (orgUnitsCount == 1) {
                defaultFilters.remove(DataSetFilter.ORG_UNIT)
            }
            return defaultFilters.values.toList()
        }

        val datasetFiltersWebApp =
            d2.settingModule().appearanceSettings().getDataSetFiltersByUid(dataSetUid)
                ?.toMutableMap() ?: mutableMapOf()

        if (orgUnitsCount == 1) {
            datasetFiltersWebApp.remove(DataSetFilter.ORG_UNIT)
        }

        return getFiltersApplyingWebAppConfig.execute(defaultFilters, datasetFiltersWebApp)
    }

    private fun createDefaultDatasetFilters(
        dataSetUid: String,
    ): LinkedHashMap<DataSetFilter, FilterItem> {
        val datasetFilters = linkedMapOf(
            DataSetFilter.PERIOD to PeriodFilter(
                ProgramType.DATASET,
                observableSortingInject,
                observableOpenFilter,
                resources.filterPeriodLabel(),
            ),
            DataSetFilter.ORG_UNIT to OrgUnitFilter(
                FilterManager.getInstance().observeOrgUnitFilters(),
                ProgramType.DATASET,
                observableSortingInject,
                observableOpenFilter,
                resources.filterOrgUnitLabel(),
            ),
            DataSetFilter.SYNC_STATUS to SyncStateFilter(
                ProgramType.DATASET,
                observableSortingInject,
                observableOpenFilter,
                resources.filterSyncLabel(),
            ),
        )

        val dataSet = d2.dataSetModule().dataSets().uid(dataSetUid).blockingGet()
        val categoryCombo =
            d2.categoryModule().categoryCombos().uid(dataSet?.categoryCombo()?.uid())
                .blockingGet()
        if (categoryCombo?.isDefault == false) {
            CatOptionComboFilter(
                categoryCombo,
                d2.categoryModule().categoryOptionCombos().byCategoryComboUid()
                    .eq(categoryCombo.uid()).blockingGet(),
                ProgramType.DATASET,
                observableSortingInject,
                observableOpenFilter,
                categoryCombo.displayName() ?: "",
            ).also { datasetFilters[DataSetFilter.CAT_COMBO] = it }
        }

        return datasetFilters
    }

    fun homeFilters(): List<FilterItem> {
        val defaultFilters = createDefaultHomeFilters()

        if (webAppIsNotConfigured()) {
            if (orgUnitsCount == 1) {
                defaultFilters.remove(HomeFilter.ORG_UNIT)
            }
            return defaultFilters.values.toList()
        }

        val homeFiltersWebApp = d2.settingModule().appearanceSettings().getHomeFilters()
            ?.toMutableMap() ?: mutableMapOf()

        if (orgUnitsCount == 1) {
            homeFiltersWebApp.remove(HomeFilter.ORG_UNIT)
        }

        return getFiltersApplyingWebAppConfig.execute(defaultFilters, homeFiltersWebApp)
    }

    private fun createDefaultHomeFilters(): LinkedHashMap<HomeFilter, FilterItem> {
        val homeFilter = linkedMapOf(
            HomeFilter.DATE to PeriodFilter(
                ProgramType.ALL,
                observableSortingInject,
                observableOpenFilter,
                resources.filterDateLabel(),
            ),
            HomeFilter.ORG_UNIT to OrgUnitFilter(
                FilterManager.getInstance().observeOrgUnitFilters(),
                ProgramType.ALL,
                observableSortingInject,
                observableOpenFilter,
                resources.filterOrgUnitLabel(),
            ),
            HomeFilter.SYNC_STATUS to SyncStateFilter(
                ProgramType.ALL,
                observableSortingInject,
                observableOpenFilter,
                resources.filterSyncLabel(),
            ),
        )

        val stagesByUserAssignment = d2.programModule()
            .programStages()
            .byEnableUserAssignment()
            .eq(true)

        if (!stagesByUserAssignment.blockingIsEmpty()) {
            val assignToMeFilter = AssignedFilter(
                programType = ProgramType.ALL,
                sortingItem = observableSortingInject,
                openFilter = observableOpenFilter,
                filterLabel = resources.filterAssignedToMeLabel(),
            )
            homeFilter[HomeFilter.ASSIGNED_TO_ME] = assignToMeFilter
        }
        return homeFilter
    }

    private fun webAppIsNotConfigured(): Boolean {
        return !d2.settingModule().appearanceSettings().blockingExists()
    }

    private fun getTrackerFilters(program: Program): List<FilterItem> {
        val defaultFilters = createGetDefaultTrackerFilter(program)

        if (webAppIsNotConfigured()) {
            if (orgUnitsCount == 1) {
                defaultFilters.remove(ProgramFilter.ORG_UNIT)
            }
            return defaultFilters.values.toList()
        }

        val trackerFiltersWebApp =
            d2.settingModule().appearanceSettings().getProgramFiltersByUid(program.uid())
                ?.toMutableMap() ?: mutableMapOf()

        if (orgUnitsCount == 1) {
            trackerFiltersWebApp.remove(ProgramFilter.ORG_UNIT)
        }

        val filterPreList =
            getFiltersApplyingWebAppConfig.execute(defaultFilters, trackerFiltersWebApp)

        if (filterPreList.isEmpty()) {
            return mutableListOf()
        }

        return setupUpFollowUpFilter(program, filterPreList.toMutableList())
    }

    private fun setupUpFollowUpFilter(
        program: Program,
        filtersToShow: MutableList<FilterItem>,
    ): List<FilterItem> {
        val teTypeName = d2.trackedEntityModule()
            .trackedEntityTypes()
            .uid(program.trackedEntityType()?.uid())
            .blockingGet()
            ?.displayName() ?: ""
        val followUpFilter = FollowUpFilter(
            ProgramType.TRACKER,
            observableSortingInject,
            observableOpenFilter,
            resources.filterFollowUpLabel(teTypeName),
        )

        if (filtersToShow.any { it.type == Filters.ASSIGNED_TO_ME }) {
            val index = filtersToShow.indexOfFirst { it.type == Filters.ASSIGNED_TO_ME }
            filtersToShow.add(index, followUpFilter)
        } else {
            filtersToShow.add(followUpFilter)
        }
        return filtersToShow.toList()
    }

    private fun createGetDefaultTrackerFilter(
        program: Program,
    ): LinkedHashMap<ProgramFilter, FilterItem> {
        val defaultTrackerFilters = linkedMapOf<ProgramFilter, FilterItem>()

        defaultTrackerFilters[ProgramFilter.EVENT_DATE] = PeriodFilter(
            ProgramType.TRACKER,
            observableSortingInject,
            observableOpenFilter,
            resources.filterEventDateLabel(),
        )
        defaultTrackerFilters[ProgramFilter.ENROLLMENT_DATE] = EnrollmentDateFilter(
            ProgramType.TRACKER,
            observableSortingInject,
            observableOpenFilter,
            program.enrollmentDateLabel() ?: resources
                .filterEnrollmentDateLabel(program.uid()),
        )
        defaultTrackerFilters[ProgramFilter.ORG_UNIT] = OrgUnitFilter(
            FilterManager.getInstance().observeOrgUnitFilters(),
            ProgramType.TRACKER,
            observableSortingInject,
            observableOpenFilter,
            resources.filterOrgUnitLabel(),
        )
        defaultTrackerFilters[ProgramFilter.SYNC_STATUS] = SyncStateFilter(
            ProgramType.TRACKER,
            observableSortingInject,
            observableOpenFilter,
            resources.filterSyncLabel(),
        )
        defaultTrackerFilters[ProgramFilter.ENROLLMENT_STATUS] = EnrollmentStatusFilter(
            ProgramType.TRACKER,
            observableSortingInject,
            observableOpenFilter,
            resources.filterEnrollmentStatusLabel(program.uid()),
        )
        defaultTrackerFilters[ProgramFilter.EVENT_STATUS] = EventStatusFilter(
            ProgramType.TRACKER,
            observableSortingInject,
            observableOpenFilter,
            resources.filterEventStatusLabel(),
        )

        val stagesByProgramUidAndUserAssignment = d2.programModule()
            .programStages()
            .byProgramUid()
            .eq(program.uid())
            .byEnableUserAssignment()
            .eq(true)

        if (!stagesByProgramUidAndUserAssignment.blockingIsEmpty()) {
            defaultTrackerFilters[ProgramFilter.ASSIGNED_TO_ME] = AssignedFilter(
                programType = ProgramType.TRACKER,
                sortingItem = observableSortingInject,
                openFilter = observableOpenFilter,
                filterLabel = resources.filterAssignedToMeLabel(),
            )
        }

        return defaultTrackerFilters
    }

    private fun getTrackerWorkingList(program: Program): WorkingListFilter? {
        val workingLists = d2.trackedEntityModule().trackedEntityInstanceFilters()
            .byProgram().eq(program.uid())
            .withTrackedEntityInstanceEventFilters()
            .blockingGet()
            .mapNotNull { teiFilterToWorkingListItemMapper.map(it) }
            .toMutableList()

        workingLists.addAll(
            d2.programModule().programStageWorkingLists()
                .byProgram().eq(program.uid())
                .withAttributeValueFilters()
                .blockingGet()
                .mapNotNull { programStageToWorkingListItemMapper.map(it) }
                .toMutableList(),
        )

        var workingListFilter: WorkingListFilter? = null
        if (workingLists.isNotEmpty()) {
            workingListFilter = WorkingListFilter(
                workingLists,
                ProgramType.TRACKER,
                observableSortingInject,
                observableOpenFilter,
                "",
            )
        }
        return workingListFilter
    }

    private fun getEventFilters(program: Program, programType: ProgramType): List<FilterItem> {
        val defaultFilters = createDefaultGetEventFilters(program, programType)
        if (webAppIsNotConfigured()) {
            if (orgUnitsCount == 1) {
                defaultFilters.remove(ProgramFilter.ORG_UNIT)
            }
            return defaultFilters.values.toMutableList()
        }

        val eventFiltersWebApp =
            d2.settingModule().appearanceSettings().getProgramFiltersByUid(program.uid())
                ?.toMutableMap() ?: mutableMapOf()

        if (orgUnitsCount == 1) {
            eventFiltersWebApp.remove(ProgramFilter.ORG_UNIT)
        }

        val filtersToShow =
            getFiltersApplyingWebAppConfig.execute(defaultFilters, eventFiltersWebApp)

        if (filtersToShow.isEmpty()) {
            return mutableListOf()
        }

        return filtersToShow.toList()
    }

    private fun getEventWorkingList(program: Program): WorkingListFilter? {
        var workingListFilter: WorkingListFilter? = null
        val workingLists = d2.eventModule().eventFilters()
            .byProgram().eq(program.uid())
            .blockingGet()
            .mapNotNull { eventFilterToWorkingListItemMapper.map(it) }

        if (workingLists.isNotEmpty()) {
            workingListFilter = WorkingListFilter(
                workingLists,
                ProgramType.EVENT,
                observableSortingInject,
                observableOpenFilter,
                "",
            )
        }
        return workingListFilter
    }

    private fun createDefaultGetEventFilters(
        program: Program,
        programType: ProgramType,
    ): LinkedHashMap<ProgramFilter, FilterItem> {
        val defaultEventFilter = linkedMapOf<ProgramFilter, FilterItem>()

        defaultEventFilter[ProgramFilter.EVENT_DATE] = PeriodFilter(
            programType,
            observableSortingInject,
            observableOpenFilter,
            resources.filterDateLabel(),
        )

        defaultEventFilter[ProgramFilter.ORG_UNIT] = OrgUnitFilter(
            FilterManager.getInstance().observeOrgUnitFilters(),
            programType,
            observableSortingInject,
            observableOpenFilter,
            resources.filterOrgUnitLabel(),
        )

        defaultEventFilter[ProgramFilter.SYNC_STATUS] = SyncStateFilter(
            programType,
            observableSortingInject,
            observableOpenFilter,
            resources.filterSyncLabel(),
        )

        defaultEventFilter[ProgramFilter.EVENT_STATUS] = EventStatusFilter(
            programType,
            observableSortingInject,
            observableOpenFilter,
            resources.filterEventStatusLabel(),
        )

        val stagesByProgramAndUserAssignment = d2.programModule()
            .programStages()
            .byProgramUid()
            .eq(program.uid())
            .byEnableUserAssignment()
            .eq(true)

        if (!stagesByProgramAndUserAssignment.blockingIsEmpty()) {
            defaultEventFilter[ProgramFilter.ASSIGNED_TO_ME] = AssignedFilter(
                programType = programType,
                sortingItem = observableSortingInject,
                openFilter = observableOpenFilter,
                filterLabel = resources.filterAssignedToMeLabel(),
            )
        }
        val categoryCombo =
            d2.categoryModule().categoryCombos().uid(program.categoryComboUid()).blockingGet()
        if (categoryCombo?.isDefault == false) {
            defaultEventFilter[ProgramFilter.CAT_COMBO] = CatOptionComboFilter(
                categoryCombo,
                d2.categoryModule().categoryOptionCombos().byCategoryComboUid()
                    .eq(categoryCombo.uid()).blockingGet(),
                programType,
                observableSortingInject,
                observableOpenFilter,
                categoryCombo.displayName() ?: "",
            )
        }
        return defaultEventFilter
    }

    fun applyWorkingList(
        teiQuery: TrackedEntitySearchCollectionRepository,
        currentWorkingList: WorkingListItem?,
    ): TrackedEntitySearchCollectionRepository {
        return currentWorkingList?.let {
            when (it) {
                is EventWorkingList ->
                    null
                is ProgramStageWorkingList ->
                    teiQuery.byProgramStageWorkingList().eq(it.uid)
                is TrackedEntityInstanceWorkingList ->
                    teiQuery.byTrackedEntityInstanceFilter().eq(it.uid)
            }
        } ?: teiQuery
    }

    fun applyWorkingList(
        eventQuery: EventQueryCollectionRepository,
        currentWorkingList: WorkingListItem?,
    ): EventQueryCollectionRepository {
        return currentWorkingList?.let {
            eventQuery.byEventFilter().eq(it.uid)
        } ?: eventQuery
    }

    fun collapseAllFilters() {
        observableOpenFilter.set(Filters.NON)
    }
}
