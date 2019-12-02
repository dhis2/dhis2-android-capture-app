package org.dhis2.usecases.main.program

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import io.reactivex.Flowable
import io.reactivex.schedulers.TestScheduler
import java.util.concurrent.TimeUnit
import org.dhis2.data.prefs.PreferenceProvider
import org.dhis2.data.schedulers.TestSchedulerProvider
import org.dhis2.usescases.main.program.HomeRepository
import org.dhis2.usescases.main.program.ProgramPresenter
import org.dhis2.usescases.main.program.ProgramView
import org.dhis2.usescases.main.program.ProgramViewModel
import org.dhis2.utils.Constants.PROGRAM_THEME
import org.dhis2.utils.filters.FilterManager
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ProgramPresenterTest {

    private lateinit var presenter: ProgramPresenter

    private val view: ProgramView = mock()
    private val homeRepository: HomeRepository = mock()
    private val schedulers: TestSchedulerProvider = TestSchedulerProvider(TestScheduler())
    private val preferences: PreferenceProvider = mock()
    private val filterManager: FilterManager = mock()

    @Before
    fun setUp() {
        presenter = ProgramPresenter(view, homeRepository, schedulers, preferences, filterManager)
    }

    @Test
    fun `Should initialize program list`() {
        val programs = listOf(programViewModel())
        val filterManagerFlowable = Flowable.just(filterManager)
        val programsFlowable = Flowable.just(programs)

        whenever(filterManager.asFlowable()) doReturn mock()
        whenever(filterManager.asFlowable().startWith(filterManager)) doReturn filterManagerFlowable
        whenever(filterManager.ouTreeFlowable()) doReturn Flowable.just(true)
        whenever(homeRepository.programModels(any(), any(), any())) doReturn programsFlowable
        whenever(homeRepository.aggregatesModels(any(), any(), any())) doReturn Flowable.empty()

        presenter.init()
        schedulers.io().advanceTimeBy(1, TimeUnit.SECONDS)
        verify(view).showFilterProgress()
        verify(view).swapProgramModelData(programs)
        verify(view).openOrgUnitTreeSelector()
    }

    @Test
    fun `Should render error when there is a problem getting programs`() {
        val filterManagerFlowable = Flowable.just(filterManager)

        whenever(filterManager.asFlowable()) doReturn mock()
        whenever(filterManager.asFlowable().startWith(filterManager)) doReturn filterManagerFlowable

        whenever(homeRepository.aggregatesModels(any(), any(),
                any())) doReturn Flowable.error(Exception(""))
        whenever(filterManager.ouTreeFlowable()) doReturn Flowable.just(true)

        presenter.init()
        schedulers.io().advanceTimeBy(1, TimeUnit.SECONDS)

        verify(view).showFilterProgress()
        verify(view).renderError("")
        verify(view).openOrgUnitTreeSelector()
    }

    @Test
    fun `Should show sync dialog when sync image is clicked`() {
        val programViewModel = programViewModel()

        presenter.onSyncStatusClick(programViewModel)

        verify(view).showSyncDialog(programViewModel)
    }

    @Test
    fun `Should navigate to program clicked and save program's theme setting if it has a theme`() {
        val programViewModel = programViewModel()

        presenter.onItemClick(programViewModel, 1)

        verify(preferences).setValue(PROGRAM_THEME, 1)
        verify(view).navigateTo(programViewModel)
    }

    @Test
    fun `Should navigate to program clicked and remove theme setting if it doesn't has a theme`() {
        val programViewModel = programViewModel()

        presenter.onItemClick(programViewModel, -1)

        verify(preferences).removeValue(PROGRAM_THEME)
        verify(view).navigateTo(programViewModel)
    }

    @Test
    fun `Should show program description when image is clicked`() {
        presenter.showDescription("description")

        verify(view).showDescription("description")
    }

    @Test
    fun `Should do nothing when program description is null`() {
        presenter.showDescription(null)

        verifyZeroInteractions(view)
    }

    @Test
    fun `Should do nothing when program description is empty`() {
        presenter.showDescription("")

        verifyZeroInteractions(view)
    }

    @Test
    fun `Should hide filters screen`() {
        presenter.showHideFilterClick()

        verify(view).showHideFilter()
    }

    @Test
    fun `Should clear all filters`() {
        presenter.clearFilterClick()

        verify(filterManager).clearAllFilters()
        verify(view).clearFilters()
    }

    @Test
    fun `Should clear all disposables`() {
        presenter.dispose()

        assertTrue(presenter.disposable.size() == 0)
    }

    @Test
    fun `Should refresh program list when granular sync finished`() {

        presenter.updateProgramQueries()
        verify(filterManager).publishData()
    }

    private fun programViewModel(): ProgramViewModel {
        return ProgramViewModel.create(
                "uid",
                "displayName",
                "#ffcdd2",
                "icon",
                1,
                "type",
                "typeName",
                "programType",
                "description",
                onlyEnrollOnce = true,
                accessDataWrite = true
        )
    }
}
