package org.dhis2.usescases.searchTrackEntity

import androidx.databinding.ObservableField
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.schedulers.TestScheduler
import org.dhis2.commons.filters.AssignedFilter
import org.dhis2.commons.filters.DisableHomeFiltersFromSettingsApp
import org.dhis2.commons.filters.FilterItem
import org.dhis2.commons.filters.FilterManager
import org.dhis2.commons.filters.Filters
import org.dhis2.commons.filters.ProgramType
import org.dhis2.commons.filters.data.FilterRepository
import org.dhis2.commons.filters.sorting.SortingItem
import org.dhis2.commons.filters.workingLists.TeiFilterToWorkingListItemMapper
import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.data.schedulers.TestSchedulerProvider
import org.dhis2.utils.analytics.AnalyticsHelper
import org.dhis2.utils.analytics.matomo.MatomoAnalyticsController
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.program.Program
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.validateMockitoUsage

class SearchTEPresenterTest {

    lateinit var presenter: SearchTEContractsModule.Presenter

    private val view: SearchTEContractsModule.View = mock()
    private val d2: D2 = Mockito.mock(D2::class.java, Mockito.RETURNS_DEEP_STUBS)
    private val repository: SearchRepository = mock()
    private val schedulers: TestSchedulerProvider = TestSchedulerProvider(TestScheduler())
    private val analyticsHelper: AnalyticsHelper = mock()
    private val initialProgram = "programUid"
    private val preferenceProvider: PreferenceProvider = mock()
    private val workingListMapper: TeiFilterToWorkingListItemMapper = mock()
    private val filterRepository: FilterRepository = mock()
    private val disableHomeFiltersFromSettingsApp: DisableHomeFiltersFromSettingsApp = mock()
    private val matomoAnalyticsController: MatomoAnalyticsController = mock()
    private val searchMessageMapper: SearchMessageMapper = mock()

    @Before
    fun setUp() {
        whenever(d2.programModule().programs().uid(initialProgram).blockingGet()) doReturn
            Program.builder().uid(
                initialProgram
            )
                .displayFrontPageList(true)
                .minAttributesRequiredToSearch(0).build()

        presenter = SearchTEPresenter(
            view,
            d2,
            repository,
            schedulers,
            analyticsHelper,
            initialProgram,
            preferenceProvider,
            workingListMapper,
            filterRepository,
            disableHomeFiltersFromSettingsApp,
            matomoAnalyticsController,
            searchMessageMapper
        )
    }

    @Test
    fun `Should ignore initial program spinner selection`() {
        val program = Program.builder()
            .uid("uid")
            .displayFrontPageList(true)
            .minAttributesRequiredToSearch(1)
            .build()

        presenter.setProgramForTesting(program)

        presenter.program = program

        verify(view, never()).clearList(program.uid())
        verify(view, never()).setFabIcon(true)
    }

    @Test
    fun `Should clear data, fab and list when another program is selected`() {
        val program = Program.builder()
            .uid("uid")
            .displayFrontPageList(true)
            .minAttributesRequiredToSearch(1)
            .build()

        val newSelectedProgram = Program.builder()
            .uid("uid2")
            .displayFrontPageList(true)
            .minAttributesRequiredToSearch(1)
            .build()

        whenever(
            d2.programModule().programStages()
                .byProgramUid().eq(newSelectedProgram.uid())
        ) doReturn mock()

        whenever(
            d2.programModule().programStages()
                .byProgramUid().eq(newSelectedProgram.uid())
                .byEnableUserAssignment()
        ) doReturn mock()

        whenever(
            d2.programModule().programStages()
                .byProgramUid().eq(newSelectedProgram.uid())
                .byEnableUserAssignment().isTrue
        ) doReturn mock()

        whenever(
            d2.programModule().programStages()
                .byProgramUid().eq(newSelectedProgram.uid())
                .byEnableUserAssignment().isTrue
                .blockingIsEmpty()
        ) doReturn false

        presenter.setProgramForTesting(program)
        presenter.program = newSelectedProgram

        verify(view).clearList(newSelectedProgram.uid())
        verify(view).setFabIcon(true)
    }

    @Test
    fun `Should show filters if list is ok`() {
        val observableSortingInject = ObservableField<SortingItem>()
        val observableOpenFilter = ObservableField<Filters>()
        whenever(filterRepository.programFilters(any())) doReturn
            listOf(
                AssignedFilter(
                    ProgramType.TRACKER,
                    observableSortingInject,
                    observableOpenFilter,
                    "asignToMe"
                )
            )
        presenter.checkFilters(true)
        verify(view, times(1)).setFiltersVisibility(true)
    }

    @Test
    fun `Should show filters if list is not ok but filters are active`() {
        val observableSortingInject = ObservableField<SortingItem>()
        val observableOpenFilter = ObservableField<Filters>()
        whenever(filterRepository.programFilters(any())) doReturn
            listOf(
                AssignedFilter(
                    ProgramType.TRACKER,
                    observableSortingInject,
                    observableOpenFilter,
                    "asignToMe"
                )
            )
        FilterManager.clearAll()
        FilterManager.getInstance().setAssignedToMe(true)
        presenter.checkFilters(false)
        verify(view, times(1)).setFiltersVisibility(true)
    }

    @Test
    fun `Should not show filters if list is not ok and filters are not active`() {
        FilterManager.clearAll()
        presenter.checkFilters(false)
        verify(view, times(1)).setFiltersVisibility(false)
    }

    @Test
    fun `Should clear other filters if webapp is config`() {
        val list = listOf<FilterItem>()
        whenever(filterRepository.homeFilters()) doReturn listOf()

        presenter.clearOtherFiltersIfWebAppIsConfig()

        verify(disableHomeFiltersFromSettingsApp).execute(list)
    }

    @Test
    fun `Should populate same list when onItemAction is triggered in FormView`() {
        presenter.populateList(null)
        verify(view, times(0)).setFabIcon(any())
    }

    @Test
    fun `Should populate list for enrollment `() {
        presenter.populateList(listOf())
        verify(view, times(1)).setFabIcon(any())
    }

    @After
    fun validate() {
        validateMockitoUsage()
    }
}
