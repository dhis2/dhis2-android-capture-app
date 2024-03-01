package org.dhis2.usescases.programEventDetail

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.paging.PagedList
import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.schedulers.Schedulers
import org.dhis2.commons.data.EventViewModel
import org.dhis2.commons.data.EventViewModelType
import org.dhis2.commons.data.ProgramEventViewModel
import org.dhis2.commons.data.tuples.Pair
import org.dhis2.commons.filters.DisableHomeFiltersFromSettingsApp
import org.dhis2.commons.filters.FilterItem
import org.dhis2.commons.filters.FilterManager
import org.dhis2.commons.filters.Filters
import org.dhis2.commons.filters.data.FilterPresenter
import org.dhis2.commons.filters.data.FilterRepository
import org.dhis2.commons.filters.sorting.SortingItem
import org.dhis2.commons.filters.sorting.SortingStatus
import org.dhis2.commons.filters.workingLists.EventFilterToWorkingListItemMapper
import org.dhis2.commons.matomo.MatomoAnalyticsController
import org.dhis2.data.schedulers.TrampolineSchedulerProvider
import org.dhis2.ui.MetadataIconData
import org.hisp.dhis.android.core.category.CategoryCombo
import org.hisp.dhis.android.core.category.CategoryOptionCombo
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.program.ProgramStage
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
    private val filterPresenter: FilterPresenter = mock()
    private val disableHomeFilters: DisableHomeFiltersFromSettingsApp = mock()
    private val matomoAnalyticsController: MatomoAnalyticsController = mock()

    @Before
    fun setUp() {
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }

        presenter = ProgramEventDetailPresenter(
            view,
            repository,
            scheduler,
            filterManager,
            workingListMapper,
            filterRepository,
            disableHomeFilters,
            matomoAnalyticsController,
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
        val catOptionComboPair = Pair.create(dummyCategoryCombo(), dummyListCatOptionCombo())

        val eventViewModel = EventViewModel(
            EventViewModelType.EVENT,
            ProgramStage.builder().uid("stageUid").build(),
            Event.builder().uid("event").build(),
            eventCount = 0,
            lastUpdate = null,
            isSelected = true,
            canAddNewEvent = true,
            orgUnitName = "orgUnit",
            catComboName = "catComboName",
            dataElementValues = emptyList(),
            groupedByStage = false,
            valueListIsOpen = false,
            displayDate = "2/01/2021",
            nameCategoryOptionCombo = "Category Option Combo",
            metadataIconData = MetadataIconData.Resource(1, 1),
        )
        val events =
            MutableLiveData<PagedList<EventViewModel>>().also {
                it.value?.add(eventViewModel)
            }

        val mapEvents = Triple<FeatureCollection, BoundingBox, List<ProgramEventViewModel>>(
            FeatureCollection.fromFeature(Feature.fromGeometry(null)),
            BoundingBox.fromLngLats(0.0, 0.0, 0.0, 0.0),
            listOf(),
        )
        val mapData = ProgramEventMapData(
            mutableListOf(),
            mutableMapOf("key" to FeatureCollection.fromFeature(Feature.fromGeometry(null))),
            BoundingBox.fromLngLats(0.0, 0.0, 0.0, 0.0),
        )
        filterManager.sortingItem = SortingItem(Filters.ORG_UNIT, SortingStatus.NONE)
        whenever(repository.getAccessDataWrite()) doReturn true
        whenever(repository.program()) doReturn Single.just(program)
        whenever(
            repository.filteredProgramEvents(),
        ) doReturn events
        whenever(
            repository.filteredEventsForMap(),
        ) doReturn Flowable.just(mapData)
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
        presenter.addEvent()

        verify(view).startNewEvent()
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

    private fun dummyListCatOptionCombo(): List<CategoryOptionCombo> =
        listOf(CategoryOptionCombo.builder().uid("uid").build())
}
