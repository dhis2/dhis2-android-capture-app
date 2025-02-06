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
import org.dhis2.mobile.aggregates.test.testModule
import org.dhis2.mobile.aggregates.ui.states.DataSetScreenState
import org.dhis2.mobile.aggregates.ui.states.DataSetSectionTable
import org.junit.After
import org.junit.Before
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.koin.test.inject
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
internal class DataSetTableViewModelTest : KoinTest {

    private lateinit var testDispatcher: TestDispatcher

    @Before
    fun setUp() = runTest {
        testDispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(testDispatcher)
        startKoin {
            modules(testModule(testDispatcher, true))
        }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        stopKoin()
    }

    @Test
    fun `should receive initial states`() = runTest {
        val viewModel: DataSetTableViewModel by inject()

        viewModel.dataSetScreenState.test {
            assertTrue(awaitItem() is DataSetScreenState.Loading)
            with(awaitItem()) {
                assertTrue(this is DataSetScreenState.Loaded)
                assertTrue(this.dataSetSectionTable is DataSetSectionTable.Loading)
            }
            with(awaitItem()) {
                assertTrue(this is DataSetScreenState.Loaded)
                assertTrue(this.dataSetSectionTable is DataSetSectionTable.Loaded)
            }
        }
    }

    @Test
    fun `should not update selected section if it is the same`() = runTest {
        val viewModel: DataSetTableViewModel by inject()

        viewModel.dataSetScreenState.test {
            awaitInitialization()
            viewModel.onSectionSelected("section_uid1")
            expectNoEvents()
        }
    }

    @Test
    fun `should update selected section`() = runTest {
        val viewModel: DataSetTableViewModel by inject()

        viewModel.dataSetScreenState.test {
            awaitInitialization()
            viewModel.onSectionSelected("section_uid2")
            with(awaitItem()) {
                assertTrue(this is DataSetScreenState.Loaded)
                assertTrue(this.dataSetSectionTable is DataSetSectionTable.Loading)
            }
            with(awaitItem()) {
                assertTrue(this is DataSetScreenState.Loaded)
                assertTrue(this.dataSetSectionTable is DataSetSectionTable.Loaded)
            }
        }
    }

    private suspend fun ReceiveTurbine<DataSetScreenState>.awaitInitialization() = with(this) {
        awaitItem()
        awaitItem()
        awaitItem()
    }
}
