package org.dhis2.mobile.aggregates.ui.viewModel

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.ui.graphics.Color
import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
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
import org.dhis2.mobile.aggregates.domain.ComputeResizeAction
import org.dhis2.mobile.aggregates.domain.GetDataSetInstanceData
import org.dhis2.mobile.aggregates.domain.GetDataSetSectionData
import org.dhis2.mobile.aggregates.domain.GetDataSetSectionIndicators
import org.dhis2.mobile.aggregates.domain.GetDataValueData
import org.dhis2.mobile.aggregates.domain.GetDataValueInput
import org.dhis2.mobile.aggregates.domain.ReopenDataSet
import org.dhis2.mobile.aggregates.domain.RunValidationRules
import org.dhis2.mobile.aggregates.domain.SetDataValue
import org.dhis2.mobile.aggregates.domain.UploadFile
import org.dhis2.mobile.aggregates.model.CellElement
import org.dhis2.mobile.aggregates.model.CellInfo
import org.dhis2.mobile.aggregates.model.DataSetCompletionStatus.COMPLETED
import org.dhis2.mobile.aggregates.model.DataSetCompletionStatus.NOT_COMPLETED_EDITABLE
import org.dhis2.mobile.aggregates.model.DataSetCustomTitle
import org.dhis2.mobile.aggregates.model.DataSetDetails
import org.dhis2.mobile.aggregates.model.DataSetEdition
import org.dhis2.mobile.aggregates.model.DataSetInstanceConfiguration
import org.dhis2.mobile.aggregates.model.DataSetInstanceData
import org.dhis2.mobile.aggregates.model.DataSetInstanceSectionConfiguration
import org.dhis2.mobile.aggregates.model.DataSetInstanceSectionData
import org.dhis2.mobile.aggregates.model.DataSetRenderingConfig
import org.dhis2.mobile.aggregates.model.DataSetSection
import org.dhis2.mobile.aggregates.model.NonEditableReason
import org.dhis2.mobile.aggregates.model.PivoteMode
import org.dhis2.mobile.aggregates.model.TableGroup
import org.dhis2.mobile.aggregates.model.TextAlignment
import org.dhis2.mobile.aggregates.model.ValidationResultStatus
import org.dhis2.mobile.aggregates.model.ValidationRulesConfiguration.MANDATORY
import org.dhis2.mobile.aggregates.model.ValidationRulesConfiguration.NONE
import org.dhis2.mobile.aggregates.model.ValidationRulesConfiguration.OPTIONAL
import org.dhis2.mobile.aggregates.model.ValidationRulesResult
import org.dhis2.mobile.aggregates.ui.dispatcher.Dispatcher
import org.dhis2.mobile.aggregates.ui.inputs.CellIdGenerator
import org.dhis2.mobile.aggregates.ui.inputs.TableId
import org.dhis2.mobile.aggregates.ui.inputs.TableIdType
import org.dhis2.mobile.aggregates.ui.provider.DataSetModalDialogProvider
import org.dhis2.mobile.aggregates.ui.provider.IdsProvider
import org.dhis2.mobile.aggregates.ui.provider.ResourceManager
import org.dhis2.mobile.aggregates.ui.states.ButtonAction
import org.dhis2.mobile.aggregates.ui.states.CellSelectionState
import org.dhis2.mobile.aggregates.ui.states.CellSelectionState.InputDataUiState
import org.dhis2.mobile.aggregates.ui.states.DataSetModalDialogUIState
import org.dhis2.mobile.aggregates.ui.states.DataSetScreenState
import org.dhis2.mobile.aggregates.ui.states.mapper.InputDataUiStateMapper
import org.dhis2.mobile.commons.di.commonsModule
import org.dhis2.mobile.commons.extensions.toColor
import org.dhis2.mobile.commons.input.InputExtra
import org.dhis2.mobile.commons.input.InputType
import org.dhis2.mobile.commons.input.UiAction
import org.dhis2.mobile.commons.input.UiActionHandler
import org.hisp.dhis.mobile.ui.designsystem.component.InputShellState
import org.hisp.dhis.mobile.ui.designsystem.component.LegendData
import org.hisp.dhis.mobile.ui.designsystem.component.table.ui.TableSelection
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.assertThrows
import org.koin.core.component.get
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.koin.test.mock.MockProvider
import org.koin.test.mock.declareMock
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doReturnConsecutively
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
internal class DataSetTableViewModelTest : KoinTest {
    private lateinit var getDataSetInstanceData: GetDataSetInstanceData
    private lateinit var getDataSetSectionData: GetDataSetSectionData
    private lateinit var getDataValue: GetDataValueData
    private lateinit var getIndicators: GetDataSetSectionIndicators
    private lateinit var getDataValueInput: GetDataValueInput
    private lateinit var setDataValue: SetDataValue
    private lateinit var uploadFile: UploadFile
    private lateinit var computeResizeAction: ComputeResizeAction

    private lateinit var dispatcher: Dispatcher
    private lateinit var testDispatcher: TestDispatcher

    private lateinit var checkValidationRulesConfiguration: CheckValidationRulesConfiguration
    private lateinit var checkCompletionStatus: CheckCompletionStatus
    private lateinit var dataSetModalDialogProvider: DataSetModalDialogProvider
    private lateinit var completeDataSet: CompleteDataSet
    private lateinit var reopenDataSet: ReopenDataSet
    private lateinit var runValidationRules: RunValidationRules
    private lateinit var uiActionHandler: UiActionHandler
    private lateinit var inputDataUiStateMapper: InputDataUiStateMapper

    private val onCloseCallback: () -> Unit = mock()
    private val modalDialog: DataSetModalDialogUIState = mock()

    private lateinit var viewModel: DataSetTableViewModel

    @Before
    fun setUp() =
        runTest {
            testDispatcher = StandardTestDispatcher(testScheduler)
            Dispatchers.setMain(testDispatcher)
            startKoin {
                modules(aggregatesModule)
                modules(commonsModule)
                modules()
                MockProvider.register {
                    mock(it.java)
                }
            }

            declareMock<DataSetInstanceRepository>()
            getDataSetInstanceData = declareMock<GetDataSetInstanceData>()
            getDataSetSectionData = declareMock<GetDataSetSectionData>()
            getDataValue = declareMock<GetDataValueData>()
            getDataValueInput = declareMock<GetDataValueInput>()
            setDataValue = declareMock<SetDataValue>()
            getIndicators = declareMock<GetDataSetSectionIndicators>()
            uploadFile = declareMock<UploadFile>()
            declareMock<ResourceManager> {
                whenever(runBlocking { defaultHeaderLabel() }) doReturn "HeaderLabel"
                whenever(runBlocking { totalsHeader() }) doReturn "TotalsHeader"
                whenever(runBlocking { provideSaved() }) doReturn "saved"
            }
            dispatcher = declareMock<Dispatcher>()
            checkValidationRulesConfiguration = declareMock<CheckValidationRulesConfiguration>()
            checkCompletionStatus = declareMock<CheckCompletionStatus>()
            dataSetModalDialogProvider = declareMock<DataSetModalDialogProvider>()
            completeDataSet = declareMock<CompleteDataSet>()
            reopenDataSet = declareMock<ReopenDataSet>()
            runValidationRules = declareMock<RunValidationRules>()
            uiActionHandler = declareMock<UiActionHandler>()
            inputDataUiStateMapper = declareMock<InputDataUiStateMapper>()
            computeResizeAction = declareMock<ComputeResizeAction>()

            whenever(dispatcher.io).thenReturn { testDispatcher }
            whenever(dispatcher.main).thenReturn { testDispatcher }
            whenever(getDataSetInstanceData(any())).thenReturn(
                DataSetInstanceData(
                    dataSetDetails =
                        DataSetDetails(
                            customTitle =
                                DataSetCustomTitle(
                                    header = "title",
                                    subHeader = null,
                                    textAlignment = null,
                                    isConfiguredTitle = true,
                                ),
                            dateLabel = "date",
                            orgUnitLabel = "ou",
                            catOptionComboLabel = null,
                            dataSetTitle = "dataSetTitle",
                            isCompleted = false,
                            edition = DataSetEdition(editable = true, NonEditableReason.None),
                        ),
                    dataSetSections =
                        listOf(
                            DataSetSection(
                                uid = "sectionUid",
                                title = "sectionTitle",
                                misconfiguredRows = emptyList(),
                            ),
                        ),
                    dataSetRenderingConfig = DataSetRenderingConfig(useVerticalTabs = true),
                    initialSectionToLoad = 0,
                ),
            )
            whenever(getDataValue(any(), anyOrNull())) doReturn emptyMap()
            whenever(getDataSetSectionData(any())).thenReturn(
                DataSetInstanceSectionData(
                    dataSetInstanceConfiguration =
                        DataSetInstanceConfiguration(
                            hasDataElementDecoration = false,
                            compulsoryDataElements = emptyList(),
                            allDataSetElements = listOf(),
                            greyedOutFields = emptyList(),
                            editable = true,
                        ),
                    dataSetInstanceSectionConfiguration =
                        DataSetInstanceSectionConfiguration(
                            showRowTotals = true,
                            showColumnTotals = true,
                            pivotedHeaderId = null,
                        ),
                    tableGroups =
                        listOf(
                            TableGroup(
                                uid = "tableGroupUid",
                                label = "tableGroupTitle",
                                subgroups = emptyList(),
                                cellElements =
                                    listOf(
                                        CellElement(
                                            uid = "cellElementUid",
                                            label = "cellElementLabel",
                                            categoryComboUid = "categoryComboUid",
                                            description = null,
                                            isMultiText = false,
                                        ),
                                        CellElement(
                                            uid = "cellElementUid2",
                                            label = "cellElementLabel2",
                                            categoryComboUid = "categoryComboUid",
                                            description = null,
                                            isMultiText = false,
                                        ),
                                    ),
                                headerRows =
                                    listOf(
                                        listOf(
                                            CellElement(
                                                uid = "row1Uid",
                                                label = "Row1",
                                                categoryComboUid = null,
                                                description = null,
                                                isMultiText = false,
                                            ),
                                        ),
                                        listOf(
                                            CellElement(
                                                uid = "row2Uid",
                                                label = "Row2",
                                                categoryComboUid = null,
                                                description = null,
                                                isMultiText = false,
                                            ),
                                        ),
                                    ),
                                headerCombinations = listOf("Values"),
                                pivotMode = PivoteMode.None,
                            ),
                        ),
                ),
            )
            whenever(getIndicators(any())).thenReturn(null)

            viewModel =
                DataSetTableViewModel(
                    onClose = onCloseCallback,
                    getDataSetInstanceData = get(),
                    getDataSetSectionData = get(),
                    getDataValueData = get(),
                    getDataSetSectionIndicators = get(),
                    getDataValueInput = get(),
                    setDataValue = get(),
                    uploadFile = get(),
                    resourceManager = get(),
                    checkValidationRulesConfiguration = get(),
                    checkCompletionStatus = get(),
                    dispatcher = get(),
                    datasetModalDialogProvider = get(),
                    completeDataSet = get(),
                    reopenDataSet = get(),
                    runValidationRules = get(),
                    uiActionHandler = get(),
                    inputDataUiStateMapper = get(),
                    fieldErrorMessageProvider = get(),
                    computeResizeAction = get(),
                )
        }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        stopKoin()
    }

    @Test
    fun `should receive initial states`() =
        runTest {
            viewModel.dataSetScreenState.test {
                assertTrue(awaitItem() is DataSetScreenState.Loading)
                with(awaitItem()) {
                    assertTrue(this is DataSetScreenState.Loaded)
                    assertTrue((this as DataSetScreenState.Loaded).dataSetSectionTable.loading)
                }
                with(awaitItem()) {
                    assertTrue(this is DataSetScreenState.Loaded)
                    assertTrue(!(this as DataSetScreenState.Loaded).dataSetSectionTable.loading)
                }
            }
        }

    @Test
    fun `should not update selected section if it is the same`() =
        runTest {
            viewModel.dataSetScreenState.test {
                awaitInitialization()
                viewModel.onSectionSelected("section_uid1")
                expectNoEvents()
            }
        }

    @Test
    fun `should update selected section`() =
        runTest {
            viewModel.dataSetScreenState.test {
                awaitInitialization()
                viewModel.onSectionSelected("section_uid2")
                with(awaitItem()) {
                    assertTrue(this is DataSetScreenState.Loaded)
                    assertTrue((this as DataSetScreenState.Loaded).dataSetSectionTable.loading)
                }
                with(awaitItem()) {
                    assertTrue(this is DataSetScreenState.Loaded)
                    assertTrue(!(this as DataSetScreenState.Loaded).dataSetSectionTable.loading)
                }
            }
        }

    @Test
    fun `should update selected cell`() =
        runTest {
            val testingId =
                CellIdGenerator.generateId(
                    rowIds = listOf(TableId("rowId123456", TableIdType.DataElement)),
                    columnIds = listOf(TableId("columnId123", TableIdType.CategoryOptionCombo)),
                )
            val cellInfoData =
                listOf(
                    CellInfo(
                        label = "Input label",
                        value = "This is it",
                        displayValue = "The display value",
                        inputType = InputType.Text,
                        inputExtra = null,
                        supportingText = emptyList(),
                        errors = emptyList(),
                        warnings = emptyList(),
                        isRequired = false,
                        legendColor = "#90EE90",
                        legendLabel = "Legend label 1",
                    ),
                    CellInfo(
                        label = "Input label",
                        value = "This is other",
                        displayValue = "The display value",
                        inputType = InputType.Text,
                        inputExtra = null,
                        supportingText = emptyList(),
                        errors = emptyList(),
                        warnings = emptyList(),
                        isRequired = false,
                        legendColor = "#CD5C5C",
                        legendLabel = "Legend label 2",
                    ),
                )
            val inputData =
                cellInfoData.map { cellInfo ->
                    InputDataUiState(
                        id = testingId,
                        label = cellInfo.label,
                        value = cellInfo.value,
                        displayValue = cellInfo.displayValue,
                        inputType = cellInfo.inputType,
                        inputShellState = InputShellState.UNFOCUSED,
                        inputExtra = InputExtra.None,
                        supportingText = emptyList(),
                        isRequired = cellInfo.isRequired,
                        legendData =
                            if (cellInfo.legendLabel != null) {
                                LegendData(
                                    color = cellInfo.legendColor?.toColor() ?: Color.Unspecified,
                                    title = cellInfo.legendLabel,
                                )
                            } else {
                                null
                            },
                        currentSelectedCell =
                            TableSelection.CellSelection(
                                tableId = "tableId",
                                columnIndex = 0,
                                rowIndex = 0,
                                globalIndex = 0,
                            ),
                        buttonAction =
                            ButtonAction(
                                buttonText = "done",
                                icon = Icons.Default.Done,
                                isDoneAction = true,
                            ),
                    )
                }

            viewModel.dataSetScreenState.test {
                awaitInitialization()
                whenever(getDataValueInput(any(), any(), any())) doReturnConsecutively cellInfoData
                whenever(
                    inputDataUiStateMapper.map(
                        cellId = any(),
                        cellInfo = any(),
                        validationError = anyOrNull(),
                        valueWithError = anyOrNull(),
                        currentCell = anyOrNull(),
                        isLastCell = any(),
                    ),
                ) doReturnConsecutively inputData
                viewModel.updateSelectedCell(testingId)
                with(awaitItem()) {
                    if (this is DataSetScreenState.Loaded) {
                        assertTrue(this.selectedCellInfo is InputDataUiState)
                        require(this.selectedCellInfo is InputDataUiState)
                        assertEquals("Legend label 1", this.selectedCellInfo.legendData?.title)
                        assertEquals("#90EE90".toColor(), this.selectedCellInfo.legendData?.color)
                    } else {
                        assertTrue(false)
                    }
                }
                viewModel.updateSelectedCell(testingId)
                with(awaitItem()) {
                    if (this is DataSetScreenState.Loaded) {
                        assertTrue(this.selectedCellInfo is InputDataUiState)
                        require(this.selectedCellInfo is InputDataUiState)
                        assertEquals("Legend label 2", this.selectedCellInfo?.legendData?.title)
                        assertEquals("#CD5C5C".toColor(), this.selectedCellInfo?.legendData?.color)
                    } else {
                        assertTrue(false)
                    }
                }
                viewModel.updateSelectedCell(null)
                with(awaitItem()) {
                    assertTrue(this is DataSetScreenState.Loaded)
                    assertTrue((this as DataSetScreenState.Loaded).selectedCellInfo is CellSelectionState.Default)
                }
            }
        }

    @Test
    fun `should finish a completed data set without validation rules`() =
        runTest {
            // Given there are no validation rules
            whenever(checkValidationRulesConfiguration()) doReturn NONE
            // And data set instance is completed
            whenever(checkCompletionStatus()) doReturn COMPLETED

            // When attempt to save
            viewModel.onSaveClicked()

            // Then data set instance is closed
            runCurrent() // Advance coroutine execution
            verify(onCloseCallback).invoke()
        }

    @Test
    fun `should show complete dialog when no validation rules and uncompleted`() =
        runTest {
            // Given there are no validation rules
            whenever(checkValidationRulesConfiguration()) doReturn NONE
            // And data set is not completed
            whenever(checkCompletionStatus()) doReturn NOT_COMPLETED_EDITABLE

            whenever(
                dataSetModalDialogProvider.provideCompletionDialog(
                    any(),
                    any(),
                    any(),
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
    fun `should ask to complete when running mandatory validation rules successfully`() =
        runTest {
            // Given there are mandatory validation rules
            whenever(checkValidationRulesConfiguration()) doReturn MANDATORY
            // And validation rules execution is OK
            val validationRulesResult =
                ValidationRulesResult(
                    validationResultStatus = ValidationResultStatus.OK,
                    violations = emptyList(),
                )
            whenever(runValidationRules()) doReturn validationRulesResult
            // And data set is not completed
            whenever(checkCompletionStatus()) doReturn NOT_COMPLETED_EDITABLE

            whenever(
                dataSetModalDialogProvider.provideCompletionDialog(
                    any(),
                    any(),
                    any(),
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
    fun `should ask to run optional validation rules`() =
        runTest {
            // Given there are optional validation rules
            whenever(checkValidationRulesConfiguration()) doReturn OPTIONAL

            whenever(
                dataSetModalDialogProvider.provideAskRunValidationsDialog(
                    any(),
                    any(),
                    any(),
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

    @Test
    fun `should open org unit tree`() =
        runTest {
            val testingId =
                CellIdGenerator.generateId(
                    rowIds = listOf(TableId("rowId123456", TableIdType.DataElement)),
                    columnIds = listOf(TableId("columnId123", TableIdType.CategoryOptionCombo)),
                )
            val cellInfo =
                CellInfo(
                    label = "Org Unit Field",
                    value = null,
                    displayValue = null,
                    inputType = InputType.Text,
                    inputExtra = null,
                    supportingText = emptyList(),
                    errors = emptyList(),
                    warnings = emptyList(),
                    isRequired = false,
                    legendLabel = null,
                    legendColor = null,
                )
            val inputData =
                InputDataUiState(
                    id = testingId,
                    label = cellInfo.label,
                    value = cellInfo.value,
                    displayValue = cellInfo.displayValue,
                    inputType = cellInfo.inputType,
                    inputShellState = InputShellState.UNFOCUSED,
                    inputExtra = InputExtra.None,
                    supportingText = emptyList(),
                    isRequired = cellInfo.isRequired,
                    legendData =
                        if (cellInfo.legendLabel != null) {
                            LegendData(
                                color = cellInfo.legendColor?.toColor() ?: Color.Unspecified,
                                title = cellInfo.legendLabel,
                            )
                        } else {
                            null
                        },
                    currentSelectedCell =
                        TableSelection.CellSelection(
                            tableId = "tableId",
                            columnIndex = 0,
                            rowIndex = 0,
                            globalIndex = 0,
                        ),
                    buttonAction =
                        ButtonAction(
                            buttonText = "done",
                            icon = Icons.Default.Done,
                            isDoneAction = true,
                        ),
                )

            whenever(getDataValueInput(any(), any(), any())) doReturn cellInfo
            whenever(
                inputDataUiStateMapper.map(
                    cellId = any(),
                    cellInfo = any(),
                    validationError = anyOrNull(),
                    valueWithError = anyOrNull(),
                    currentCell = anyOrNull(),
                    isLastCell = any(),
                ),
            ) doReturn inputData

            viewModel.dataSetScreenState.test {
                awaitInitialization()
                viewModel.updateSelectedCell(testingId)
                awaitItem()
                viewModel.onUiAction(UiAction.OnOpenOrgUnitTree(testingId, null))
                testDispatcher.scheduler.advanceUntilIdle()
                verify(uiActionHandler).onCaptureOrgUnit(any(), any())
            }
        }

    @Test
    fun `should start call intent`() =
        runTest {
            val testingId =
                CellIdGenerator.generateId(
                    rowIds = listOf(TableId("rowId123456", TableIdType.DataElement)),
                    columnIds = listOf(TableId("columnId123", TableIdType.CategoryOptionCombo)),
                )
            viewModel.dataSetScreenState.test {
                awaitInitialization()
                viewModel.onUiAction(UiAction.OnCall(testingId, "111111111"))
                testDispatcher.scheduler.advanceUntilIdle()
                verify(uiActionHandler).onCall(any(), any())
            }
        }

    @Test
    fun `should start send email intent`() =
        runTest {
            val testingId =
                CellIdGenerator.generateId(
                    rowIds = listOf(TableId("rowId123456", TableIdType.DataElement)),
                    columnIds = listOf(TableId("columnId123", TableIdType.CategoryOptionCombo)),
                )
            viewModel.dataSetScreenState.test {
                awaitInitialization()
                viewModel.onUiAction(UiAction.OnEmailAction(testingId, "email@email.com"))
                testDispatcher.scheduler.advanceUntilIdle()
                verify(uiActionHandler).onSendEmail(any(), any())
            }
        }

    @Test
    fun `should start open url intent`() =
        runTest {
            val testingId =
                CellIdGenerator.generateId(
                    rowIds = listOf(TableId("rowId123456", TableIdType.DataElement)),
                    columnIds = listOf(TableId("columnId123", TableIdType.CategoryOptionCombo)),
                )
            viewModel.dataSetScreenState.test {
                awaitInitialization()
                viewModel.onUiAction(UiAction.OnLinkClicked(testingId, "www.test.com"))
                testDispatcher.scheduler.advanceUntilIdle()
                verify(uiActionHandler).onOpenLink(any(), any())
            }
        }

    @Test
    fun `should upload file`() =
        runTest {
            val testingId =
                CellIdGenerator.generateId(
                    rowIds = listOf(TableId("rowId123456", TableIdType.DataElement)),
                    columnIds = listOf(TableId("columnId123", TableIdType.CategoryOptionCombo)),
                )
            val filepath = "filepath"

            viewModel.dataSetScreenState.test {
                awaitInitialization()
                viewModel.onUiAction(UiAction.OnDownloadFile(testingId, filepath))
                testDispatcher.scheduler.advanceUntilIdle()
                verify(uiActionHandler).onDownloadFile(any(), any(), any())
            }
        }

    @Test
    fun `should start a share image intent`() =
        runTest {
            val testingId =
                CellIdGenerator.generateId(
                    rowIds = listOf(TableId("rowId123456", TableIdType.DataElement)),
                    columnIds = listOf(TableId("columnId123", TableIdType.CategoryOptionCombo)),
                )
            val filepath = "filepath"

            viewModel.dataSetScreenState.test {
                awaitInitialization()
                viewModel.onUiAction(UiAction.OnShareImage(testingId, filepath))
                testDispatcher.scheduler.advanceUntilIdle()
                verify(uiActionHandler).onShareImage(any(), any())
            }
        }

    @Test
    fun `should select image from gallery`() =
        runTest {
            val testingId =
                CellIdGenerator.generateId(
                    rowIds = listOf(TableId("rowId123456", TableIdType.DataElement)),
                    columnIds = listOf(TableId("columnId123", TableIdType.CategoryOptionCombo)),
                )

            viewModel.dataSetScreenState.test {
                awaitInitialization()
                viewModel.onUiAction(UiAction.OnAddImage(testingId))
                testDispatcher.scheduler.advanceUntilIdle()
                verify(uiActionHandler).onAddImage(any(), any())
            }
        }

    @Test
    fun `should open camera to take photo`() =
        runTest {
            val testingId =
                CellIdGenerator.generateId(
                    rowIds = listOf(TableId("rowId123456", TableIdType.DataElement)),
                    columnIds = listOf(TableId("columnId123", TableIdType.CategoryOptionCombo)),
                )

            viewModel.dataSetScreenState.test {
                awaitInitialization()
                viewModel.onUiAction(UiAction.OnTakePhoto(testingId))
                testDispatcher.scheduler.advanceUntilIdle()
                verify(uiActionHandler).onTakePicture(any())
            }
        }

    @Test
    fun `should throw error if more than one data element is provided`() {
        val exception =
            assertThrows<IllegalStateException> {
                IdsProvider.getDataElementUid(
                    rowIds = listOf(TableId("dataElementId", TableIdType.DataElement)),
                    columnIds = listOf(TableId("dataElementId", TableIdType.DataElement)),
                )
            }
        assertEquals(
            "Only one data element can be provided",
            exception.message,
        )
    }

    @Test
    fun `should throw error if more than one category option combo is provided`() =
        runTest {
            val exception =
                assertThrows<IllegalStateException> {
                    IdsProvider.getCategoryOptionCombo(
                        rowIds =
                            listOf(
                                TableId("dataElementUid", TableIdType.DataElement),
                                TableId("catOptionComboUid", TableIdType.CategoryOptionCombo),
                            ),
                        columnIds = listOf(TableId("catOptionComboUid", TableIdType.CategoryOptionCombo)),
                    )
                }
            assertEquals(
                "Only one category option combo can be provided",
                exception.message,
            )
        }

    @Test
    fun `should throw error if category options and category option combos are provided`() =
        runTest {
            val exception =
                assertThrows<IllegalStateException> {
                    IdsProvider.getCategoryOptionCombo(
                        rowIds =
                            listOf(
                                TableId("dataElementUid", TableIdType.DataElement),
                                TableId("catOptionUid", TableIdType.CategoryOption),
                            ),
                        columnIds =
                            listOf(
                                TableId("catOptionComboUid", TableIdType.CategoryOptionCombo),
                            ),
                    )
                }
            assertEquals(
                "Category options and category option combos cannot be provided at the same time",
                exception.message,
            )
        }

    @Test
    fun `should set left title alignment correctly`() =
        runTest {
            whenever(getDataSetInstanceData(any())).thenReturn(
                DataSetInstanceData(
                    dataSetDetails =
                        DataSetDetails(
                            customTitle =
                                DataSetCustomTitle(
                                    header = "Title",
                                    subHeader = "Subtitle",
                                    textAlignment = TextAlignment.LEFT,
                                    isConfiguredTitle = true,
                                ),
                            dateLabel = "date",
                            orgUnitLabel = "ou",
                            catOptionComboLabel = null,
                            dataSetTitle = "dataSetTitle",
                            isCompleted = false,
                            edition = DataSetEdition(editable = true, NonEditableReason.None),
                        ),
                    dataSetSections =
                        listOf(
                            DataSetSection(
                                uid = "sectionUid",
                                title = "sectionTitle",
                                misconfiguredRows = emptyList(),
                            ),
                        ),
                    dataSetRenderingConfig = DataSetRenderingConfig(useVerticalTabs = true),
                    initialSectionToLoad = 0,
                ),
            )

            viewModel.dataSetScreenState.test {
                // Then shows optional validation rules dialog
                assertTrue(awaitItem() is DataSetScreenState.Loading)
                with(awaitItem()) {
                    assertTrue(this is DataSetScreenState.Loaded)
                    assertEquals(
                        TextAlignment.LEFT,
                        (this as DataSetScreenState.Loaded).dataSetDetails.customTitle.textAlignment,
                    )
                    assertEquals("Title", this.dataSetDetails.customTitle.header)
                    assertEquals("Subtitle", this.dataSetDetails.customTitle.subHeader)
                }
            }
        }

    @Test
    fun `should set right title alignment correctly`() =
        runTest {
            whenever(getDataSetInstanceData(any())).thenReturn(
                DataSetInstanceData(
                    dataSetDetails =
                        DataSetDetails(
                            customTitle =
                                DataSetCustomTitle(
                                    header = "Title",
                                    subHeader = "Subtitle",
                                    textAlignment = TextAlignment.RIGHT,
                                    isConfiguredTitle = true,
                                ),
                            dateLabel = "date",
                            orgUnitLabel = "ou",
                            catOptionComboLabel = null,
                            dataSetTitle = "dataSetTitle",
                            isCompleted = false,
                            edition = DataSetEdition(editable = true, NonEditableReason.None),
                        ),
                    dataSetSections =
                        listOf(
                            DataSetSection(
                                uid = "sectionUid",
                                title = "sectionTitle",
                                misconfiguredRows = emptyList(),
                            ),
                        ),
                    dataSetRenderingConfig = DataSetRenderingConfig(useVerticalTabs = true),
                    initialSectionToLoad = 0,
                ),
            )

            viewModel.dataSetScreenState.test {
                // Then shows optional validation rules dialog
                assertTrue(awaitItem() is DataSetScreenState.Loading)
                with(awaitItem()) {
                    assertTrue(this is DataSetScreenState.Loaded)
                    assertEquals(
                        TextAlignment.RIGHT,
                        (this as DataSetScreenState.Loaded).dataSetDetails.customTitle.textAlignment,
                    )
                }
            }
        }

    @Test
    fun `should set center title alignment correctly`() =
        runTest {
            whenever(getDataSetInstanceData(any())).thenReturn(
                DataSetInstanceData(
                    dataSetDetails =
                        DataSetDetails(
                            customTitle =
                                DataSetCustomTitle(
                                    header = "Title",
                                    subHeader = "Subtitle",
                                    textAlignment = TextAlignment.CENTER,
                                    isConfiguredTitle = true,
                                ),
                            dateLabel = "date",
                            orgUnitLabel = "ou",
                            catOptionComboLabel = null,
                            dataSetTitle = "dataSetTitle",
                            isCompleted = false,
                            edition = DataSetEdition(editable = true, NonEditableReason.None),
                        ),
                    dataSetSections =
                        listOf(
                            DataSetSection(
                                uid = "sectionUid",
                                title = "sectionTitle",
                                misconfiguredRows = emptyList(),
                            ),
                        ),
                    dataSetRenderingConfig = DataSetRenderingConfig(useVerticalTabs = true),
                    initialSectionToLoad = 0,
                ),
            )

            viewModel.dataSetScreenState.test {
                // Then shows optional validation rules dialog
                assertTrue(awaitItem() is DataSetScreenState.Loading)
                with(awaitItem()) {
                    assertTrue(this is DataSetScreenState.Loaded)
                    assertEquals(
                        TextAlignment.CENTER,
                        (this as DataSetScreenState.Loaded).dataSetDetails.customTitle.textAlignment,
                    )
                }
            }
        }

    @Test
    fun shouldKeepFirstSection() =
        runTest {
            viewModel.dataSetScreenState.test {
                awaitInitialization()
                viewModel.onSectionSelected("section_uid1")
                with(awaitItem()) {
                    assertTrue(this is DataSetScreenState.Loaded)
                    assertTrue((this as DataSetScreenState.Loaded).dataSetSectionTable.loading)
                    assertTrue(this.currentSection() == "section_uid1")
                }
                viewModel.onSectionSelected("section_uid2")
                with(awaitItem()) {
                    assertTrue(this is DataSetScreenState.Loaded)
                    assertTrue((this as DataSetScreenState.Loaded).dataSetSectionTable.loading)
                    assertTrue(this.currentSection() == "section_uid2")
                }
                viewModel.onSectionSelected("section_uid1")
                with(awaitItem()) {
                    assertTrue(this is DataSetScreenState.Loaded)
                    assertTrue((this as DataSetScreenState.Loaded).dataSetSectionTable.loading)
                    assertTrue(this.currentSection() == "section_uid1")
                }
                with(awaitItem()) {
                    assertTrue(this is DataSetScreenState.Loaded)
                    assertTrue(!(this as DataSetScreenState.Loaded).dataSetSectionTable.loading)
                    assertTrue(this.currentSection() == "section_uid1")
                }
                expectNoEvents()
            }
        }

    private suspend fun ReceiveTurbine<DataSetScreenState>.awaitInitialization() =
        with(this) {
            awaitItem()
            awaitItem()
            awaitItem()
        }
}
