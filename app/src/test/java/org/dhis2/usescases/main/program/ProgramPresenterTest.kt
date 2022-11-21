package org.dhis2.usescases.main.program

import android.graphics.Color
import androidx.lifecycle.MutableLiveData
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Flowable
import io.reactivex.schedulers.TestScheduler
import java.util.concurrent.TimeUnit
import org.dhis2.commons.R
import org.dhis2.commons.filters.FilterManager
import org.dhis2.commons.matomo.MatomoAnalyticsController
import org.dhis2.data.schedulers.TestSchedulerProvider
import org.dhis2.data.service.SyncStatusController
import org.dhis2.data.service.SyncStatusData
import org.dhis2.ui.MetadataIconData
import org.dhis2.ui.ThemeManager
import org.hisp.dhis.android.core.common.State
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
    private val syncStatusController: SyncStatusController = mock()

    @Before
    fun setUp() {
        presenter = ProgramPresenter(
            view,
            programRepository,
            schedulers,
            themeManager,
            filterManager,
            matomoAnalyticsController,
            syncStatusController
        )
    }

    @Test
    fun `Should initialize program list`() {
        val programs = listOf(programViewModel())
        val filterManagerFlowable = Flowable.just(filterManager)
        val programsFlowable = Flowable.just(programs)
        val syncStatusData = SyncStatusData(true)

        whenever(filterManager.asFlowable()) doReturn mock()
        whenever(filterManager.asFlowable().startWith(filterManager)) doReturn filterManagerFlowable
        whenever(filterManager.ouTreeFlowable()) doReturn Flowable.just(true)
        whenever(
            syncStatusController.observeDownloadProcess()
        ) doReturn MutableLiveData(syncStatusData)
        whenever(programRepository.homeItems(any())) doReturn programsFlowable

        presenter.init()
        schedulers.io().advanceTimeBy(1, TimeUnit.SECONDS)
        verify(view).showFilterProgress()
        verify(view).swapProgramModelData(programs)
        verify(view).openOrgUnitTreeSelector()
    }

    @Test
    fun `Should render error when there is a problem getting programs`() {
        val filterManagerFlowable = Flowable.just(filterManager)
        val syncStatusData = SyncStatusData(true)

        whenever(filterManager.asFlowable()) doReturn mock()
        whenever(filterManager.asFlowable().startWith(filterManager)) doReturn filterManagerFlowable
        whenever(
            syncStatusController.observeDownloadProcess()
        ) doReturn MutableLiveData(syncStatusData)

        whenever(
            programRepository.homeItems(syncStatusData)
        ) doReturn Flowable.error(Exception(""))

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

        verify(themeManager).setProgramTheme(programViewModel.uid)
        verify(view).navigateTo(programViewModel)
    }

    @Test
    fun `Should navigate to dataSet clicked and save program's theme setting if it has a theme`() {
        val dataSetViewModel = dataSetViewModel()

        presenter.onItemClick(dataSetViewModel)

        verify(themeManager).setDataSetTheme(dataSetViewModel.uid)
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

        verifyNoMoreInteractions(view)
    }

    @Test
    fun `Should do nothing when program description is empty`() {
        presenter.showDescription("")

        verifyNoMoreInteractions(view)
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
        return ProgramViewModel(
            "uid",
            "displayName",
            MetadataIconData(
                programColor = Color.parseColor("#84FFFF"),
                iconResource = R.drawable.ic_home_positive
            ),
            1,
            "type",
            "typeName",
            "programType",
            "description",
            onlyEnrollOnce = true,
            accessDataWrite = true,
            state = State.SYNCED,
            hasOverdueEvent = false,
            filtersAreActive = false,
            downloadState = ProgramDownloadState.NONE
        )
    }

    private fun dataSetViewModel(): ProgramViewModel {
        return ProgramViewModel(
            "uid",
            "displayName",
            MetadataIconData(
                programColor = Color.parseColor("#84FFFF"),
                iconResource = R.drawable.ic_home_positive
            ),
            1,
            "type",
            "typeName",
            "",
            "description",
            onlyEnrollOnce = true,
            accessDataWrite = true,
            state = State.SYNCED,
            hasOverdueEvent = false,
            filtersAreActive = false,
            downloadState = ProgramDownloadState.NONE
        )
    }
}
