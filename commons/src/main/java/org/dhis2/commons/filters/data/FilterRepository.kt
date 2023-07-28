package org.dhis2.commons.filters.data

import androidx.databinding.ObservableField
import java.util.Calendar
import javax.inject.Inject
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
import org.hisp.dhis.android.core.program.ProgramType
import org.hisp.dhis.android.core.settings.DataSetFilter
import org.hisp.dhis.android.core.settings.HomeFilter
import org.hisp.dhis.android.core.settings.ProgramFilter
import org.hisp.dhis.android.core.trackedentity.search.TrackedEntityInstanceQueryCollectionRepository
import timber.log.Timber

data class TextFilter(val dataElement: String, val text: String)

class FilterRepository @Inject constructor(
    private val d2: D2,
    val resources: FilterResources,
    private val getFiltersApplyingWebAppConfig: GetFiltersApplyingWebAppConfig,
    private val eventFilterToWorkingListItemMapper: EventFilterToWorkingListItemMapper,
    private val teiFilterToWorkingListItemMapper: TeiFilterToWorkingListItemMapper,
    private val programStageToWorkingListItemMapper: ProgramStageToWorkingListItemMapper
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
        repository: EventQueryCollectionRepository,
        eventStatuses: List<EventStatus>
    ): EventQueryCollectionRepository {
        return repository.byStatus().`in`(eventStatuses)
    }

    fun applyCategoryOptionComboFilter(
        repository: EventQueryCollectionRepository,
        categoryOptionCombos: List<CategoryOptionCombo>
    ): EventQueryCollectionRepository {
        return repository.byAttributeOptionCombo().`in`(
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
        return repository.byEventDate().inDatePeriod(datePeriod)
            .byEventStatus().`in`(EventStatus.values().toMutableList())
    }

    fun applyEnrollmentDateFilter(
        repository: TrackedEntityInstanceQueryCollectionRepository,
        datePeriod: DatePeriod
    ): TrackedEntityInstanceQueryCollectionRepository {
        return repository.byProgramDate().inDatePeriod(datePeriod)
    }

    fun applyAssignToMe(
        repository: TrackedEntityInstanceQueryCollectionRepository
    ): TrackedEntityInstanceQueryCollectionRepository {
        return repository.byAssignedUserMode().eq(AssignedUserMode.CURRENT)
    }

    fun applyFollowUp(
        repository: TrackedEntityInstanceQueryCollectionRepository
    ): TrackedEntityInstanceQueryCollectionRepository {
        return repository.byFollowUp().isTrue
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

    fun eventsByProgram(programUid: String): EventQueryCollectionRepository {
        return d2.eventModule()
            .eventQuery()
            .byIncludeDeleted().eq(false)
            .byProgram().eq(programUid)
    }

    fun eventsByProgramAndTextFilter(
        programUid: String,
        textFilter: TextFilter?
    ): EventQueryCollectionRepository {

        if (textFilter != null && textFilter.dataElement.isNotBlank() && textFilter.text.isNotBlank()) {
            val uidsByTextFilter =
                getEventUIdsFilteredByValue(textFilter.dataElement, textFilter.text)

            return d2.eventModule()
                .eventQuery()
                .byIncludeDeleted()
                .eq(false)
                .byProgram()
                .eq(programUid)
                .byUid().`in`(if (uidsByTextFilter.isNotEmpty()) uidsByTextFilter else listOf(textFilter.text))
        } else {
            return d2.eventModule()
                .eventQuery()
                .byIncludeDeleted()
                .eq(false)
                .byProgram()
                .eq(programUid)
        }
    }

    fun applyOrgUnitFilter(
        repository: EventQueryCollectionRepository,
        orgUnitUis: List<String>
    ): EventQueryCollectionRepository {
        return repository.byOrgUnits().`in`(orgUnitUis)
    }

    fun applyStateFilter(
        repository: EventQueryCollectionRepository,
        states: List<State>
    ): EventQueryCollectionRepository {
        return repository.byStates().`in`(states)
    }

    fun applyDateFilter(
        repository: EventQueryCollectionRepository,
        datePeriod: DatePeriod
    ): EventQueryCollectionRepository {
        return repository.byEventDate().inDatePeriod(datePeriod)
    }

    fun applyAssignToMe(
        repository: EventQueryCollectionRepository
    ): EventQueryCollectionRepository {
        return repository.byAssignedUser().eq(AssignedUserMode.CURRENT)
    }

    fun sortByEventDate(
        repository: EventQueryCollectionRepository,
        orderDirection: RepositoryScope.OrderByDirection
    ): EventQueryCollectionRepository {
        return repository.orderByEventDate().eq(orderDirection)
    }

    fun sortByOrgUnit(
        repository: EventQueryCollectionRepository,
        orderDirection: RepositoryScope.OrderByDirection
    ): EventQueryCollectionRepository {
        return repository.orderByOrganisationUnitName().eq(orderDirection)
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

    fun orgUnitsByName(name: String): List<OrganisationUnit> = d2.organisationUnitModule()
        .organisationUnits()
        .byDisplayName().like("%$name%")
        .blockingGet()

    fun programFilters(programUid: String): List<FilterItem> {
        return d2.programModule().programs().uid(programUid).get()
            .map {
                if (it.programType() == ProgramType.WITH_REGISTRATION) {
                    getTrackerFilters(it, true)
                } else {
                    getEventFilters(it)
                }
            }.blockingGet()
    }

    fun dashboardFilters(programUid: String): List<FilterItem> {
        return d2.programModule().programs().uid(programUid).get()
            .map {
                if (it.programType() == ProgramType.WITH_REGISTRATION) {
                    getTrackerFilters(it, false)
                } else {
                    getEventFilters(it)
                }
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
            d2.settingModule().appearanceSettings().trackedEntityTypeFilters
        globalTrackedEntityTypeFiltersWebApp.remove(ProgramFilter.ASSIGNED_TO_ME)
        globalTrackedEntityTypeFiltersWebApp.remove(ProgramFilter.ENROLLMENT_DATE)

        if (orgUnitsCount == 1) {
            globalTrackedEntityTypeFiltersWebApp.remove(ProgramFilter.ORG_UNIT)
        }

        return getFiltersApplyingWebAppConfig.execute(
            defaultFilters,
            globalTrackedEntityTypeFiltersWebApp
        )
    }

    private fun createDefaultTrackedEntityFilters(): LinkedHashMap<ProgramFilter, FilterItem> {
        return linkedMapOf(
            ProgramFilter.ORG_UNIT to OrgUnitFilter(
                FilterManager.getInstance().observeOrgUnitFilters(),
                org.dhis2.commons.filters.ProgramType.TRACKER,
                observableSortingInject,
                observableOpenFilter,
                resources.filterOrgUnitLabel()
            ),
            ProgramFilter.SYNC_STATUS to SyncStateFilter(
                org.dhis2.commons.filters.ProgramType.TRACKER,
                observableSortingInject,
                observableOpenFilter,
                resources.filterSyncLabel()
            )
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

        if (orgUnitsCount == 1) {
            datasetFiltersWebApp.remove(DataSetFilter.ORG_UNIT)
        }

        return getFiltersApplyingWebAppConfig.execute(defaultFilters, datasetFiltersWebApp)
    }

    private fun createDefaultDatasetFilters(
        dataSetUid: String
    ): LinkedHashMap<DataSetFilter, FilterItem> {
        val datasetFilters = linkedMapOf(
            DataSetFilter.PERIOD to PeriodFilter(
                org.dhis2.commons.filters.ProgramType.DATASET,
                observableSortingInject,
                observableOpenFilter,
                resources.filterPeriodLabel()
            ),
            DataSetFilter.ORG_UNIT to OrgUnitFilter(
                FilterManager.getInstance().observeOrgUnitFilters(),
                org.dhis2.commons.filters.ProgramType.DATASET,
                observableSortingInject,
                observableOpenFilter,
                resources.filterOrgUnitLabel()
            ),
            DataSetFilter.SYNC_STATUS to SyncStateFilter(
                org.dhis2.commons.filters.ProgramType.DATASET,
                observableSortingInject,
                observableOpenFilter,
                resources.filterSyncLabel()
            )
        )

        val dataSet = d2.dataSetModule().dataSets().uid(dataSetUid).blockingGet()
        val categoryCombo =
            d2.categoryModule().categoryCombos().uid(dataSet.categoryCombo()?.uid())
                .blockingGet()
        if (categoryCombo.isDefault == false) {
            CatOptionComboFilter(
                categoryCombo,
                d2.categoryModule().categoryOptionCombos().byCategoryComboUid()
                    .eq(categoryCombo.uid()).blockingGet(),
                org.dhis2.commons.filters.ProgramType.DATASET,
                observableSortingInject,
                observableOpenFilter,
                categoryCombo.displayName() ?: ""
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

        val homeFiltersWebApp = d2.settingModule().appearanceSettings().homeFilters

        if (orgUnitsCount == 1) {
            homeFiltersWebApp.remove(HomeFilter.ORG_UNIT)
        }

        return getFiltersApplyingWebAppConfig.execute(defaultFilters, homeFiltersWebApp)
    }

    private fun createDefaultHomeFilters(): LinkedHashMap<HomeFilter, FilterItem> {
        val homeFilter = linkedMapOf(
            HomeFilter.DATE to PeriodFilter(
                org.dhis2.commons.filters.ProgramType.ALL,
                observableSortingInject,
                observableOpenFilter,
                resources.filterDateLabel()
            ),
            HomeFilter.ORG_UNIT to OrgUnitFilter(
                FilterManager.getInstance().observeOrgUnitFilters(),
                org.dhis2.commons.filters.ProgramType.ALL,
                observableSortingInject,
                observableOpenFilter,
                resources.filterOrgUnitLabel()
            ),
            HomeFilter.SYNC_STATUS to SyncStateFilter(
                org.dhis2.commons.filters.ProgramType.ALL,
                observableSortingInject,
                observableOpenFilter,
                resources.filterSyncLabel()
            )
        )

        val stagesByUserAssignment = d2.programModule()
            .programStages()
            .byEnableUserAssignment()
            .eq(true)

        if (!stagesByUserAssignment.blockingIsEmpty()) {
            val assignToMeFilter = AssignedFilter(
                programType = org.dhis2.commons.filters.ProgramType.ALL,
                sortingItem = observableSortingInject,
                openFilter = observableOpenFilter,
                filterLabel = resources.filterAssignedToMeLabel()
            )
            homeFilter[HomeFilter.ASSIGNED_TO_ME] = assignToMeFilter
        }
        return homeFilter
    }

    private fun webAppIsNotConfigured(): Boolean {
        return !d2.settingModule().appearanceSettings().blockingExists()
    }

    private fun getTrackerFilters(program: Program, showWorkingLists: Boolean): List<FilterItem> {
        val defaultFilters = createGetDefaultTrackerFilter(program)

        if (webAppIsNotConfigured()) {
            val workingListFilter = getTrackerWorkingList(program, showWorkingLists)
            if (orgUnitsCount == 1) {
                defaultFilters.remove(ProgramFilter.ORG_UNIT)
            }
            if (workingListFilter != null) {
                return defaultFilters.values.toMutableList().apply {
                    add(0, workingListFilter)
                }
            }
            return defaultFilters.values.toList()
        }

        val trackerFiltersWebApp =
            d2.settingModule().appearanceSettings().getProgramFiltersByUid(program.uid())

        if (orgUnitsCount == 1) {
            trackerFiltersWebApp.remove(ProgramFilter.ORG_UNIT)
        }

        val filterPreList =
            getFiltersApplyingWebAppConfig.execute(defaultFilters, trackerFiltersWebApp)
        val workingListFilter = getTrackerWorkingList(program, showWorkingLists)

        if (filterPreList.isEmpty() && workingListFilter == null) {
            return mutableListOf()
        }

        val filtersToShow = setupUpFollowUpFilter(program, filterPreList.toMutableList())

        if (workingListFilter != null) {
            return filtersToShow.toMutableList().apply {
                add(0, workingListFilter)
            }
        }
        return filtersToShow
    }

    private fun setupUpFollowUpFilter(
        program: Program,
        filtersToShow: MutableList<FilterItem>
    ): List<FilterItem> {
        val teTypeName = d2.trackedEntityModule()
            .trackedEntityTypes()
            .uid(program.trackedEntityType()?.uid())
            .blockingGet()
            .displayName() ?: ""
        val followUpFilter = FollowUpFilter(
            org.dhis2.commons.filters.ProgramType.TRACKER,
            observableSortingInject,
            observableOpenFilter,
            resources.filterFollowUpLabel(teTypeName)
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
        program: Program
    ): LinkedHashMap<ProgramFilter, FilterItem> {
        val defaultTrackerFilters = linkedMapOf<ProgramFilter, FilterItem>()

        defaultTrackerFilters[ProgramFilter.EVENT_DATE] = PeriodFilter(
            org.dhis2.commons.filters.ProgramType.TRACKER,
            observableSortingInject,
            observableOpenFilter,
            resources.filterEventDateLabel()
        )
        defaultTrackerFilters[ProgramFilter.ENROLLMENT_DATE] = EnrollmentDateFilter(
            org.dhis2.commons.filters.ProgramType.TRACKER,
            observableSortingInject,
            observableOpenFilter,
            program.enrollmentDateLabel() ?: resources
                .filterEnrollmentDateLabel()
        )
        defaultTrackerFilters[ProgramFilter.ORG_UNIT] = OrgUnitFilter(
            FilterManager.getInstance().observeOrgUnitFilters(),
            org.dhis2.commons.filters.ProgramType.TRACKER,
            observableSortingInject,
            observableOpenFilter,
            resources.filterOrgUnitLabel()
        )
        defaultTrackerFilters[ProgramFilter.SYNC_STATUS] = SyncStateFilter(
            org.dhis2.commons.filters.ProgramType.TRACKER,
            observableSortingInject,
            observableOpenFilter,
            resources.filterSyncLabel()
        )
        defaultTrackerFilters[ProgramFilter.ENROLLMENT_STATUS] = EnrollmentStatusFilter(
            org.dhis2.commons.filters.ProgramType.TRACKER,
            observableSortingInject,
            observableOpenFilter,
            resources.filterEnrollmentStatusLabel()
        )
        defaultTrackerFilters[ProgramFilter.EVENT_STATUS] = EventStatusFilter(
            org.dhis2.commons.filters.ProgramType.TRACKER,
            observableSortingInject,
            observableOpenFilter,
            resources.filterEventStatusLabel()
        )

        val stagesByProgramUidAndUserAssignment = d2.programModule()
            .programStages()
            .byProgramUid()
            .eq(program.uid())
            .byEnableUserAssignment()
            .eq(true)

        if (!stagesByProgramUidAndUserAssignment.blockingIsEmpty()) {
            defaultTrackerFilters[ProgramFilter.ASSIGNED_TO_ME] = AssignedFilter(
                programType = org.dhis2.commons.filters.ProgramType.TRACKER,
                sortingItem = observableSortingInject,
                openFilter = observableOpenFilter,
                filterLabel = resources.filterAssignedToMeLabel()
            )
        }

        return defaultTrackerFilters
    }

    private fun getTrackerWorkingList(
        program: Program,
        showWorkingLists: Boolean
    ): WorkingListFilter? {
        if (!showWorkingLists) {
            return null
        }
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
                .toMutableList()
        )

        var workingListFilter: WorkingListFilter? = null
        if (workingLists.isNotEmpty()) {
            workingListFilter = WorkingListFilter(
                workingLists,
                org.dhis2.commons.filters.ProgramType.TRACKER,
                observableSortingInject,
                observableOpenFilter,
                ""
            )
        }
        return workingListFilter
    }

    private fun getEventFilters(program: Program): List<FilterItem> {
        val defaultFilters = createDefaultGetEventFilters(program)

        if (webAppIsNotConfigured()) {
            val workingListFilter = getEventWorkingList(program)
            if (orgUnitsCount == 1) {
                defaultFilters.remove(ProgramFilter.ORG_UNIT)
            }
            if (workingListFilter != null) {
                return defaultFilters.values.toMutableList().apply {
                    add(0, workingListFilter)
                }
            }
            return defaultFilters.values.toMutableList()
        }

        val eventFiltersWebApp =
            d2.settingModule().appearanceSettings().getProgramFiltersByUid(program.uid())

        if (orgUnitsCount == 1) {
            eventFiltersWebApp.remove(ProgramFilter.ORG_UNIT)
        }

        val filtersToShow =
            getFiltersApplyingWebAppConfig.execute(defaultFilters, eventFiltersWebApp)
        val workingListFilter: WorkingListFilter? = getEventWorkingList(program)

        if (filtersToShow.isEmpty() && workingListFilter == null) {
            return mutableListOf()
        }

        if (workingListFilter != null) {
            return filtersToShow.toMutableList().apply {
                add(0, workingListFilter)
            }
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
                org.dhis2.commons.filters.ProgramType.EVENT,
                observableSortingInject,
                observableOpenFilter,
                ""
            )
        }
        return workingListFilter
    }

    private fun createDefaultGetEventFilters(
        program: Program
    ): LinkedHashMap<ProgramFilter, FilterItem> {
        val defaultEventFilter = linkedMapOf<ProgramFilter, FilterItem>()

        defaultEventFilter[ProgramFilter.EVENT_DATE] = PeriodFilter(
            org.dhis2.commons.filters.ProgramType.EVENT,
            observableSortingInject,
            observableOpenFilter,
            resources.filterDateLabel()
        )

        defaultEventFilter[ProgramFilter.ORG_UNIT] = OrgUnitFilter(
            FilterManager.getInstance().observeOrgUnitFilters(),
            org.dhis2.commons.filters.ProgramType.EVENT,
            observableSortingInject,
            observableOpenFilter,
            resources.filterOrgUnitLabel()
        )

        defaultEventFilter[ProgramFilter.SYNC_STATUS] = SyncStateFilter(
            org.dhis2.commons.filters.ProgramType.EVENT,
            observableSortingInject,
            observableOpenFilter,
            resources.filterSyncLabel()
        )

        defaultEventFilter[ProgramFilter.EVENT_STATUS] = EventStatusFilter(
            org.dhis2.commons.filters.ProgramType.EVENT,
            observableSortingInject,
            observableOpenFilter,
            resources.filterEventStatusLabel()
        )

        val stagesByProgramAndUserAssignment = d2.programModule()
            .programStages()
            .byProgramUid()
            .eq(program.uid())
            .byEnableUserAssignment()
            .eq(true)

        if (!stagesByProgramAndUserAssignment.blockingIsEmpty()) {
            defaultEventFilter[ProgramFilter.ASSIGNED_TO_ME] = AssignedFilter(
                programType = org.dhis2.commons.filters.ProgramType.EVENT,
                sortingItem = observableSortingInject,
                openFilter = observableOpenFilter,
                filterLabel = resources.filterAssignedToMeLabel()
            )
        }
        val categoryCombo =
            d2.categoryModule().categoryCombos().uid(program.categoryComboUid()).blockingGet()
        if (categoryCombo.isDefault == false) {
            defaultEventFilter[ProgramFilter.CAT_COMBO] = CatOptionComboFilter(
                categoryCombo,
                d2.categoryModule().categoryOptionCombos().byCategoryComboUid()
                    .eq(categoryCombo.uid()).blockingGet(),
                org.dhis2.commons.filters.ProgramType.EVENT,
                observableSortingInject,
                observableOpenFilter,
                categoryCombo.displayName() ?: ""
            )
        }
        return defaultEventFilter
    }

    fun applyWorkingList(
        teiQuery: TrackedEntityInstanceQueryCollectionRepository,
        currentWorkingList: WorkingListItem?
    ): TrackedEntityInstanceQueryCollectionRepository {
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
        currentWorkingList: WorkingListItem?
    ): EventQueryCollectionRepository {
        return currentWorkingList?.let {
            eventQuery.byEventFilter().eq(it.uid)
        } ?: eventQuery
    }

    fun collapseAllFilters() {
        observableOpenFilter.set(Filters.NON)
    }

    private fun getEventUIdsFilteredByValue(dataElement: String, value: String): List<String> {
        val uids: MutableList<String> = ArrayList()
        val eventByTextValueQuery = "SELECT Event.uid FROM Event " +
            "LEFT OUTER JOIN TrackedEntityDataValue AS Value ON Value.event = Event.uid " +
            "WHERE Value.dataElement = '" + dataElement + "' AND Value.value like '%" +
            value + "%'"
        try {

            d2.databaseAdapter().rawQuery(eventByTextValueQuery).use { idsCursor ->
                if (idsCursor != null) {
                    idsCursor.moveToFirst()
                    for (i in 0 until idsCursor.count) {
                        uids.add(idsCursor.getString(0))
                        idsCursor.moveToNext()
                    }
                }
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
        return uids
    }
}
