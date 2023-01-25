package org.dhis2.usescases.datasets.dataSetTable.dataSetSection

import androidx.annotation.VisibleForTesting
import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.dhis2.commons.schedulers.SchedulerProvider
import org.dhis2.composetable.TableScreenState
import org.dhis2.composetable.model.TableCell
import org.dhis2.composetable.model.TableModel
import org.dhis2.composetable.model.TextInputModel
import org.dhis2.data.forms.dataentry.ValueStore
import org.dhis2.data.forms.dataentry.tablefields.spinner.SpinnerViewModel
import org.dhis2.form.model.DispatcherProvider
import org.dhis2.form.model.ValueStoreResult.ERROR_UPDATING_VALUE
import org.dhis2.form.model.ValueStoreResult.VALUE_CHANGED
import org.dhis2.form.model.ValueStoreResult.VALUE_HAS_NOT_CHANGED
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.dataelement.DataElement
import timber.log.Timber

class DataValuePresenter(
    private val view: DataValueContract.View,
    private val repository: DataValueRepository,
    private val valueStore: ValueStore,
    private val schedulerProvider: SchedulerProvider,
    private val mapper: TableDataToTableModelMapper,
    private val dispatcherProvider: DispatcherProvider
) : CoroutineScope {
    var disposable: CompositeDisposable = CompositeDisposable()
    private val screenState: MutableStateFlow<TableScreenState> = MutableStateFlow(
        TableScreenState(
            emptyList(),
            false,
            overwrittenRowHeaderWidth = repository.getWidthForSection()
        )
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
                TableScreenState(
                    tables = it,
                    selectNext = false,
                    overwrittenRowHeaderWidth = repository.getWidthForSection()
                )
            }
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.io())
                .subscribe(
                    {
                        screenState.update { currentScreenState ->
                            currentScreenState.copy(tables = it.tables)
                        }
                    },
                    { Timber.e(it) }
                )
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

    private fun updateData(catComboUid: String, selectNext: Boolean) {
        val dataTableModel =
            repository.getDataTableModel(catComboUid)
                .blockingFirst()
        val tableData = repository.setTableData(dataTableModel, errors)
        val updatedTableModel = mapper(tableData)

        val updatedIndicators = indicatorTables()

        val updatedTables = screenState.value.tables.map { tableModel ->
            if (tableModel.id == catComboUid) {
                updatedTableModel.copy(overwrittenValues = tableModel.overwrittenValues)
            } else if (tableModel.id == null && updatedIndicators != null) {
                updatedIndicators
            } else {
                tableModel
            }
        }

        screenState.update { currentScreenState ->
            currentScreenState.copy(tables = updatedTables, selectNext = selectNext)
        }
    }

    fun onDettach() {
        disposable.clear()
    }

    fun currentState(): StateFlow<TableScreenState> = screenState

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun mutableTableData() = screenState

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun errors() = errors

    /**
     * Returns an TextInputModel if the current cell requires text input, null otherwise.
     * TODO: Refactor once we migrate all other value types inputs to compose.
     * */
    fun onCellClick(cell: TableCell, updateCellValue: (TableCell) -> Unit): TextInputModel? {
        val ids = cell.id?.split("_") ?: return null
        val dataElementUid = ids[0]
        val dataElement = getDataElement(dataElementUid)
        handleElementInteraction(dataElement, cell, updateCellValue)
        return dataElement.takeIf { it.optionSetUid() == null }
            ?.valueType()?.toKeyBoardInputType()?.let { inputType ->
            TextInputModel(
                id = cell.id ?: "",
                mainLabel = dataElement.displayFormName() ?: "-",
                secondaryLabels = repository.getCatOptComboOptions(ids[1]),
                currentValue = cell.value,
                keyboardInputType = inputType,
                error = errors[cell.id]
            )
        }
    }

    private fun handleElementInteraction(
        dataElement: DataElement,
        cell: TableCell,
        updateCellValue: (TableCell) -> Unit
    ) {
        if (dataElement.optionSetUid() != null) {
            view.showOptionSetDialog(
                dataElement,
                cell,
                getSpinnerViewModel(dataElement, cell),
                updateCellValue
            )
        } else when (dataElement.valueType()) {
            ValueType.BOOLEAN,
            ValueType.TRUE_ONLY -> view.showBooleanDialog(dataElement, cell, updateCellValue)
            ValueType.DATE -> view.showCalendar(dataElement, cell, false, updateCellValue)
            ValueType.DATETIME -> view.showCalendar(dataElement, cell, true, updateCellValue)
            ValueType.TIME -> view.showTimePicker(dataElement, cell, updateCellValue)
            ValueType.COORDINATE -> view.showCoordinatesDialog(dataElement, cell, updateCellValue)
            ValueType.ORGANISATION_UNIT -> view.showOtgUnitDialog(
                dataElement,
                cell,
                repository.orgUnits(),
                updateCellValue
            )
            ValueType.AGE -> view.showAgeDialog(dataElement, cell, updateCellValue)
            else -> {}
        }
    }

    private fun getSpinnerViewModel(dataElement: DataElement, cell: TableCell): SpinnerViewModel {
        return repository.getOptionSetViewModel(dataElement, cell)
    }

    private fun getDataElement(dataElementUid: String): DataElement {
        return repository.getDataElement(dataElementUid)
    }

    fun onSaveValueChange(cell: TableCell, selectNext: Boolean = false) {
        launch(dispatcherProvider.io()) {
            saveValue(cell, selectNext)
            view.onValueProcessed()
        }
    }

    private suspend fun saveValue(cell: TableCell, selectNext: Boolean) =
        withContext(dispatcherProvider.io()) {
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
                cell.value
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
                updateData(catComboUid!!, selectNext)
            }
        }

    fun saveWidth(widthDpValue: Float) {
        repository.saveWidthForSection(widthDpValue)
    }
}
