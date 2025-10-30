package org.dhis2.usescases.programEventDetail

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import io.reactivex.Single
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.schedulers.Schedulers
import org.dhis2.commons.filters.DisableHomeFiltersFromSettingsApp
import org.dhis2.commons.filters.FilterItem
import org.dhis2.commons.filters.FilterManager
import org.dhis2.commons.filters.data.FilterRepository
import org.dhis2.commons.filters.workingLists.EventFilterToWorkingListItemMapper
import org.dhis2.commons.matomo.MatomoAnalyticsController
import org.dhis2.commons.prefs.Preference.Companion.CURRENT_ORG_UNIT
import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.data.schedulers.TrampolineSchedulerProvider
import org.hisp.dhis.android.core.category.CategoryCombo
import org.hisp.dhis.android.core.category.CategoryOptionCombo
import org.hisp.dhis.android.core.program.Program
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class ProgramEventDetailPresenterTest {
    @Rule
    @JvmField
    var instantExecutorRule = InstantTaskExecutorRule()

    private val filterRepository: FilterRepository = mock()
    private lateinit var presenter: ProgramEventDetailPresenter

    private val view: ProgramEventDetailView = mock()
    private val repository: ProgramEventDetailRepository = mock()
    private val scheduler = TrampolineSchedulerProvider()
    private val filterManager: FilterManager = FilterManager.getInstance()
    private val workingListMapper: EventFilterToWorkingListItemMapper = mock()
    private val disableHomeFilters: DisableHomeFiltersFromSettingsApp = mock()
    private val matomoAnalyticsController: MatomoAnalyticsController = mock()
    private val preferences: PreferenceProvider = mock()

    @Before
    fun setUp() {
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }

        presenter =
            ProgramEventDetailPresenter(
                view,
                repository,
                scheduler,
                filterManager,
                workingListMapper,
                filterRepository,
                disableHomeFilters,
                matomoAnalyticsController,
                preferences,
            )
    }

    @After
    fun clear() {
        FilterManager.getInstance().clearAllFilters()
        RxAndroidPlugins.reset()
    }

    @Test
    fun `Should init screen`() {
        val program = Program.builder().uid("programUid").build()

        whenever(repository.getAccessDataWrite()) doReturn true
        whenever(repository.program()) doReturn Single.just(program)

        presenter.init()
        verify(view).setWritePermission(true)
        verify(view).setProgram(program)
    }

    @Test
    fun `Should show sync dialog`() {
        presenter.onSyncIconClick("uid")

        verify(view).showSyncDialog("uid")
    }

    @Test
    fun `Should start new event`() {
        whenever(preferences.getString(CURRENT_ORG_UNIT, null)) doReturn "orgUnit"

        presenter.addEvent()

        verify(view).selectOrgUnitForNewEvent(listOf("orgUnit"))
    }

    @Test
    fun `Should go back when back button is pressed`() {
        presenter.onBackClick()

        verify(view).back()
    }

    @Test
    fun `Should dispose of all disposables`() {
        presenter.onDettach()

        val result = presenter.compositeDisposable.size()

        assert(result == 0)
    }

    @Test
    fun `Should display message`() {
        val message = "message"

        presenter.displayMessage(message)

        verify(view).displayMessage(message)
    }

    @Test
    fun `Should show or hide filter`() {
        presenter.showFilter()

        verify(view).showHideFilter()
    }

    @Test
    fun `Should clear all filters when reset filter button is clicked`() {
        presenter.clearFilterClick()
        assertTrue(filterManager.totalFilters == 0)
    }

    @Test
    fun `Should clear other filters if webapp is config`() {
        val list = listOf<FilterItem>()
        whenever(filterRepository.homeFilters()) doReturn listOf()

        presenter.clearOtherFiltersIfWebAppIsConfig()

        verify(disableHomeFilters).execute(list)
    }

    private fun dummyCategoryCombo() = CategoryCombo.builder().uid("uid").build()

    private fun dummyListCatOptionCombo(): List<CategoryOptionCombo> = listOf(CategoryOptionCombo.builder().uid("uid").build())
}
