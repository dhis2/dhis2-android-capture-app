package org.dhis2.usescases.main.program

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Flowable
import io.reactivex.schedulers.TestScheduler
import java.util.concurrent.TimeUnit
import org.dhis2.commons.filters.FilterManager
import org.dhis2.data.schedulers.TestSchedulerProvider
import org.dhis2.ui.ThemeManager
import org.dhis2.utils.analytics.matomo.MatomoAnalyticsController
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ProgramPresenterTest {

    private lateinit var presenter: ProgramPresenter

    private val view: ProgramView = mock()
    private val programRepository: ProgramRepository = mock()
    private val schedulers: TestSchedulerProvider = TestSchedulerProvider(TestScheduler())
    private val themeManager: ThemeManager = mock()
    private val filterManager: FilterManager = mock()
    private val matomoAnalyticsController: MatomoAnalyticsController = mock()

    @Before
    fun setUp() {
        presenter = ProgramPresenter(
            view,
            programRepository,
            schedulers,
            themeManager,
            filterManager,
            matomoAnalyticsController
        )
    }

    @Test
    fun `Should initialize program list`() {
        val programs = listOf(programViewModel())
        val filterManagerFlowable = Flowable.just(filterManager)
        val programsFlowable = Flowable.just(programs)

        whenever(filterManager.asFlowable()) doReturn mock()
        whenever(filterManager.asFlowable().startWith(filterManager)) doReturn filterManagerFlowable
        whenever(filterManager.ouTreeFlowable()) doReturn Flowable.just(true)
        whenever(programRepository.programModels()) doReturn programsFlowable
        whenever(
            programRepository.aggregatesModels()
        ) doReturn Flowable.empty()

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
        whenever(
            programRepository.programModels()
        ) doReturn Flowable.error(Exception(""))
        whenever(
            programRepository.aggregatesModels()
        ) doReturn mock()

        whenever(filterManager.ouTreeFlowable()) doReturn Flowable.just(true)

        presenter.init()
        schedulers.io().advanceTimeBy(1, TimeUnit.SECONDS)

        verify(view).showFilterProgress()
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

        presenter.onItemClick(programViewModel)

        verify(themeManager).setProgramTheme(programViewModel.id())
        verify(view).navigateTo(programViewModel)
    }

    @Test
    fun `Should navigate to dataSet clicked and save program's theme setting if it has a theme`() {
        val dataSetViewModel = dataSetViewModel()

        presenter.onItemClick(dataSetViewModel)

        verify(themeManager).setDataSetTheme(dataSetViewModel.id())
        verify(view).navigateTo(dataSetViewModel)
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
            accessDataWrite = true,
            state = "Synced",
            hasOverdueEvent = false
        )
    }

    private fun dataSetViewModel(): ProgramViewModel {
        return ProgramViewModel.create(
            "uid",
            "displayName",
            "#ffcdd2",
            "icon",
            1,
            "type",
            "typeName",
            "",
            "description",
            onlyEnrollOnce = true,
            accessDataWrite = true,
            state = "Synced",
            hasOverdueEvent = false
        )
    }
}
