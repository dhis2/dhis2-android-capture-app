package org.dhis2.usescases.datasets.datasetDetail.datasetlist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import io.reactivex.Flowable
import io.reactivex.processors.FlowableProcessor
import io.reactivex.processors.PublishProcessor
import org.dhis2.commons.filters.FilterManager
import org.dhis2.commons.matomo.Actions
import org.dhis2.commons.matomo.Categories
import org.dhis2.commons.matomo.Labels
import org.dhis2.commons.matomo.MatomoAnalyticsController
import org.dhis2.data.schedulers.TrampolineSchedulerProvider
import org.dhis2.usescases.datasets.datasetDetail.DataSetDetailModel
import org.dhis2.usescases.datasets.datasetDetail.DataSetDetailRepository
import org.dhis2.usescases.datasets.datasetDetail.datasetList.DataSetListViewModel
import org.hisp.dhis.android.core.common.State
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class DataSetListViewModelTest {
    @get:Rule
    val rule = InstantTaskExecutorRule()

    private lateinit var viewModel: DataSetListViewModel
    private val repository: DataSetDetailRepository = mock()
    private val scheduler = TrampolineSchedulerProvider()
    private val filterManager: FilterManager = mock()
    private val matomoAnalyticsController: MatomoAnalyticsController = mock()

    private val filterProcessor: FlowableProcessor<FilterManager> = PublishProcessor.create()
    private val filterManagerFlowable = Flowable.just(filterManager).startWith(filterProcessor)

    @Before
    fun setUp() {
        whenever(filterManager.asFlowable()) doReturn filterManagerFlowable
        whenever(repository.canWriteAny()) doReturn Flowable.just(true)
        viewModel = DataSetListViewModel(
            repository,
            scheduler,
            filterManager,
            matomoAnalyticsController
        )
    }

    @Test
    fun `Should get the list of dataSet`() {
        val dataSets = listOf(dummyDataSet(), dummyDataSet(), dummyDataSet())
        whenever(
            repository.dataSetGroups(any(), any(), any(), any())
        ) doReturn Flowable.just(dataSets)
        viewModel = DataSetListViewModel(
            repository,
            scheduler,
            filterManager,
            matomoAnalyticsController
        )
        assert(viewModel.datasets.value == dataSets)
    }

    @Test
    fun `Should get write permissions`() {
        whenever(repository.canWriteAny()) doReturn Flowable.just(true)
        assert(viewModel.canWrite.value == true)
    }

    @Test
    fun `Should open dataset`() {
        val dataSet = dummyDataSet()
        viewModel.openDataSet(dataSet)
        assert(viewModel.selectedDataset.value?.peekContent() == dataSet)
    }

    @Test
    fun `Should sync dataset`() {
        val dataSet = dummyDataSet()
        viewModel.syncDataSet(dataSet)
        verify(matomoAnalyticsController).trackEvent(
            Categories.DATASET_LIST,
            Actions.SYNC_DATASET,
            Labels.CLICK
        )
        assert(viewModel.selectedSync.value?.peekContent() == dataSet)
    }

    @Test
    fun `Should updateData`() {
        viewModel.updateData()
        verify(filterManager).publishData()
    }

    private fun dummyDataSet() = DataSetDetailModel.create(
        "",
        "",
        "",
        "",
        "",
        "",
        State.SYNCED,
        "",
        true,
        false
    )
}
