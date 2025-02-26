package org.dhis2.mobile.aggregates.ui.viewModel

import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.dhis2.mobile.aggregates.data.DataSetInstanceRepository
import org.dhis2.mobile.aggregates.di.aggregatesModule
import org.dhis2.mobile.aggregates.domain.CheckCompletionStatus
import org.dhis2.mobile.aggregates.domain.CheckValidationRulesConfiguration
import org.dhis2.mobile.aggregates.domain.CompleteDataSet
import org.dhis2.mobile.aggregates.domain.GetDataSetInstanceData
import org.dhis2.mobile.aggregates.domain.GetDataSetSectionData
import org.dhis2.mobile.aggregates.domain.GetDataSetSectionIndicators
import org.dhis2.mobile.aggregates.domain.GetDataValueData
import org.dhis2.mobile.aggregates.domain.RunValidationRules
import org.dhis2.mobile.aggregates.model.DataSetCompletionStatus.COMPLETED
import org.dhis2.mobile.aggregates.model.DataSetCompletionStatus.NOT_COMPLETED
import org.dhis2.mobile.aggregates.model.DataSetDetails
import org.dhis2.mobile.aggregates.model.DataSetInstanceData
import org.dhis2.mobile.aggregates.model.DataSetInstanceSectionData
import org.dhis2.mobile.aggregates.model.ValidationResultStatus
import org.dhis2.mobile.aggregates.model.ValidationRulesConfiguration.MANDATORY
import org.dhis2.mobile.aggregates.model.ValidationRulesConfiguration.NONE
import org.dhis2.mobile.aggregates.model.ValidationRulesConfiguration.OPTIONAL
import org.dhis2.mobile.aggregates.model.ValidationRulesResult
import org.dhis2.mobile.aggregates.ui.dispatcher.Dispatcher
import org.dhis2.mobile.aggregates.ui.provider.DataSetModalDialogProvider
import org.dhis2.mobile.aggregates.ui.provider.ResourceManager
import org.dhis2.mobile.aggregates.ui.states.DataSetModalDialogUIState
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
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
internal class DataSetTableViewModelTest : KoinTest {

    private lateinit var testDispatcher: TestDispatcher
    private lateinit var resourceManager: ResourceManager
    private lateinit var getDataSetInstanceData: GetDataSetInstanceData
    private lateinit var getDataSetSectionData: GetDataSetSectionData
    private lateinit var getDataValueConflict: GetDataValueData
    private lateinit var getDataValue: GetDataValueData
    private lateinit var getIndicators: GetDataSetSectionIndicators
    private lateinit var dispatcher: Dispatcher
    private lateinit var checkValidationRulesConfiguration: CheckValidationRulesConfiguration
    private lateinit var checkCompletionStatus: CheckCompletionStatus
    private lateinit var dataSetModalDialogProvider: DataSetModalDialogProvider
    private lateinit var completeDataSet: CompleteDataSet
    private lateinit var runValidationRules: RunValidationRules

    private val onCloseCallback: () -> Unit = mock()
    private val modalDialog: DataSetModalDialogUIState = mock()

    private lateinit var viewModel: DataSetTableViewModel

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
        resourceManager = declareMock<ResourceManager>()
        getDataSetInstanceData = declareMock<GetDataSetInstanceData>()
        getDataSetSectionData = declareMock<GetDataSetSectionData>()
        getDataValueConflict = declareMock<GetDataValueData>()
        getDataValue = declareMock<GetDataValueData>()
        getIndicators = declareMock<GetDataSetSectionIndicators>()
        dispatcher = declareMock<Dispatcher>()
        checkValidationRulesConfiguration = declareMock<CheckValidationRulesConfiguration>()
        checkCompletionStatus = declareMock<CheckCompletionStatus>()
        dataSetModalDialogProvider = declareMock<DataSetModalDialogProvider>()
        completeDataSet = declareMock<CompleteDataSet>()
        runValidationRules = declareMock<RunValidationRules>()

        whenever(resourceManager.defaultHeaderLabel()) doReturn "resource"
        whenever(dispatcher.io).thenReturn { testDispatcher }
        whenever(dispatcher.main).thenReturn { testDispatcher }
        whenever(getDataSetInstanceData(any())).thenReturn(
            DataSetInstanceData(
                dataSetDetails = DataSetDetails(
                    titleLabel = "label",
                    dateLabel = "date",
                    orgUnitLabel = "ou",
                    catOptionComboLabel = null,
                ),
                dataSetSections = emptyList(),
                dataSetRenderingConfig = mock(),
            ),
        )
        whenever(getDataSetSectionData(any())).thenReturn(
            DataSetInstanceSectionData(
                dataSetInstanceConfiguration = mock(),
                dataSetInstanceSectionConfiguration = mock(),
                tableGroups = listOf(),
            ),
        )
        whenever(getIndicators(any())).thenReturn(null)

        viewModel = DataSetTableViewModel(
            onCloseCallback,
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        stopKoin()
    }

    @Test
    fun `should receive initial states`() = runTest {
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
        viewModel.dataSetScreenState.test {
            awaitInitialization()
            viewModel.onSectionSelected("section_uid1")
            expectNoEvents()
        }
    }

    @Test
    fun `should update selected section`() = runTest {
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

    @Test
    fun `should finish a completed data set without validation rules`() = runTest {
        // Given there are no validation rules
        whenever(checkValidationRulesConfiguration()) doReturn NONE
        // And data set instance is completed
        whenever(checkCompletionStatus()) doReturn COMPLETED
        // And data set is saved
        whenever(resourceManager.provideSaved()) doReturn "saved"

        // When attempt to save
        viewModel.onSaveClicked()

        // Then data set instance is closed
        runCurrent() // Advance coroutine execution
        verify(onCloseCallback).invoke()
    }

    @Test
    fun `should show complete dialog when no validation rules and uncompleted`() = runTest {
        // Given there are no validation rules
        whenever(checkValidationRulesConfiguration()) doReturn NONE
        // And data set is not completed
        whenever(checkCompletionStatus()) doReturn NOT_COMPLETED

        whenever(
            dataSetModalDialogProvider.provideCompletionDialog(
                any(), any(), any(),
            ),
        ) doReturn modalDialog

        viewModel.dataSetScreenState.test {
            awaitInitialization()

            // When attempt to save
            viewModel.onSaveClicked()

            // Then shows completion dialog
            with(awaitItem()) {
                assertTrue(this is DataSetScreenState.Loaded)
                assertEquals(modalDialog, (this as DataSetScreenState.Loaded).modalDialog)
            }
        }
    }

    @Test
    fun `should ask to complete when running mandatory validation rules successfully`() = runTest {
        // Given there are mandatory validation rules
        whenever(checkValidationRulesConfiguration()) doReturn MANDATORY
        // And validation rules execution is OK
        val validationRulesResult = ValidationRulesResult(
            validationResultStatus = ValidationResultStatus.OK,
            violations = emptyList(),
        )
        whenever(runValidationRules()) doReturn validationRulesResult
        // And data set is not completed
        whenever(checkCompletionStatus()) doReturn NOT_COMPLETED

        whenever(
            dataSetModalDialogProvider.provideCompletionDialog(
                any(), any(), any(),
            ),
        ) doReturn modalDialog

        viewModel.dataSetScreenState.test {
            awaitInitialization()

            // When attempt to save
            viewModel.onSaveClicked()

            // Then shows completion dialog
            with(awaitItem()) {
                assertTrue(this is DataSetScreenState.Loaded)
                assertEquals(modalDialog, (this as DataSetScreenState.Loaded).modalDialog)
            }
        }
    }

    @Test
    fun `should ask to run optional validation rules`() = runTest {
        // Given there are optional validation rules
        whenever(checkValidationRulesConfiguration()) doReturn OPTIONAL

        whenever(
            dataSetModalDialogProvider.provideAskRunValidationsDialog(
                any(), any(), any(),
            ),
        ) doReturn modalDialog

        viewModel.dataSetScreenState.test {
            awaitInitialization()

            // When attempt to save
            viewModel.onSaveClicked()

            // Then shows optional validation rules dialog
            with(awaitItem()) {
                assertTrue(this is DataSetScreenState.Loaded)
                assertEquals(modalDialog, (this as DataSetScreenState.Loaded).modalDialog)
            }
        }
    }

    private suspend fun ReceiveTurbine<DataSetScreenState>.awaitInitialization() = with(this) {
        awaitItem()
        awaitItem()
        awaitItem()
    }
}
