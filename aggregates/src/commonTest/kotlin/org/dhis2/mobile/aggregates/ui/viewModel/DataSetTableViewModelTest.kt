package org.dhis2.mobile.aggregates.ui.viewModel

import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.dhis2.mobile.aggregates.data.DataSetInstanceRepository
import org.dhis2.mobile.aggregates.di.aggregatesModule
import org.dhis2.mobile.aggregates.domain.GetDataSetInstanceDetails
import org.dhis2.mobile.aggregates.domain.GetDataSetRenderingConfig
import org.dhis2.mobile.aggregates.domain.GetDataSetSectionData
import org.dhis2.mobile.aggregates.domain.GetDataSetSectionIndicators
import org.dhis2.mobile.aggregates.domain.GetDataSetSections
import org.dhis2.mobile.aggregates.domain.GetDataValue
import org.dhis2.mobile.aggregates.domain.GetDataValueConflict
import org.dhis2.mobile.aggregates.model.DataSetDetails
import org.dhis2.mobile.aggregates.model.DataSetInstanceSectionData
import org.dhis2.mobile.aggregates.ui.dispatcher.Dispatcher
import org.dhis2.mobile.aggregates.ui.states.DataSetScreenState
import org.dhis2.mobile.aggregates.ui.states.DataSetSectionTable
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.koin.core.component.get
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.koin.test.mock.MockProvider
import org.koin.test.mock.declareMock
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class DataSetTableViewModelTest : KoinTest {

    private lateinit var testDispatcher: TestDispatcher
    private lateinit var getDataSetInstanceDetails: GetDataSetInstanceDetails
    private lateinit var getDataSetSection: GetDataSetSections
    private lateinit var getDataSetRenderingConfig: GetDataSetRenderingConfig
    private lateinit var getDataSetSectionData: GetDataSetSectionData
    private lateinit var getDataValueConflict: GetDataValueConflict
    private lateinit var getDataValue: GetDataValue
    private lateinit var getIndicators: GetDataSetSectionIndicators
    private lateinit var dispatcher: Dispatcher

    @Before
    fun setUp() = runTest {
        testDispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(testDispatcher)
        startKoin {
            modules(aggregatesModule)
            MockProvider.register {
                mock(it.java)
            }
        }
        declareMock<DataSetInstanceRepository>()
        getDataSetInstanceDetails = declareMock<GetDataSetInstanceDetails>()
        getDataSetSection = declareMock<GetDataSetSections>()
        getDataSetRenderingConfig = declareMock<GetDataSetRenderingConfig>()
        getDataSetSectionData = declareMock<GetDataSetSectionData>()
        getDataValueConflict = declareMock<GetDataValueConflict>()
        getDataValue = declareMock<GetDataValue>()
        getIndicators = declareMock<GetDataSetSectionIndicators>()
        dispatcher = declareMock<Dispatcher>()

        whenever(dispatcher.io).thenReturn { testDispatcher }
        whenever(getDataSetInstanceDetails()).thenReturn(
            DataSetDetails(
                titleLabel = "label",
                dateLabel = "date",
                orgUnitLabel = "ou",
                catOptionComboLabel = null,
            ),
        )
        whenever(getDataSetRenderingConfig()).thenReturn(mock())
        whenever(getDataSetSection()).thenReturn(emptyList())
        whenever(getDataSetSectionData(any())).thenReturn(
            DataSetInstanceSectionData(
                dataSetInstanceConfiguration = mock(),
                dataSetInstanceSectionConfiguration = mock(),
                tableGroups = listOf(),
            ),
        )
        whenever(getIndicators(any())).thenReturn(null)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        stopKoin()
    }

    @Test
    fun `should receive initial states`() = runTest {
        val viewModel = DataSetTableViewModel(
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
        )

        viewModel.dataSetScreenState.test {
            assertTrue(awaitItem() is DataSetScreenState.Loading)
            with(awaitItem()) {
                assertTrue(this is DataSetScreenState.Loaded)
                assertTrue((this as DataSetScreenState.Loaded).dataSetSectionTable is DataSetSectionTable.Loading)
            }
            with(awaitItem()) {
                assertTrue(this is DataSetScreenState.Loaded)
                assertTrue((this as DataSetScreenState.Loaded).dataSetSectionTable is DataSetSectionTable.Loaded)
            }
        }
    }

    @Test
    fun `should not update selected section if it is the same`() = runTest {
        val viewModel = DataSetTableViewModel(
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
        )

        viewModel.dataSetScreenState.test {
            awaitInitialization()
            viewModel.onSectionSelected("section_uid1")
            expectNoEvents()
        }
    }

    @Test
    fun `should update selected section`() = runTest {
        val viewModel = DataSetTableViewModel(
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
        )

        viewModel.dataSetScreenState.test {
            awaitInitialization()
            viewModel.onSectionSelected("section_uid2")
            with(awaitItem()) {
                assertTrue(this is DataSetScreenState.Loaded)
                assertTrue((this as DataSetScreenState.Loaded).dataSetSectionTable is DataSetSectionTable.Loading)
            }
            with(awaitItem()) {
                assertTrue(this is DataSetScreenState.Loaded)
                assertTrue((this as DataSetScreenState.Loaded).dataSetSectionTable is DataSetSectionTable.Loaded)
            }
        }
    }

    private suspend fun ReceiveTurbine<DataSetScreenState>.awaitInitialization() = with(this) {
        awaitItem()
        awaitItem()
        awaitItem()
    }
}
