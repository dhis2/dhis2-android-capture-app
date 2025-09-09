package org.dhis2.mobile.aggregates.ui.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
import org.dhis2.mobile.aggregates.model.DataSetCompletionStatus.COMPLETED
import org.dhis2.mobile.aggregates.model.DataSetCompletionStatus.NOT_COMPLETED_EDITABLE
import org.dhis2.mobile.aggregates.model.DataSetCompletionStatus.NOT_COMPLETED_NOT_EDITABLE
import org.dhis2.mobile.aggregates.model.DataSetCustomTitle
import org.dhis2.mobile.aggregates.model.DataSetMandatoryFieldsStatus.ERROR
import org.dhis2.mobile.aggregates.model.DataSetMandatoryFieldsStatus.MISSING_MANDATORY_FIELDS
import org.dhis2.mobile.aggregates.model.DataSetMandatoryFieldsStatus.MISSING_MANDATORY_FIELDS_COMBINATION
import org.dhis2.mobile.aggregates.model.DataSetMandatoryFieldsStatus.SUCCESS
import org.dhis2.mobile.aggregates.model.ValidationResultStatus
import org.dhis2.mobile.aggregates.model.ValidationRulesConfiguration.MANDATORY
import org.dhis2.mobile.aggregates.model.ValidationRulesConfiguration.NONE
import org.dhis2.mobile.aggregates.model.ValidationRulesConfiguration.OPTIONAL
import org.dhis2.mobile.aggregates.model.Violation
import org.dhis2.mobile.aggregates.model.mapper.toTableModel
import org.dhis2.mobile.aggregates.model.mapper.updateValue
import org.dhis2.mobile.aggregates.model.mapper.withTotalsRow
import org.dhis2.mobile.aggregates.ui.constants.NO_SECTION_UID
import org.dhis2.mobile.aggregates.ui.dispatcher.Dispatcher
import org.dhis2.mobile.aggregates.ui.inputs.CellIdGenerator
import org.dhis2.mobile.aggregates.ui.inputs.ResizeAction
import org.dhis2.mobile.aggregates.ui.provider.DataSetModalDialogProvider
import org.dhis2.mobile.aggregates.ui.provider.IdsProvider.getCategoryOptionCombo
import org.dhis2.mobile.aggregates.ui.provider.IdsProvider.getDataElementUid
import org.dhis2.mobile.aggregates.ui.provider.ResourceManager
import org.dhis2.mobile.aggregates.ui.snackbar.SnackbarController
import org.dhis2.mobile.aggregates.ui.snackbar.SnackbarEvent
import org.dhis2.mobile.aggregates.ui.states.CellSelectionState
import org.dhis2.mobile.aggregates.ui.states.DataSetScreenState
import org.dhis2.mobile.aggregates.ui.states.DataSetSectionTable
import org.dhis2.mobile.aggregates.ui.states.OverwrittenDimension
import org.dhis2.mobile.aggregates.ui.states.ValidationBarUiState
import org.dhis2.mobile.aggregates.ui.states.mapper.InputDataUiStateMapper
import org.dhis2.mobile.commons.coroutine.CoroutineTracker
import org.dhis2.mobile.commons.input.CallbackStatus
import org.dhis2.mobile.commons.input.InputType
import org.dhis2.mobile.commons.input.UiAction
import org.dhis2.mobile.commons.input.UiActionHandler
import org.dhis2.mobile.commons.providers.FieldErrorMessageProvider
import org.hisp.dhis.mobile.ui.designsystem.component.UploadFileState
import org.hisp.dhis.mobile.ui.designsystem.component.table.model.TableCell
import org.hisp.dhis.mobile.ui.designsystem.component.table.model.TableModel
import org.hisp.dhis.mobile.ui.designsystem.component.table.ui.TableSelection

internal class DataSetTableViewModel(
    private val onClose: () -> Unit,
    private val getDataSetInstanceData: GetDataSetInstanceData,
    private val getDataSetSectionData: GetDataSetSectionData,
    private val getDataValueData: GetDataValueData,
    private val getDataSetSectionIndicators: GetDataSetSectionIndicators,
    private val getDataValueInput: GetDataValueInput,
    private val setDataValue: SetDataValue,
    private val uploadFile: UploadFile,
    private val computeResizeAction: ComputeResizeAction,
    private val resourceManager: ResourceManager,
    private val checkValidationRulesConfiguration: CheckValidationRulesConfiguration,
    private val checkCompletionStatus: CheckCompletionStatus,
    private val dispatcher: Dispatcher,
    private val datasetModalDialogProvider: DataSetModalDialogProvider,
    private val completeDataSet: CompleteDataSet,
    private val reopenDataSet: ReopenDataSet,
    private val runValidationRules: RunValidationRules,
    private val uiActionHandler: UiActionHandler,
    private val inputDataUiStateMapper: InputDataUiStateMapper,
    private val fieldErrorMessageProvider: FieldErrorMessageProvider,
) : ViewModel() {
    private var sectionChangeJob: Job? = null

    private val _dataSetScreenState =
        MutableStateFlow<DataSetScreenState>(DataSetScreenState.Loading)
    val dataSetScreenState =
        _dataSetScreenState
            .onStart { loadDataSet() }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000L),
                DataSetScreenState.Loading,
            )

    fun loadDataSet() {
        viewModelScope.launch(dispatcher.io()) {
            val dataSetInstanceData = getDataSetInstanceData(this)
            val initialSection = dataSetInstanceData.initialSectionToLoad

            val dataSetSectionTitle =
                if (!dataSetInstanceData.dataSetDetails.customTitle.isConfiguredTitle && dataSetInstanceData.dataSetSections.isNotEmpty()) {
                    dataSetInstanceData.dataSetSections[initialSection].title
                } else {
                    dataSetInstanceData.dataSetDetails.customTitle.header
                }
            val sectionToLoad =
                if (initialSection != 0) {
                    dataSetInstanceData.dataSetSections[initialSection].uid
                } else {
                    dataSetInstanceData.dataSetSections.firstOrNull()?.uid ?: NO_SECTION_UID
                }
            _dataSetScreenState.value =
                DataSetScreenState.Loaded(
                    dataSetDetails =
                        dataSetInstanceData.dataSetDetails.copy(
                            customTitle =
                                dataSetInstanceData.dataSetDetails.customTitle.copy(
                                    header = dataSetSectionTitle,
                                ),
                        ),
                    dataSetSections = dataSetInstanceData.dataSetSections,
                    renderingConfig = dataSetInstanceData.dataSetRenderingConfig,
                    dataSetSectionTable =
                        DataSetSectionTable(
                            sectionToLoad,
                            emptyList(),
                            overridingDimensions = overwrittenWidths(sectionToLoad),
                            loading = true,
                        ),
                    initialSection = dataSetInstanceData.initialSectionToLoad,
                    selectedCellInfo = CellSelectionState.Default(TableSelection.Unselected()),
                )

            val sectionTable = async { sectionData(sectionToLoad) }

            _dataSetScreenState.update {
                when (it) {
                    is DataSetScreenState.Loaded ->
                        it.copy(
                            dataSetDetails =
                                dataSetInstanceData.dataSetDetails.copy(
                                    customTitle =
                                        dataSetInstanceData.dataSetDetails.customTitle.copy(
                                            header = dataSetSectionTitle,
                                        ),
                                ),
                            dataSetSectionTable =
                                DataSetSectionTable(
                                    sectionToLoad,
                                    sectionTable.await(),
                                    overridingDimensions = overwrittenWidths(sectionToLoad),
                                    loading = false,
                                ),
                        )

                    DataSetScreenState.Loading ->
                        DataSetScreenState.Loaded(
                            dataSetDetails = dataSetInstanceData.dataSetDetails,
                            dataSetSections = dataSetInstanceData.dataSetSections,
                            renderingConfig = dataSetInstanceData.dataSetRenderingConfig,
                            dataSetSectionTable =
                                DataSetSectionTable(
                                    sectionToLoad,
                                    sectionTable.await(),
                                    overridingDimensions = overwrittenWidths(sectionToLoad),
                                    loading = true,
                                ),
                            initialSection = initialSection,
                            selectedCellInfo = CellSelectionState.Default(TableSelection.Unselected()),
                        )
                }
            }
        }
    }

    fun onSectionSelected(sectionUid: String) {
        if (_dataSetScreenState.value.currentSection() == sectionUid) return
        sectionChangeJob?.takeIf { it.isActive }?.cancel()
        sectionChangeJob =
            viewModelScope.launch(dispatcher.io()) {
                val selectedSectionIndex =
                    (dataSetScreenState.value as? DataSetScreenState.Loaded)
                        ?.dataSetSections
                        ?.indexOfFirst { it.uid == sectionUid }
                CoroutineTracker.increment()

                _dataSetScreenState.update {
                    if (it is DataSetScreenState.Loaded) {
                        val dataSetSectionTitle =
                            if (it.dataSetDetails.customTitle.isConfiguredTitle) {
                                it.dataSetDetails.customTitle.header
                            } else {
                                it.dataSetSections.firstOrNull { section -> section.uid == sectionUid }?.title
                            }
                        it.copy(
                            dataSetDetails =
                                it.dataSetDetails.copy(
                                    customTitle =
                                        DataSetCustomTitle(
                                            header = dataSetSectionTitle,
                                            subHeader = it.dataSetDetails.customTitle.subHeader,
                                            textAlignment = it.dataSetDetails.customTitle.textAlignment,
                                            isConfiguredTitle = it.dataSetDetails.customTitle.isConfiguredTitle,
                                        ),
                                ),
                            dataSetSectionTable =
                                it.dataSetSectionTable.copy(
                                    id = sectionUid,
                                    tableModels = emptyList(),
                                    overridingDimensions = overwrittenWidths(sectionUid),
                                    loading = true,
                                ),
                            selectedCellInfo = CellSelectionState.Default(TableSelection.Unselected()),
                            initialSection = selectedSectionIndex ?: 0,
                        )
                    } else {
                        it
                    }
                }

                val sectionData = async { sectionData(sectionUid) }
                _dataSetScreenState.update {
                    if (it is DataSetScreenState.Loaded) {
                        it.copy(
                            dataSetSectionTable =
                                it.dataSetSectionTable.copy(
                                    id = sectionUid,
                                    tableModels = sectionData.await(),
                                    overridingDimensions = overwrittenWidths(sectionUid),
                                    loading = false,
                                ),
                            initialSection = selectedSectionIndex ?: 0,
                        )
                    } else {
                        it
                    }
                }
                CoroutineTracker.decrement()
            }
    }

    private suspend fun overwrittenWidths(sectionUid: String) =
        OverwrittenDimension(
            overwrittenTableWidth =
                computeResizeAction(
                    ResizeAction.GetTableSavedWidth(sectionUid),
                )?.tableWidth() ?: emptyMap(),
            overwrittenRowHeaderWidth =
                computeResizeAction(
                    ResizeAction.GetRowHeaderSavedWidth(sectionUid),
                )?.rowHeaderWidths() ?: emptyMap(),
            overwrittenColumnWidth =
                computeResizeAction(
                    ResizeAction.GetColumSavedWidth(sectionUid),
                )?.columnWidths() ?: emptyMap(),
        )

    private suspend fun sectionData(sectionUid: String): List<TableModel> =
        withContext(dispatcher.io()) {
            val sectionData = getDataSetSectionData(sectionUid)

            val dataMaps =
                sectionData.tableGroups
                    .map { tableGroup ->
                        async {
                            tableGroup to
                                getDataValueData(
                                    dataElementUids = tableGroup.cellElements.map { it.uid },
                                    pivotedCategoryUid = sectionData.pivotedHeaderId(),
                                )
                        }
                    }.awaitAll()

            var absoluteRowIndex = 0
            val tables =
                dataMaps.map { (tableGroup, dataValueDataMap) ->
                    val tableModel =
                        tableGroup
                            .toTableModel(
                                resourceManager = resourceManager,
                                sectionData = sectionData,
                                dataValueDataMap = dataValueDataMap,
                                absoluteRowIndex = absoluteRowIndex,
                            ).also { absoluteRowIndex += it.tableRows.size }

                    if (sectionData.showColumnTotals()) {
                        tableModel
                            .withTotalsRow(resourceManager, absoluteRowIndex)
                            .also { absoluteRowIndex += 1 }
                    } else {
                        tableModel
                    }
                }

            val indicators =
                getDataSetSectionIndicators(sectionUid)
                    ?.toTableModel(resourceManager, absoluteRowIndex)
                    ?.also { absoluteRowIndex + it.tableRows.size }
                    ?.let { listOf(it) }
                    ?: emptyList()

            tables + indicators
        }

    suspend fun updateSelectedCell(
        cellId: String?,
        fetchOptions: Boolean = false,
        newValue: String? = null,
        validationError: String? = null,
        showInputDialog: Boolean = true,
    ) {
        CoroutineTracker.increment()
        val inputData =
            if (cellId != null) {
                val (rowIds, columnIds) = CellIdGenerator.getIdInfo(cellId)
                val dataElementUid = getDataElementUid(rowIds, columnIds)
                val categoryOptionComboUidData =
                    getCategoryOptionCombo(
                        rowIds,
                        columnIds,
                    )

                withContext(dispatcher.io()) {
                    val cellInfo =
                        getDataValueInput(
                            dataElementUid,
                            categoryOptionComboUidData,
                            fetchOptions,
                        )
                    inputDataUiStateMapper.map(
                        cellId = cellId,
                        cellInfo = cellInfo,
                        validationError = validationError,
                        valueWithError = newValue,
                        currentCell =
                            findCell(cellId)?.let { (tableId, cell) ->
                                TableSelection.CellSelection(
                                    tableId = tableId,
                                    rowIndex = requireNotNull(cell.row),
                                    columnIndex = cell.column,
                                    globalIndex = 0,
                                )
                            },
                        isLastCell = isLastCell(cellId),
                    )
                }
            } else {
                CellSelectionState.Default(TableSelection.Unselected())
            }

        _dataSetScreenState.update {
            (it as? DataSetScreenState.Loaded)?.copy(
                dataSetSectionTable =
                    if (inputData !is CellSelectionState.Default) {
                        require(inputData is CellSelectionState.InputDataUiState)
                        it.dataSetSectionTable.copy(
                            tableModels =
                                it.dataSetSectionTable.tableModels.map { table ->
                                    table.updateValue(
                                        cellId = cellId,
                                        updatedValue = inputData.displayValue,
                                        legendData = inputData.legendData,
                                        error = validationError,
                                        resourceManager = resourceManager,
                                    )
                                },
                        )
                    } else {
                        it.dataSetSectionTable
                    },
                selectedCellInfo = if (showInputDialog) inputData else CellSelectionState.Default(TableSelection.Unselected()),
            ) ?: it
        }
        CoroutineTracker.decrement()
    }

    fun onUiAction(uiAction: UiAction) {
        viewModelScope.launch {
            when (uiAction) {
                is UiAction.OnFocusChanged -> {
                }

                is UiAction.OnNextClick -> {
                    findCell(
                        cellId = uiAction.id,
                        findNextEditable = true,
                    )?.let { (_, nextCell) ->
                        updateSelectedCell(nextCell.id)
                    }
                }

                is UiAction.OnDoneClick -> {
                    _dataSetScreenState.update {
                        (it as? DataSetScreenState.Loaded)?.copy(
                            selectedCellInfo = CellSelectionState.Default(TableSelection.Unselected()),
                        ) ?: it
                    }
                    updateSelectedCell(null)
                }

                is UiAction.OnValueChanged -> {
                    CoroutineTracker.increment()

                    val result =
                        withContext(dispatcher.io()) {
                            val (rowIds, columnIds) = CellIdGenerator.getIdInfo(uiAction.id)
                            setDataValue(
                                rowIds = rowIds,
                                columnIds = columnIds,
                                value = uiAction.newValue,
                            )
                        }
                    result.fold(
                        onSuccess = {
                            val selectedCellInfo =
                                (_dataSetScreenState.value as? DataSetScreenState.Loaded)
                                    ?.selectedCellInfo

                            val fetchOptions =
                                when {
                                    selectedCellInfo is CellSelectionState.InputDataUiState &&
                                        selectedCellInfo.inputType is InputType.MultiText ->
                                        !selectedCellInfo.multiTextExtras().optionsFetched
                                    else ->
                                        false
                                }

                            updateSelectedCell(uiAction.id, fetchOptions, showInputDialog = uiAction.showInputDialog)
                        },
                        onFailure = {
                            updateSelectedCell(
                                cellId = uiAction.id,
                                newValue = uiAction.newValue,
                                validationError =
                                    fieldErrorMessageProvider.getFriendlyErrorMessage(
                                        it,
                                    ),
                                showInputDialog = uiAction.showInputDialog,
                            )
                        },
                    )
                    CoroutineTracker.decrement()
                }

                is UiAction.OnCall -> {
                    val actionCanNotBePerformedMsg = resourceManager.actionCantBePerformed()
                    uiActionHandler.onCall(uiAction.phoneNumber) {
                        showSnackbar(actionCanNotBePerformedMsg)
                    }
                }

                is UiAction.OnCaptureCoordinates -> {
                    val dataElementUid =
                        withContext(dispatcher.io()) {
                            val (rowIds, columnIds) = CellIdGenerator.getIdInfo(uiAction.id)
                            getDataElementUid(rowIds, columnIds)
                        }

                    uiActionHandler.onCaptureCoordinates(
                        fieldUid = dataElementUid,
                        locationType = uiAction.locationType,
                        initialData = uiAction.initialData,
                    ) { result ->
                        onUiAction(UiAction.OnValueChanged(uiAction.id, result))
                    }
                }

                is UiAction.OnAddImage -> {
                    uiActionHandler.onAddImage(uiAction.id) { result ->
                        result?.let {
                            uploadFile(uiAction.id, result)
                        }
                    }
                }

                is UiAction.OnTakePhoto -> {
                    uiActionHandler.onTakePicture { result ->
                        result?.let {
                            uploadFile(uiAction.id, it)
                        }
                    }
                }

                is UiAction.OnEmailAction -> {
                    val actionCanNotBePerformedMsg = resourceManager.actionCantBePerformed()
                    uiActionHandler.onSendEmail(uiAction.email) {
                        showSnackbar(actionCanNotBePerformedMsg)
                    }
                }

                is UiAction.OnLinkClicked -> {
                    val actionCanNotBePerformedMsg = resourceManager.actionCantBePerformed()
                    uiActionHandler.onOpenLink(uiAction.link) {
                        showSnackbar(actionCanNotBePerformedMsg)
                    }
                }

                is UiAction.OnDownloadFile -> {
                    val fileDownloadMsg = resourceManager.provideFileDownload()
                    val fileDownloadErrorMsg = resourceManager.provideFileDownloadError()
                    uiActionHandler.onDownloadFile(
                        uiAction.id,
                        uiAction.filePath,
                    ) { result ->
                        result?.let {
                            if (it == CallbackStatus.OK.name) {
                                showSnackbar(fileDownloadMsg)
                            } else {
                                showSnackbar(fileDownloadErrorMsg)
                            }
                        }
                    }
                }

                is UiAction.OnSelectFile -> {
                    updateFileLoadingState(UploadFileState.UPLOADING)
                    uiActionHandler.onSelectFile(
                        uiAction.id,
                        { result ->
                            result?.let { uploadFile(uiAction.id, result) }
                        },
                        {
                            updateFileLoadingState(UploadFileState.ADD)
                        },
                    )
                }

                is UiAction.OnShareImage -> {
                    val actionCanNotBePerformedMsg = resourceManager.actionCantBePerformed()
                    uiActionHandler.onShareImage(uiAction.filePath) {
                        showSnackbar(actionCanNotBePerformedMsg)
                    }
                }

                is UiAction.OnOpenOrgUnitTree -> {
                    uiActionHandler.onCaptureOrgUnit(
                        uiAction.currentOrgUnitUid
                            ?.let { listOf(it) } ?: emptyList(),
                    ) {
                        onUiAction(
                            UiAction.OnValueChanged(
                                id = uiAction.id,
                                newValue = it,
                            ),
                        )
                    }
                }

                is UiAction.OnFetchOptions ->
                    updateSelectedCell(uiAction.id, true)

                is UiAction.OnBarCodeScan -> {
                    // Not supported in DataSet Table
                }

                is UiAction.OnQRCodeScan -> {
                    // Not supported in DataSet Table
                }
            }
        }
    }

    private fun updateFileLoadingState(state: UploadFileState) {
        viewModelScope.launch(dispatcher.io()) {
            _dataSetScreenState.update {
                (it as? DataSetScreenState.Loaded)?.copy(
                    selectedCellInfo =
                        if (it.selectedCellInfo is CellSelectionState.InputDataUiState) {
                            it.selectedCellInfo.copy(
                                inputExtra =
                                    it.selectedCellInfo.fileExtras().copy(
                                        fileState = state,
                                    ),
                            )
                        } else {
                            it.selectedCellInfo
                        },
                ) ?: it
            }
        }
    }

    fun onReopenDataSet() {
        viewModelScope.launch(dispatcher.io()) {
            reopenDataSet()
            val updatedDetails = getDataSetInstanceData(this).dataSetDetails
            withContext(dispatcher.main()) {
                _dataSetScreenState.update {
                    (it as? DataSetScreenState.Loaded)?.copy(
                        dataSetDetails = updatedDetails,
                    ) ?: it
                }
            }
        }
    }

    fun onTableResize(resizeAction: ResizeAction) {
        viewModelScope.launch(dispatcher.io()) {
            computeResizeAction(resizeAction)
            val currentSectionId =
                (_dataSetScreenState.value as? DataSetScreenState.Loaded)
                    ?.dataSetSectionTable
                    ?.id ?: return@launch
            val updatedDimensions = overwrittenWidths(currentSectionId)
            _dataSetScreenState.update { state ->
                (state as? DataSetScreenState.Loaded)?.copy(
                    dataSetSectionTable =
                        state.dataSetSectionTable.copy(
                            overridingDimensions = updatedDimensions,
                        ),
                ) ?: state
            }
        }
    }

    private fun findCell(
        cellId: String,
        findNextEditable: Boolean = false,
    ): Pair<String, TableCell>? {
        val tables =
            (dataSetScreenState.value as DataSetScreenState.Loaded)
                .dataSetSectionTable.tableModels

        var currentCellFound = !findNextEditable

        for (table in tables) {
            for (row in table.tableRows) {
                for (cell in row.values.values) {
                    when {
                        findNextEditable && !currentCellFound && cell.id == cellId ->
                            currentCellFound = true

                        findNextEditable && currentCellFound && cell.editable ->
                            return Pair(table.id, cell)

                        !findNextEditable && cell.id == cellId ->
                            return Pair(table.id, cell)
                    }
                }
            }
        }
        return null
    }

    private fun isLastCell(cellId: String): Boolean {
        val tables =
            (dataSetScreenState.value as DataSetScreenState.Loaded).dataSetSectionTable.tableModels

        for (table in tables.asReversed()) {
            val lastEditableCell = getLastEditableCellFromTable(table)
            if (lastEditableCell != null) {
                return lastEditableCell == cellId
            }
        }
        return false
    }

    private fun getLastEditableCellFromTable(table: TableModel): String? {
        for (row in table.tableRows.asReversed()) {
            for (cell in row.values.values.reversed()) {
                if (cell.editable) {
                    return cell.id
                }
            }
        }
        return null
    }

    private fun uploadFile(
        cellId: String,
        path: String,
    ) {
        viewModelScope.launch {
            val result =
                withContext(dispatcher.io()) {
                    uploadFile(path)
                }
            result.fold(
                onSuccess = {
                    onUiAction(UiAction.OnValueChanged(cellId, it))
                },
                onFailure = {
                },
            )
        }
    }

    fun onSaveClicked() {
        viewModelScope.launch {
            CoroutineTracker.increment()
            val result =
                withContext(dispatcher.io()) {
                    checkValidationRulesConfiguration()
                }
            CoroutineTracker.decrement()

            when (result) {
                NONE -> {
                    attemptToFinish()
                }

                MANDATORY -> {
                    checkValidationRules()
                }

                OPTIONAL -> {
                    askRunValidationRules()
                }
            }
        }
    }

    private fun askRunValidationRules() {
        viewModelScope.launch {
            _dataSetScreenState.update {
                if (it is DataSetScreenState.Loaded) {
                    it.copy(
                        modalDialog =
                            datasetModalDialogProvider.provideAskRunValidationsDialog(
                                onDismiss = { onModalDialogDismissed() },
                                onDeny = { attemptToFinish() },
                                onAccept = { checkValidationRules() },
                            ),
                    )
                } else {
                    it
                }
            }
            CoroutineTracker.decrement()
        }
    }

    private fun checkValidationRules() {
        viewModelScope.launch {
            CoroutineTracker.increment()

            val rules =
                withContext(dispatcher.io()) {
                    runValidationRules()
                }

            when (rules.validationResultStatus) {
                ValidationResultStatus.OK -> {
                    _dataSetScreenState.update {
                        if (it is DataSetScreenState.Loaded) {
                            it.copy(validationBar = null)
                        } else {
                            it
                        }
                    }
                    attemptToFinish()
                }

                ValidationResultStatus.ERROR -> {
                    onModalDialogDismissed()
                    val completionStatus =
                        withContext(dispatcher.io()) {
                            checkCompletionStatus()
                        }
                    _dataSetScreenState.update {
                        if (it is DataSetScreenState.Loaded) {
                            it.copy(
                                validationBar =
                                    ValidationBarUiState(
                                        quantity = rules.violations.size,
                                        description =
                                            resourceManager.provideValidationErrorDescription(
                                                errors = rules.violations.size,
                                            ),
                                        onExpandErrors = {
                                            expandValidationErrors(
                                                violations = rules.violations,
                                                mandatory = rules.mandatory,
                                                canComplete = completionStatus == NOT_COMPLETED_EDITABLE,
                                            )
                                        },
                                    ),
                            )
                        } else {
                            it
                        }
                    }
                }
            }
            CoroutineTracker.decrement()
        }
    }

    private fun expandValidationErrors(
        violations: List<Violation>,
        mandatory: Boolean,
        canComplete: Boolean,
    ) {
        viewModelScope.launch {
            _dataSetScreenState.update {
                if (it is DataSetScreenState.Loaded) {
                    it.copy(
                        modalDialog =
                            datasetModalDialogProvider.provideValidationRulesErrorDialog(
                                violations = violations,
                                mandatory = mandatory,
                                onDismiss = { onModalDialogDismissed() },
                                onMarkAsComplete = { attemptToComplete() },
                                canComplete = canComplete,
                            ),
                    )
                } else {
                    it
                }
            }
        }
    }

    private fun attemptToFinish() {
        viewModelScope.launch {
            CoroutineTracker.increment()
            val onSavedMessage = resourceManager.provideSaved()

            val result =
                withContext(dispatcher.io()) {
                    checkCompletionStatus()
                }

            when (result) {
                COMPLETED, NOT_COMPLETED_NOT_EDITABLE -> onExit(onSavedMessage)
                NOT_COMPLETED_EDITABLE -> {
                    _dataSetScreenState.update {
                        if (it is DataSetScreenState.Loaded) {
                            it.copy(
                                modalDialog =
                                    datasetModalDialogProvider.provideCompletionDialog(
                                        onDismiss = { onModalDialogDismissed() },
                                        onNotNow = { onExit(onSavedMessage) },
                                        onComplete = { attemptToComplete() },
                                    ),
                            )
                        } else {
                            it
                        }
                    }
                }
            }
            CoroutineTracker.decrement()
        }
    }

    private fun attemptToComplete() {
        viewModelScope.launch {
            CoroutineTracker.increment()
            val result =
                withContext(dispatcher.io()) {
                    completeDataSet()
                }
            when (result) {
                MISSING_MANDATORY_FIELDS -> {
                    _dataSetScreenState.update {
                        if (it is DataSetScreenState.Loaded) {
                            it.copy(
                                modalDialog =
                                    datasetModalDialogProvider.provideMandatoryFieldsDialog(
                                        mandatoryFieldsMessage = resourceManager.provideMandatoryFieldsMessage(),
                                        onDismiss = { onModalDialogDismissed() },
                                        onAccept = { onModalDialogDismissed() },
                                    ),
                            )
                        } else {
                            it
                        }
                    }
                }

                MISSING_MANDATORY_FIELDS_COMBINATION -> {
                    _dataSetScreenState.update {
                        if (it is DataSetScreenState.Loaded) {
                            it.copy(
                                modalDialog =
                                    datasetModalDialogProvider.provideMandatoryFieldsDialog(
                                        mandatoryFieldsMessage = resourceManager.provideMandatoryFieldsCombinationMessage(),
                                        onDismiss = { onModalDialogDismissed() },
                                        onAccept = { onModalDialogDismissed() },
                                    ),
                            )
                        } else {
                            it
                        }
                    }
                }

                SUCCESS -> {
                    onModalDialogDismissed()
                    onExit(resourceManager.provideSavedAndCompleted())
                }

                ERROR -> {
                    onModalDialogDismissed()
                    showSnackbar(resourceManager.provideErrorOnCompleteDataset())
                }
            }
            CoroutineTracker.decrement()
        }
    }

    private fun onExit(exitMessage: String) {
        showSnackbar(exitMessage)
        viewModelScope.launch {
            withContext(dispatcher.main()) {
                onClose()
            }
        }
    }

    private fun onModalDialogDismissed() {
        _dataSetScreenState.update {
            if (it is DataSetScreenState.Loaded) {
                it.copy(modalDialog = null)
            } else {
                it
            }
        }
    }

    private fun showSnackbar(message: String) {
        viewModelScope.launch {
            SnackbarController.sendEvent(
                event =
                    SnackbarEvent(
                        message = message,
                    ),
            )
        }
    }

    fun onInputAction(
        cellId: String,
        isActionDone: Boolean,
    ) {
        if (isActionDone) {
            onUiAction(UiAction.OnDoneClick(cellId))
        } else {
            onUiAction(UiAction.OnNextClick(cellId))
        }
    }

    fun onResizingStatusChanged(selection: TableSelection) {
        _dataSetScreenState.update {
            if (it is DataSetScreenState.Loaded) {
                it.copy(
                    selectedCellInfo = CellSelectionState.Default(selection),
                )
            } else {
                it
            }
        }
    }
}
