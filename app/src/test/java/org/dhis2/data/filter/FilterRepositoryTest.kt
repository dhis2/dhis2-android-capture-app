package org.dhis2.data.filter

import androidx.databinding.ObservableField
import io.reactivex.Single
import org.dhis2.commons.filters.AssignedFilter
import org.dhis2.commons.filters.EnrollmentStatusFilter
import org.dhis2.commons.filters.EventStatusFilter
import org.dhis2.commons.filters.FilterItem
import org.dhis2.commons.filters.FilterResources
import org.dhis2.commons.filters.Filters
import org.dhis2.commons.filters.PeriodFilter
import org.dhis2.commons.filters.ProgramType
import org.dhis2.commons.filters.data.FilterRepository
import org.dhis2.commons.filters.data.GetFiltersApplyingWebAppConfig
import org.dhis2.commons.filters.sorting.SortingItem
import org.dhis2.commons.filters.workingLists.EventFilterToWorkingListItemMapper
import org.dhis2.commons.filters.workingLists.ProgramStageToWorkingListItemMapper
import org.dhis2.commons.filters.workingLists.TeiFilterToWorkingListItemMapper
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.category.CategoryCombo
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.settings.FilterSetting
import org.hisp.dhis.android.core.settings.HomeFilter
import org.hisp.dhis.android.core.settings.ProgramFilter
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceFilter
import org.hisp.dhis.android.core.trackedentity.TrackedEntityType
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class FilterRepositoryTest {

    companion object {
        const val EVENT_DATE = "date"
        const val ENROLLMENT_DATE = "enrollment_date"
        const val ORG_UNIT = "org_unit"
        const val SYNC_STATUS = "sync_status"
        const val ENROLLMENT_STATUS = "enrollment_status"
        const val EVENT_STATUS = "event_status"
        const val ASSIGN_TO_ME = "assign_to_me"
        const val HOME_FILTER = "HomeFilter"
        const val PROGRAM_FILTER = "ProgramFilter"
        const val FOLLOW_UP = "follow_up"
    }

    private val observableSortingInject = ObservableField<SortingItem>()
    private val observableOpenFilter = ObservableField<Filters>()
    private val d2: D2 = Mockito.mock(D2::class.java, Mockito.RETURNS_DEEP_STUBS)
    private val filterResources: FilterResources = mock()
    private val getFiltersApplyingWebAppConfig: GetFiltersApplyingWebAppConfig = mock()
    private val eventFilterToWorkingListItemMapper: EventFilterToWorkingListItemMapper = mock()
    private val teiFilterToWorkingListItemMapper: TeiFilterToWorkingListItemMapper =
        TeiFilterToWorkingListItemMapper("defaultLabel")
    private val programStageToWorkingListItemMapper: ProgramStageToWorkingListItemMapper = mock()
    private lateinit var filterRepository: FilterRepository

    @Before
    fun setUp() {
        mockFilterLabels()
        filterRepository = FilterRepository(
            d2,
            filterResources,
            getFiltersApplyingWebAppConfig,
            eventFilterToWorkingListItemMapper,
            teiFilterToWorkingListItemMapper,
            programStageToWorkingListItemMapper
        )
    }

    private fun mockFilterLabels() {
        whenever(filterResources.filterOrgUnitLabel()) doReturn ORG_UNIT
        whenever(filterResources.filterSyncLabel()) doReturn SYNC_STATUS
        whenever(filterResources.filterEnrollmentStatusLabel()) doReturn
            ENROLLMENT_STATUS
        whenever(filterResources.filterDateLabel()) doReturn EVENT_DATE
        whenever(filterResources.filterEventStatusLabel()) doReturn EVENT_STATUS
        whenever(filterResources.filterEnrollmentDateLabel()) doReturn
            ENROLLMENT_DATE
        whenever(filterResources.filterAssignedToMeLabel()) doReturn ASSIGN_TO_ME
        whenever(filterResources.filterEventDateLabel()) doReturn EVENT_DATE
        whenever(filterResources.filterFollowUpLabel("Name")) doReturn FOLLOW_UP
    }

    @Test
    fun `Should get home filters without assign to me when webapp is not configured`() {
        whenever(d2.settingModule().appearanceSettings().blockingExists()) doReturn false
        whenever(
            d2.programModule().programStages().byEnableUserAssignment().eq(true)
        ) doReturn mock()
        whenever(
            d2.programModule().programStages().byEnableUserAssignment()
                .eq(true).blockingIsEmpty()
        ) doReturn true

        val result = filterRepository.homeFilters()

        assert(result[0].type == Filters.PERIOD)
        assert(result[1].type == Filters.ORG_UNIT)
        assert(result[2].type == Filters.SYNC_STATE)
        assert(result.size == 3)
    }

    @Test
    fun `Should get home filters with assign to me when webapp is not configured`() {
        whenever(d2.settingModule().appearanceSettings().blockingExists()) doReturn false
        whenever(
            d2.programModule().programStages().byEnableUserAssignment().eq(true)
        ) doReturn mock()
        whenever(
            d2.programModule().programStages().byEnableUserAssignment()
                .eq(true).blockingIsEmpty()
        ) doReturn false

        val result = filterRepository.homeFilters()
        assert(result[0].type == Filters.PERIOD)
        assert(result[1].type == Filters.ORG_UNIT)
        assert(result[2].type == Filters.SYNC_STATE)
        assert(result[3].type == Filters.ASSIGNED_TO_ME)
        assert(result.size == 4)
    }

    @Test
    fun `Should get home filters when webapp is configured`() {
        whenever(d2.settingModule().appearanceSettings().blockingExists()) doReturn true
        whenever(
            d2.programModule().programStages().byEnableUserAssignment().eq(true)
        ) doReturn mock()
        whenever(
            d2.programModule().programStages().byEnableUserAssignment()
                .eq(true).blockingIsEmpty()
        ) doReturn false
        whenever(
            d2.settingModule().appearanceSettings().homeFilters
        ) doReturn createWebAppHomeFilters()
        whenever(
            getFiltersApplyingWebAppConfig.execute(
                any<LinkedHashMap<HomeFilter, FilterItem>>(),
                any<Map<HomeFilter, FilterSetting>>()
            )
        ) doReturn createHomeFiltersResult()

        val result = filterRepository.homeFilters()

        assert(result[0].type == Filters.PERIOD)
        assert(result[1].type == Filters.ASSIGNED_TO_ME)
        assert(result.size == 2)
    }

    @Test
    fun `Should get global tracked entity filter when webapp is not configured`() {
        whenever(d2.settingModule().appearanceSettings().blockingExists()) doReturn false

        val result = filterRepository.globalTrackedEntityFilters()

        assert(result[0].type == Filters.ORG_UNIT)
        assert(result[1].type == Filters.SYNC_STATE)
        assert(result.size == 2)
    }

    @Test
    fun `Should get global tracked entity filter when webapp is configured`() {
        whenever(d2.settingModule().appearanceSettings().blockingExists()) doReturn true
        whenever(
            d2.settingModule().appearanceSettings().trackedEntityTypeFilters
        ) doReturn createWebAppTrackedEntityFilters()
        whenever(
            getFiltersApplyingWebAppConfig.execute(
                any<LinkedHashMap<ProgramFilter, FilterItem>>(),
                any<Map<ProgramFilter, FilterSetting>>()
            )
        ) doReturn createGlobalTrackerFilterResult()

        val result = filterRepository.globalTrackedEntityFilters()

        assert(result[0].type == Filters.PERIOD)
        assert(result[1].type == Filters.ENROLLMENT_STATUS)
        assert(result.size == 2)
    }

    @Test
    fun `Should get dashboard filters when webapp is not configured`() {
        val program = Program.builder().uid("random")
            .programType(org.hisp.dhis.android.core.program.ProgramType.WITH_REGISTRATION).build()
        val catCombo = CategoryCombo.builder().uid("catCom").isDefault(true).build()
        whenever(d2.settingModule().appearanceSettings().blockingExists()) doReturn false
        whenever(d2.programModule().programs().uid(any())) doReturn mock()
        whenever(d2.programModule().programs().uid(any()).get()) doReturn Single.just(program)
        whenever(
            d2.programModule().programStages().byProgramUid().eq(program.uid())
        ) doReturn mock()
        whenever(
            d2.programModule().programStages().byProgramUid().eq(program.uid())
                .byEnableUserAssignment()
        ) doReturn mock()
        whenever(
            d2.programModule().programStages().byProgramUid().eq(program.uid())
                .byEnableUserAssignment().eq(true)
        ) doReturn mock()
        whenever(
            d2.programModule().programStages().byProgramUid().eq(program.uid())
                .byEnableUserAssignment().eq(true).blockingIsEmpty()
        ) doReturn false
        whenever(
            d2.categoryModule().categoryCombos().uid(program.categoryComboUid()).blockingGet()
        ) doReturn catCombo

        val result = filterRepository.dashboardFilters(program.uid())

        assert(result[0].type == Filters.PERIOD)
        assert(result[1].type == Filters.ORG_UNIT)
        assert(result[2].type == Filters.SYNC_STATE)
        assert(result[3].type == Filters.EVENT_STATUS)
        assert(result[4].type == Filters.ASSIGNED_TO_ME)
        assert(result.size == 5)
    }

    @Test
    fun `Should get dashboard filters with catOptCombo when webapp is not configured`() {
        val program = Program.builder().uid("random")
            .programType(org.hisp.dhis.android.core.program.ProgramType.WITH_REGISTRATION).build()
        val catCombo = CategoryCombo.builder().uid("catCom").isDefault(false).build()
        whenever(d2.settingModule().appearanceSettings().blockingExists()) doReturn false
        whenever(d2.programModule().programs().uid(any())) doReturn mock()
        whenever(d2.programModule().programs().uid(any()).get()) doReturn Single.just(program)
        whenever(
            d2.programModule().programStages().byProgramUid().eq(program.uid())
        ) doReturn mock()
        whenever(
            d2.programModule().programStages().byProgramUid().eq(program.uid())
                .byEnableUserAssignment()
        ) doReturn mock()
        whenever(
            d2.programModule().programStages().byProgramUid().eq(program.uid())
                .byEnableUserAssignment().eq(true)
        ) doReturn mock()
        whenever(
            d2.programModule().programStages().byProgramUid().eq(program.uid())
                .byEnableUserAssignment().eq(true).blockingIsEmpty()
        ) doReturn false
        whenever(
            d2.categoryModule().categoryCombos().uid(program.categoryComboUid()).blockingGet()
        ) doReturn catCombo
        whenever(
            d2.categoryModule().categoryOptionCombos().byCategoryComboUid()
        ) doReturn mock()
        whenever(
            d2.categoryModule().categoryOptionCombos().byCategoryComboUid().eq(catCombo.uid())
        ) doReturn mock()
        whenever(
            d2.categoryModule().categoryOptionCombos().byCategoryComboUid().eq(catCombo.uid())
                .blockingGet()
        ) doReturn emptyList()

        val result = filterRepository.dashboardFilters(program.uid())

        assert(result[0].type == Filters.PERIOD)
        assert(result[1].type == Filters.ORG_UNIT)
        assert(result[2].type == Filters.SYNC_STATE)
        assert(result[3].type == Filters.EVENT_STATUS)
        assert(result[4].type == Filters.ASSIGNED_TO_ME)
        assert(result[5].type == Filters.CAT_OPT_COMB)
        assert(result.size == 6)
    }

    @Test
    fun `Should get dashboard filters when webapp is configured with empty result`() {
        val program = Program.builder().uid("random")
            .programType(org.hisp.dhis.android.core.program.ProgramType.WITH_REGISTRATION).build()
        val catCombo = CategoryCombo.builder().uid("catCom").isDefault(true).build()
        whenever(d2.programModule().programs().uid(any())) doReturn mock()
        whenever(d2.programModule().programs().uid(any()).get()) doReturn Single.just(program)
        whenever(d2.settingModule().appearanceSettings().blockingExists()) doReturn true
        whenever(
            d2.programModule().programStages().byProgramUid().eq(program.uid())
        ) doReturn mock()
        whenever(
            d2.programModule().programStages().byProgramUid().eq(program.uid())
                .byEnableUserAssignment()
        ) doReturn mock()
        whenever(
            d2.programModule().programStages().byProgramUid().eq(program.uid())
                .byEnableUserAssignment().eq(true)
        ) doReturn mock()
        whenever(
            d2.programModule().programStages().byProgramUid().eq(program.uid())
                .byEnableUserAssignment().eq(true).blockingIsEmpty()
        ) doReturn false
        whenever(
            d2.settingModule().appearanceSettings().getProgramFiltersByUid(program.uid())
        ) doReturn emptyMap()
        whenever(
            getFiltersApplyingWebAppConfig.execute(
                any<LinkedHashMap<ProgramFilter, FilterItem>>(),
                any<Map<ProgramFilter, FilterSetting>>()
            )
        ) doReturn emptyList()
        whenever(
            d2.categoryModule().categoryCombos().uid(program.categoryComboUid()).blockingGet()
        ) doReturn catCombo

        val result = filterRepository.dashboardFilters(program.uid())

        assert(result.isEmpty())
    }

    @Test
    fun `Should get tracker filters with working list when webapp is configured`() {
        val program = Program.builder().uid("random")
            .programType(org.hisp.dhis.android.core.program.ProgramType.WITH_REGISTRATION)
            .trackedEntityType(
                TrackedEntityType.builder().uid("trackerUid").displayName("Name").build()
            )
            .build()
        whenever(d2.programModule().programs().uid(any())) doReturn mock()
        whenever(d2.programModule().programs().uid(any()).get()) doReturn Single.just(program)
        whenever(d2.settingModule().appearanceSettings().blockingExists()) doReturn true
        whenever(
            d2.programModule().programStages().byProgramUid().eq(program.uid())
        ) doReturn mock()
        whenever(
            d2.programModule().programStages().byProgramUid().eq(program.uid())
                .byEnableUserAssignment()
        ) doReturn mock()
        whenever(
            d2.programModule().programStages().byProgramUid().eq(program.uid())
                .byEnableUserAssignment().eq(true)
        ) doReturn mock()
        whenever(
            d2.programModule().programStages().byProgramUid().eq(program.uid())
                .byEnableUserAssignment().eq(true).blockingIsEmpty()
        ) doReturn false
        whenever(
            d2.trackedEntityModule().trackedEntityInstanceFilters().byProgram().eq(program.uid())
        ) doReturn mock()
        whenever(
            d2.trackedEntityModule().trackedEntityInstanceFilters().byProgram().eq(program.uid())
                .withTrackedEntityInstanceEventFilters()
        ) doReturn mock()
        whenever(
            d2.trackedEntityModule().trackedEntityInstanceFilters().byProgram().eq(program.uid())
                .withTrackedEntityInstanceEventFilters().blockingGet()
        ) doReturn createTrackerEntityInstanceFilters()
        whenever(
            d2.settingModule().appearanceSettings().getProgramFiltersByUid(program.uid())
        ) doReturn createWebAppTrackedEntityFilters()
        whenever(
            getFiltersApplyingWebAppConfig.execute(
                any<LinkedHashMap<ProgramFilter, FilterItem>>(),
                any<Map<ProgramFilter, FilterSetting>>()
            )
        ) doReturn createGlobalTrackerFilterResult()
        whenever(
            d2.trackedEntityModule().trackedEntityTypes()
        ) doReturn mock()
        whenever(
            d2.trackedEntityModule().trackedEntityTypes().uid(program.trackedEntityType()?.uid())
        ) doReturn mock()
        whenever(
            d2.trackedEntityModule().trackedEntityTypes().uid(program.trackedEntityType()?.uid())
                .blockingGet()
        ) doReturn mock()
        whenever(
            d2.trackedEntityModule().trackedEntityTypes().uid(program.trackedEntityType()?.uid())
                .blockingGet().displayName()
        ) doReturn program.trackedEntityType()?.displayName()
        whenever(
            d2.programModule().programStageWorkingLists().byProgram().eq(program.uid())
        ) doReturn mock()
        whenever(
            d2.programModule().programStageWorkingLists().byProgram().eq(program.uid())
                .withAttributeValueFilters()
        ) doReturn mock()
        whenever(
            d2.programModule().programStageWorkingLists().byProgram().eq(program.uid())
                .withAttributeValueFilters().blockingGet()
        ) doReturn emptyList()

        val result = filterRepository.programFilters(program.uid())

        assert(result[0].type == Filters.WORKING_LIST)
        assert(result[1].type == Filters.PERIOD)
        assert(result[2].type == Filters.ENROLLMENT_STATUS)
        assert(result[3].type == Filters.FOLLOW_UP)
        assert(result.size == 4)
    }

    @Test
    fun `Should get default tracker filters with follow up filter`() {
        val program = Program.builder().uid("random")
            .programType(org.hisp.dhis.android.core.program.ProgramType.WITH_REGISTRATION)
            .trackedEntityType(
                TrackedEntityType.builder().uid("trackerUid").displayName("Name").build()
            )
            .build()
        whenever(d2.programModule().programs().uid(any())) doReturn mock()
        whenever(d2.programModule().programs().uid(any()).get()) doReturn Single.just(program)
        whenever(d2.settingModule().appearanceSettings().blockingExists()) doReturn true
        whenever(
            d2.programModule().programStages().byProgramUid().eq(program.uid())
        ) doReturn mock()
        whenever(
            d2.programModule().programStages().byProgramUid().eq(program.uid())
                .byEnableUserAssignment()
        ) doReturn mock()
        whenever(
            d2.programModule().programStages().byProgramUid().eq(program.uid())
                .byEnableUserAssignment().eq(true)
        ) doReturn mock()
        whenever(
            d2.programModule().programStages().byProgramUid().eq(program.uid())
                .byEnableUserAssignment().eq(true).blockingIsEmpty()
        ) doReturn false
        whenever(
            d2.trackedEntityModule().trackedEntityInstanceFilters().byProgram().eq(program.uid())
        ) doReturn mock()
        whenever(
            d2.trackedEntityModule().trackedEntityInstanceFilters().byProgram().eq(program.uid())
                .withTrackedEntityInstanceEventFilters()
        ) doReturn mock()
        whenever(
            d2.trackedEntityModule().trackedEntityInstanceFilters().byProgram().eq(program.uid())
                .withTrackedEntityInstanceEventFilters().blockingGet()
        ) doReturn emptyList()
        whenever(
            d2.settingModule().appearanceSettings().getProgramFiltersByUid(program.uid())
        ) doReturn emptyMap()
        whenever(
            getFiltersApplyingWebAppConfig.execute(
                any<LinkedHashMap<ProgramFilter, FilterItem>>(),
                any<Map<ProgramFilter, FilterSetting>>()
            )
        ) doReturn createDefaultTrackerFilterResult()
        whenever(
            d2.trackedEntityModule().trackedEntityTypes()
        ) doReturn mock()
        whenever(
            d2.trackedEntityModule().trackedEntityTypes().uid(program.trackedEntityType()?.uid())
        ) doReturn mock()
        whenever(
            d2.trackedEntityModule().trackedEntityTypes().uid(program.trackedEntityType()?.uid())
                .blockingGet()
        ) doReturn mock()
        whenever(
            d2.trackedEntityModule().trackedEntityTypes().uid(program.trackedEntityType()?.uid())
                .blockingGet().displayName()
        ) doReturn program.trackedEntityType()?.displayName()
        whenever(
            d2.programModule().programStageWorkingLists().byProgram().eq(program.uid())
        ) doReturn mock()
        whenever(
            d2.programModule().programStageWorkingLists().byProgram().eq(program.uid())
                .withAttributeValueFilters()
        ) doReturn mock()
        whenever(
            d2.programModule().programStageWorkingLists().byProgram().eq(program.uid())
                .withAttributeValueFilters().blockingGet()
        ) doReturn emptyList()

        val result = filterRepository.programFilters(program.uid())

        assert(result[0].type == Filters.PERIOD)
        assert(result[1].type == Filters.ENROLLMENT_STATUS)
        assert(result[2].type == Filters.EVENT_STATUS)
        assert(result[3].type == Filters.FOLLOW_UP)
        assert(result[4].type == Filters.ASSIGNED_TO_ME)
        assert(result.size == 5)
    }

    private fun createTrackerEntityInstanceFilters(): List<TrackedEntityInstanceFilter> {
        return listOf(
            TrackedEntityInstanceFilter.builder().uid("uid").displayName("random").build(),
            TrackedEntityInstanceFilter.builder().uid("uid2").displayName("random2").build()
        )
    }

    private fun createGlobalTrackerFilterResult(): List<FilterItem> {
        return listOf(
            PeriodFilter(
                ProgramType.TRACKER,
                observableSortingInject,
                observableOpenFilter,
                EVENT_DATE
            ),
            EnrollmentStatusFilter(
                ProgramType.TRACKER,
                observableSortingInject,
                observableOpenFilter,
                ENROLLMENT_STATUS
            )
        )
    }

    private fun createDefaultTrackerFilterResult(): List<FilterItem> {
        return listOf(
            PeriodFilter(
                ProgramType.TRACKER,
                observableSortingInject,
                observableOpenFilter,
                EVENT_DATE
            ),
            EnrollmentStatusFilter(
                ProgramType.TRACKER,
                observableSortingInject,
                observableOpenFilter,
                ENROLLMENT_STATUS
            ),
            EventStatusFilter(
                ProgramType.TRACKER,
                observableSortingInject,
                observableOpenFilter,
                EVENT_STATUS
            ),
            AssignedFilter(
                ProgramType.TRACKER,
                observableSortingInject,
                observableOpenFilter,
                ASSIGN_TO_ME
            )
        )
    }

    private fun createFilterValue(
        scope: String,
        filterType: String,
        show: Boolean = true
    ): FilterSetting {
        return FilterSetting.builder().filter(show).filterType(filterType).id(25)
            .scope(scope).sort(show).build()
    }

    private fun createWebAppHomeFilters(): Map<HomeFilter, FilterSetting> {
        return mapOf(
            HomeFilter.DATE to createFilterValue(HOME_FILTER, "DATE"),
            HomeFilter.ASSIGNED_TO_ME to createFilterValue(HOME_FILTER, "ASSIGNED_TO_ME"),
            HomeFilter.ORG_UNIT to createFilterValue(HOME_FILTER, "ORG_UNIT", false)
        )
    }

    private fun createWebAppTrackedEntityFilters(): Map<ProgramFilter, FilterSetting> {
        return mapOf(
            ProgramFilter.EVENT_DATE to createFilterValue(
                PROGRAM_FILTER,
                "EVENT_DATE",
                false
            ),
            ProgramFilter.SYNC_STATUS to createFilterValue(
                PROGRAM_FILTER,
                "EVE",
                false
            ),
            ProgramFilter.ORG_UNIT to createFilterValue(
                PROGRAM_FILTER,
                "ORG_UNIT",
                false
            ),
            ProgramFilter.ENROLLMENT_DATE to createFilterValue(
                PROGRAM_FILTER,
                "ENROLLMENT_DATE",
                false
            ),
            ProgramFilter.ENROLLMENT_STATUS to createFilterValue(
                PROGRAM_FILTER,
                "ENROLLMENT_DATE",
                false
            ),
            ProgramFilter.ASSIGNED_TO_ME to createFilterValue(
                PROGRAM_FILTER,
                "ENROLLMENT_DATE",
                false
            ),
            ProgramFilter.EVENT_STATUS to createFilterValue(
                PROGRAM_FILTER,
                "ENROLLMENT_DATE",
                false
            ),
            ProgramFilter.FOLLOW_UP to createFilterValue(
                PROGRAM_FILTER,
                "FOLLOW_UP",
                false
            )
        )
    }

    private fun createHomeFiltersResult(): List<FilterItem> {
        return listOf(
            PeriodFilter(
                ProgramType.ALL,
                observableSortingInject,
                observableOpenFilter,
                EVENT_DATE
            ),
            AssignedFilter(
                ProgramType.ALL,
                observableSortingInject,
                observableOpenFilter,
                ASSIGN_TO_ME
            )
        )
    }
}
