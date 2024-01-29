package org.dhis2.usescases.datasets.dataSetTable.dataSetSection

import androidx.annotation.VisibleForTesting
import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.dhis2.commons.schedulers.SchedulerProvider
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.composetable.TableConfigurationState
import org.dhis2.composetable.TableScreenState
import org.dhis2.composetable.actions.Validator
import org.dhis2.composetable.model.TableCell
import org.dhis2.composetable.model.TableModel
import org.dhis2.composetable.model.TextInputModel
import org.dhis2.composetable.model.ValidationResult
import org.dhis2.data.forms.dataentry.ValueStore
import org.dhis2.data.forms.dataentry.tablefields.spinner.SpinnerViewModel
import org.dhis2.form.model.ValueStoreResult.ERROR_UPDATING_VALUE
import org.dhis2.form.model.ValueStoreResult.VALUE_CHANGED
import org.dhis2.form.model.ValueStoreResult.VALUE_HAS_NOT_CHANGED
import org.hisp.dhis.android.core.arch.helpers.Result
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.dataelement.DataElement
import timber.log.Timber
import kotlin.coroutines.CoroutineContext

class DataValuePresenter(
    private val view: DataValueContract.View,
    private val repository: DataValueRepository,
    private val valueStore: ValueStore,
    private val tableDimensionStore: TableDimensionStore,
    private val schedulerProvider: SchedulerProvider,
    private val mapper: TableDataToTableModelMapper,
    private val dispatcherProvider: DispatcherProvider,
) : CoroutineScope, Validator {
    var disposable: CompositeDisposable = CompositeDisposable()
    private val screenState: MutableStateFlow<TableScreenState> = MutableStateFlow(
        TableScreenState(emptyList()),
    )
    private val tableConfigurationState = MutableStateFlow(
        TableConfigurationState(
            overwrittenTableWidth = tableDimensionStore.getTableWidth(),
            overwrittenRowHeaderWidth = tableDimensionStore.getWidthForSection(),
            overwrittenColumnWidth = tableDimensionStore.getColumnWidthForSection(null),
        ),
    )

    private val errors: MutableMap<String, String> = mutableMapOf()

    private val dataSetInfo = repository.getDataSetInfo()

    private var job = Job()

    override val coroutineContext: CoroutineContext
        get() = job + dispatcherProvider.io()

    fun init() {
        disposable.add(
            Flowable.fromCallable {
                val tables = tables().blockingFirst()
                val indicators = indicatorTables()

                tables.toMutableList().also { list ->
                    indicators?.let { list.add(indicators) }
                }
            }.map {
                TableScreenState(tables = it)
            }
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.io())
                .subscribe(
                    {
                        screenState.update { currentScreenState ->
                            currentScreenState.copy(tables = it.tables)
                        }
                    },
                    { Timber.e(it) },
                ),
        )
    }

    private fun tables() = repository.getCatCombo().map {
        it.map { categoryCombo ->
            val dataTable = repository.getDataTableModel(categoryCombo).blockingFirst()
            mapper(repository.setTableData(dataTable, errors))
        }
    }

    private fun indicatorTables(): TableModel? = try {
        repository.getDataSetIndicators().map { indicatorsData ->
            mapper.map(indicatorsData)
        }.blockingGet()
    } catch (e: Exception) {
        null
    }

    private fun updateData(catComboUid: String) {
        val dataTableModel =
            repository.getDataTableModel(catComboUid)
                .blockingFirst()
        val tableData = repository.setTableData(dataTableModel, errors)
        val updatedTableModel = mapper(tableData)

        val updatedTables = screenState.value.tables.map { tableModel ->
            if (tableModel.id == catComboUid) {
                updatedTableModel.copy(overwrittenValues = tableModel.overwrittenValues)
            } else {
                indicatorTables() ?: tableModel
            }
        }

        screenState.update { currentScreenState ->
            currentScreenState.copy(tables = updatedTables)
        }
    }

    fun onDettach() {
        disposable.clear()
    }

    fun currentState(): StateFlow<TableScreenState> = screenState
    fun currentTableConfState(): StateFlow<TableConfigurationState> = tableConfigurationState

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun mutableTableData() = screenState

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun errors() = errors

    /**
     * Returns an TextInputModel if the current cell requires text input, null otherwise.
     * TODO: Refactor once we migrate all other value types inputs to compose.
     * */
    fun onCellClick(
        tableId: String,
        cell: TableCell,
        updateCellValue: (TableCell) -> Unit,
    ): TextInputModel? {
        val ids = cell.id?.split("_") ?: return null
        val dataElementUid = ids[0]
        val dataElement = getDataElement(dataElementUid)
        dataElement?.let { handleElementInteraction(dataElement, cell, updateCellValue) }
        return dataElement.takeIf { it?.optionSetUid() == null }
            ?.valueType()?.toKeyBoardInputType()?.let { inputType ->
                TextInputModel(
                    id = cell.id ?: "",
                    mainLabel = dataElement?.displayFormName() ?: "-",
                    secondaryLabels = repository.getCatOptComboOptions(ids[1]),
                    currentValue = cell.value,
                    keyboardInputType = inputType,
                    error = errors[cell.id],
                )
            }
    }

    private fun handleElementInteraction(
        dataElement: DataElement,
        cell: TableCell,
        updateCellValue: (TableCell) -> Unit,
    ) {
        if (dataElement.optionSetUid() != null) {
            view.showOptionSetDialog(
                dataElement,
                cell,
                getSpinnerViewModel(dataElement, cell),
                updateCellValue,
            )
        } else {
            when (dataElement.valueType()) {
                ValueType.BOOLEAN,
                ValueType.TRUE_ONLY,
                -> view.showBooleanDialog(dataElement, cell, updateCellValue)
                ValueType.DATE -> view.showCalendar(dataElement, cell, false, updateCellValue)
                ValueType.DATETIME -> view.showCalendar(dataElement, cell, true, updateCellValue)
                ValueType.TIME -> view.showTimePicker(dataElement, cell, updateCellValue)
                ValueType.COORDINATE -> view.showCoordinatesDialog(
                    dataElement,
                    cell,
                    updateCellValue,
                )
                ValueType.ORGANISATION_UNIT -> view.showOtgUnitDialog(
                    dataElement,
                    cell,
                    repository.orgUnits(),
                    updateCellValue,
                )
                ValueType.AGE -> view.showAgeDialog(dataElement, cell, updateCellValue)
                else -> {}
            }
        }
    }

    private fun getSpinnerViewModel(dataElement: DataElement, cell: TableCell): SpinnerViewModel {
        return repository.getOptionSetViewModel(dataElement, cell)
    }

    private fun getDataElement(dataElementUid: String): DataElement? {
        return repository.getDataElement(dataElementUid)
    }

    fun onSaveValueChange(cell: TableCell) {
        launch(
            dispatcherProvider.io(),
            start = CoroutineStart.ATOMIC,
        ) {
            saveValue(cell)
            view.onValueProcessed()
        }
    }

    override fun validate(tableCell: TableCell): ValidationResult {
        val ids = tableCell.id?.split("_")
        val dataElementUid = ids!![0]
        return when (val result = valueStore.validate(dataElementUid, tableCell.value)) {
            is Result.Failure -> ValidationResult.Error(result.failure.message ?: "")
            is Result.Success -> ValidationResult.Success(tableCell.value)
        }
    }

    private suspend fun saveValue(cell: TableCell) = withContext(dispatcherProvider.io()) {
        val ids = cell.id?.split("_")
        val dataElementUid = ids!![0]
        val catOptCombUid = ids[1]
        val catComboUid = screenState.value.tables
            .find { tableModel -> tableModel.hasCellWithId(cell.id) }?.id

        val result = valueStore.save(
            dataSetInfo.second,
            dataSetInfo.first,
            dataSetInfo.third,
            dataElementUid,
            catOptCombUid,
            cell.value,
        )
        val storeResult = result.blockingFirst()
        val saveResult = storeResult.valueStoreResult
        if (
            saveResult == VALUE_CHANGED || saveResult == ERROR_UPDATING_VALUE ||
            saveResult == VALUE_HAS_NOT_CHANGED
        ) {
            if (saveResult == ERROR_UPDATING_VALUE) {
                errors[cell.id!!] =
                    storeResult.valueStoreResultMessage ?: "-"
            } else {
                errors.remove(cell.id!!)
            }
            updateData(catComboUid!!)
        }
    }

    fun saveWidth(tableId: String, widthDpValue: Float) {
        tableDimensionStore.saveWidthForSection(tableId, widthDpValue)
    }

    fun saveColumnWidth(tableId: String, column: Int, widthDpValue: Float) {
        tableDimensionStore.saveColumnWidthForSection(tableId, column, widthDpValue)
    }

    fun resetTableDimensions(tableId: String) {
        tableDimensionStore.resetTable(tableId)
    }

    fun saveTableWidth(tableId: String, widthDpValue: Float) {
        tableDimensionStore.saveTableWidth(tableId, widthDpValue)
    }
}
