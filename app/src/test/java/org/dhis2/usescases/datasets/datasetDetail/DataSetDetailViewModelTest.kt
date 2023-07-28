package org.dhis2.usescases.datasets.datasetDetail

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.setMain
import mock
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class DataSetDetailViewModelTest {

    @get:Rule
    val executorRule = InstantTaskExecutorRule()

    private val dispatcher: DispatcherProvider = com.nhaarman.mockitokotlin2.mock {
        on { io() } doReturn Dispatchers.IO
    }
    private val dataSetPageConfigurator: DataSetPageConfigurator = mock()
    private val initializedConfigurator: DataSetPageConfigurator = mock()

    private lateinit var viewModel: DataSetDetailViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @Test
    fun `Should init variables of page configurator`() {
        whenever(dataSetPageConfigurator.initVariables()) doReturn initializedConfigurator

        viewModel = DataSetDetailViewModel(
            dispatcher,
            dataSetPageConfigurator
        )
        viewModel.pageConfiguration.observeForever { result ->
            assertEquals(result, initializedConfigurator)
        }
    }
}
