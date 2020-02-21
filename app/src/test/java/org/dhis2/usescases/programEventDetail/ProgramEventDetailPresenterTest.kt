
package org.dhis2.usescases.programEventDetail

import androidx.lifecycle.MutableLiveData
import androidx.paging.PagedList
import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.TestScheduler
import junit.framework.Assert.assertTrue
import org.dhis2.data.schedulers.TestSchedulerProvider
import org.dhis2.data.schedulers.TrampolineSchedulerProvider
import org.dhis2.data.tuples.Pair
import org.dhis2.utils.filters.FilterManager
import org.hisp.dhis.android.core.category.CategoryCombo
import org.hisp.dhis.android.core.category.CategoryOptionCombo
import org.hisp.dhis.android.core.common.FeatureType
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.event.EventStatus
import org.hisp.dhis.android.core.program.Program
import org.junit.Before
import org.junit.Test
import java.util.Date

class ProgramEventDetailPresenterTest {

    private lateinit var presenter: ProgramEventDetailPresenter

    private val view: ProgramEventDetailContract.View = mock()
    private val repository: ProgramEventDetailRepository = mock()
    private val scheduler = TrampolineSchedulerProvider()
    private val filterManager: FilterManager = FilterManager.getInstance()

    @Before
    fun setUp() {
        presenter = ProgramEventDetailPresenter(view, repository, scheduler, filterManager)
    }

    @Test
    fun `Should init screen`() {
        val program = Program.builder().uid("programUid").build()
        val catOptionComboPair = Pair.create(dummyCategoryCombo(), dummyListCatOptionCombo())
        val programEventViewModel = ProgramEventViewModel.create(
            "uid",
            "orgUnitUid",
            "orgUnit",
            Date(),
            State.TO_UPDATE,
            mutableListOf(),
            EventStatus.ACTIVE,
            true,
            "attr"
        )
        val events =
            MutableLiveData<PagedList<ProgramEventViewModel>>().also { it.value?.add(programEventViewModel) }
        val mapEvents = Pair<FeatureCollection, BoundingBox>(
            FeatureCollection.fromFeature(Feature.fromGeometry(null)),
            BoundingBox.fromLngLats(0.0, 0.0,0.0,0.0)
        )
        whenever(repository.featureType()) doReturn Single.just(FeatureType.POINT)
        whenever(repository.accessDataWrite) doReturn true
        whenever(repository.hasAccessToAllCatOptions()) doReturn Single.just(true)
        whenever(repository.program()) doReturn Observable.just(program)
        whenever(repository.catOptionCombos()) doReturn Single.just(catOptionComboPair)
        whenever(
            repository.filteredProgramEvents(any(), any(), any(), any(), any())
        ) doReturn events
        whenever(
            repository.filteredEventsForMap(any(), any(), any(), any(), any())
        ) doReturn Flowable.just(mapEvents)
        presenter.init()
        verify(view).setFeatureType()
        verify(view).setWritePermission(true)
        verify(view).setOptionComboAccess(true)
        verify(view).setProgram(program)
        verify(view).setCatOptionComboFilter(catOptionComboPair)
        verify(view).setLiveData(events)
        //verify(view).setMap()
    }

    @Test
    fun `Should show sync dialog`() {
        presenter.onSyncIconClick("uid")

        verify(view).showSyncDialog("uid")
    }

    @Test
    fun `Should navigate to event`() {
        presenter.onEventClick("eventId","orgUnit")

        verify(view).navigateToEvent("eventId","orgUnit")
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
        verify(view).clearFilters()
    }


    private fun dummyCategoryCombo() = CategoryCombo.builder().uid("uid").build()

    private fun dummyListCatOptionCombo(): List<CategoryOptionCombo> =
        listOf(CategoryOptionCombo.builder().uid("uid").build())
}
