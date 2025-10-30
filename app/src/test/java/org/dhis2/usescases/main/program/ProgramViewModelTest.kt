package org.dhis2.usescases.main.program

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import io.reactivex.Flowable
import io.reactivex.processors.FlowableProcessor
import io.reactivex.processors.PublishProcessor
import io.reactivex.schedulers.TestScheduler
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.setMain
import org.dhis2.commons.featureconfig.data.FeatureConfigRepository
import org.dhis2.commons.filters.FilterManager
import org.dhis2.commons.matomo.MatomoAnalyticsController
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.data.schedulers.TestSchedulerProvider
import org.dhis2.data.service.SyncStatusController
import org.dhis2.data.service.SyncStatusData
import org.dhis2.mobile.commons.extensions.toColor
import org.dhis2.mobile.commons.model.MetadataIconData
import org.dhis2.utils.MainCoroutineScopeRule
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.mobile.ui.designsystem.component.ImageCardData
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

@ExperimentalCoroutinesApi
class ProgramViewModelTest {
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineScope = MainCoroutineScopeRule()

    private lateinit var presenter: ProgramViewModel

    private val view: ProgramView = mock()
    private val programRepository: ProgramRepository = mock()
    private val matomoAnalyticsController: MatomoAnalyticsController = mock()
    private val filterManager: FilterManager = mock()
    private val schedulerProvider: TestSchedulerProvider = TestSchedulerProvider(TestScheduler())
    private val syncStatusController: SyncStatusController = mock()
    private val testingDispatcher = UnconfinedTestDispatcher()
    private val featureConfigRepository: FeatureConfigRepository =
        mock {
            on { isFeatureEnable(any()) } doReturn false
        }
    private val dispatcherProvider =
        object : DispatcherProvider {
            override fun io(): CoroutineDispatcher = testingDispatcher

            override fun computation(): CoroutineDispatcher = testingDispatcher

            override fun ui(): CoroutineDispatcher = testingDispatcher
        }

    @Before
    fun setUp() {
        Dispatchers.setMain(testingDispatcher)
        presenter =
            ProgramViewModel(
                view,
                programRepository,
                featureConfigRepository,
                dispatcherProvider,
                matomoAnalyticsController,
                filterManager,
                syncStatusController,
                schedulerProvider,
            )
    }

    @Test
    fun `Should initialize program list`() {
        val programs = listOf(programViewModel())
        val programsFlowable = Flowable.just(programs)
        val filterProcessor: FlowableProcessor<FilterManager> = PublishProcessor.create()

        val syncStatusData = SyncStatusData(true)
        val filterManagerFlowable = Flowable.just(filterManager).startWith(filterProcessor)

        whenever(filterManager.asFlowable()) doReturn filterManagerFlowable

        whenever(
            syncStatusController.observeDownloadProcess(),
        ) doReturn MutableStateFlow(syncStatusData)
        whenever(programRepository.homeItems(any())) doReturn programsFlowable

        presenter.init()
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

    private fun programViewModel(): ProgramUiModel =
        ProgramUiModel(
            uid = "uid",
            title = "displayName",
            metadataIconData =
                MetadataIconData(
                    imageCardData =
                        ImageCardData.IconCardData(
                            "",
                            "",
                            "ic_home_positive",
                            "#84FFFF".toColor(),
                        ),
                    color = "#84FFFF".toColor(),
                ),
            count = 1,
            type = "type",
            typeName = "typeName",
            programType = "programType",
            description = "description",
            onlyEnrollOnce = true,
            accessDataWrite = true,
            state = State.SYNCED,
            downloadState = ProgramDownloadState.NONE,
            isStockUseCase = false,
            lastUpdated = Date(),
            filtersAreActive = false,
        )

    private fun dataSetViewModel(): ProgramUiModel =
        ProgramUiModel(
            uid = "uid",
            title = "displayName",
            metadataIconData =
                MetadataIconData(
                    imageCardData =
                        ImageCardData.IconCardData(
                            "",
                            "",
                            "ic_home_positive",
                            "#84FFFF".toColor(),
                        ),
                    color = "#84FFFF".toColor(),
                ),
            count = 1,
            type = "type",
            typeName = "typeName",
            programType = "",
            description = "description",
            onlyEnrollOnce = true,
            accessDataWrite = true,
            state = State.SYNCED,
            downloadState = ProgramDownloadState.NONE,
            isStockUseCase = false,
            lastUpdated = Date(),
            filtersAreActive = false,
        )
}
