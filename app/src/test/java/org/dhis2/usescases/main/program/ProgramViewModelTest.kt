package org.dhis2.usescases.main.program

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import io.reactivex.Flowable
import io.reactivex.schedulers.TestScheduler
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import org.dhis2.commons.matomo.MatomoAnalyticsController
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.data.schedulers.TestSchedulerProvider
import org.dhis2.data.service.SyncStatusController
import org.dhis2.data.service.SyncStatusData
import org.dhis2.ui.MetadataIconData
import org.dhis2.ui.toColor
import org.dhis2.utils.MainCoroutineScopeRule
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.mobile.ui.designsystem.component.internal.ImageCardData
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Date
import java.util.concurrent.TimeUnit

class ProgramViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @OptIn(ExperimentalCoroutinesApi::class)
    @get:Rule
    val coroutineScope = MainCoroutineScopeRule()

    private lateinit var presenter: ProgramViewModel

    private val view: ProgramView = mock()
    private val programRepository: ProgramRepository = mock()
    private val schedulers: TestSchedulerProvider = TestSchedulerProvider(TestScheduler())
    private val matomoAnalyticsController: MatomoAnalyticsController = mock()
    private val syncStatusController: SyncStatusController = mock()
    private val testingDispatcher = StandardTestDispatcher()
    private val dispatcherProvider = object : DispatcherProvider {
        override fun io(): CoroutineDispatcher {
            return testingDispatcher
        }

        override fun computation(): CoroutineDispatcher {
            return testingDispatcher
        }

        override fun ui(): CoroutineDispatcher {
            return testingDispatcher
        }
    }

    @Before
    fun setUp() {
        presenter = ProgramViewModel(
            view,
            programRepository,
            dispatcherProvider,
            matomoAnalyticsController,
            syncStatusController,
        )
    }

    @Test
    fun `Should initialize program list`() {
        val programs = listOf(programViewModel())
        val programsFlowable = Flowable.just(programs)
        val syncStatusData = SyncStatusData(true)

        whenever(
            syncStatusController.observeDownloadProcess(),
        ) doReturn MutableLiveData(syncStatusData)
        whenever(programRepository.homeItems(any())) doReturn programsFlowable

        presenter.init()
        schedulers.io().advanceTimeBy(1, TimeUnit.SECONDS)
        verify(programRepository).clearCache()
        assertTrue(presenter.programs.value?.isNotEmpty() == true)
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

        verify(view).navigateTo(programViewModel)
    }

    @Test
    fun `Should navigate to dataSet clicked and save program's theme setting if it has a theme`() {
        val dataSetViewModel = dataSetViewModel()

        presenter.onItemClick(dataSetViewModel)

        verify(view).navigateTo(dataSetViewModel)
    }

    @Test
    fun `Should clear all disposables`() {
        presenter.dispose()

        assertTrue(presenter.disposable.size() == 0)
    }

    private fun programViewModel(): ProgramUiModel {
        return ProgramUiModel(
            "uid",
            "displayName",
            MetadataIconData(
                imageCardData = ImageCardData.IconCardData("", "", "ic_home_positive", "#84FFFF".toColor()),
                color = "#84FFFF".toColor(),
            ),
            1,
            "type",
            "typeName",
            "programType",
            "description",
            onlyEnrollOnce = true,
            accessDataWrite = true,
            state = State.SYNCED,
            downloadState = ProgramDownloadState.NONE,
            stockConfig = null,
            lastUpdated = Date(),
        )
    }

    private fun dataSetViewModel(): ProgramUiModel {
        return ProgramUiModel(
            "uid",
            "displayName",
            MetadataIconData(
                imageCardData = ImageCardData.IconCardData("", "", "ic_home_positive", "#84FFFF".toColor()),
                color = "#84FFFF".toColor(),
            ),
            1,
            "type",
            "typeName",
            "",
            "description",
            onlyEnrollOnce = true,
            accessDataWrite = true,
            state = State.SYNCED,
            downloadState = ProgramDownloadState.NONE,
            stockConfig = null,
            lastUpdated = Date(),
        )
    }
}
