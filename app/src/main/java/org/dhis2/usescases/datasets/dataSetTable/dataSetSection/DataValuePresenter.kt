package org.dhis2.usescases.datasets.dataSetTable.dataSetSection

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.dhis2.commons.schedulers.SchedulerProvider
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
) {
    var disposable: CompositeDisposable = CompositeDisposable()
    private val allTableState: MutableLiveData<List<TableModel>> = MutableLiveData(emptyList())
    private val errors: MutableMap<String, String> = mutableMapOf()

    private val dataSetInfo = repository.getDataSetInfo()

    fun init() {
        disposable.add(
            Flowable.fromCallable {
                val tables = tables().blockingFirst()
                val indicators = indicatorTables()

                tables.toMutableList().also { list ->
                    indicators?.let { list.add(indicators) }
                }
            }
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.io())
                .subscribe(
                    { allTableState.postValue(it) },
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

    private fun updateData(catComboUid: String) {
        val dataTableModel =
            repository.getDataTableModel(catComboUid)
                .blockingFirst()
        val tableData = repository.setTableData(dataTableModel, errors)
        val updatedTableModel = mapper(tableData)

        val updatedIndicators = indicatorTables()

        allTableState.postValue(
            allTableState.value?.map { tableModel ->
                if (tableModel.id == catComboUid) {
                    updatedTableModel
                } else if (tableModel.id == null && updatedIndicators != null) {
                    updatedIndicators
                } else {
                    tableModel
                }
            }
        )
    }

    fun onDettach() {
        disposable.clear()
    }

    fun tableData(): LiveData<List<TableModel>> = allTableState

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun mutableTableData() = allTableState

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun errors() = errors

    fun onCellValueChanged(tableCell: TableCell) {
        val updatedData = allTableState.value?.map { tableModel ->
            if (tableModel.hasCellWithId(tableCell.id)) {
                tableModel.copy(
                    overwrittenValues = mapOf(
                        Pair(tableCell.column!!, tableCell)
                    )
                )
            } else {
                tableModel
            }
        }
        allTableState.postValue(updatedData)
    }

    /**
     * Returns an TextInputModel if the current cell requires text input, null otherwise.
     * TODO: Refactor once we migrate all other value types inputs to compose.
     * */
    fun onCellClick(cell: TableCell): TextInputModel? {
        val ids = cell.id?.split("_") ?: return null
        val dataElementUid = ids[0]
        val dataElement = getDataElement(dataElementUid)
        handleElementInteraction(dataElement, cell)
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
        cell: TableCell
    ) {
        if (dataElement.optionSetUid() != null) {
            view.showOptionSetDialog(dataElement, cell, getSpinnerViewModel(dataElement, cell))
        } else when (dataElement.valueType()) {
            ValueType.BOOLEAN,
            ValueType.TRUE_ONLY -> view.showBooleanDialog(dataElement, cell)
            ValueType.DATE -> view.showCalendar(dataElement, cell, false)
            ValueType.DATETIME -> view.showCalendar(dataElement, cell, true)
            ValueType.TIME -> view.showTimePicker(dataElement, cell)
            ValueType.COORDINATE -> view.showCoordinatesDialog(dataElement, cell)
            ValueType.ORGANISATION_UNIT -> view.showOtgUnitDialog(
                dataElement,
                cell,
                repository.orgUnits()
            )
            ValueType.AGE -> view.showAgeDialog(dataElement, cell)
            else -> {}
        }
    }

    private fun getSpinnerViewModel(dataElement: DataElement, cell: TableCell): SpinnerViewModel {
        return repository.getOptionSetViewModel(dataElement, cell)
    }

    private fun getDataElement(dataElementUid: String): DataElement {
        return repository.getDataElement(dataElementUid)
    }

    fun onSaveValueChange(cell: TableCell) {
        runBlocking {
            saveValue(cell)
            view.onValueProcessed()
        }
    }

    private suspend fun saveValue(cell: TableCell) = withContext(dispatcherProvider.io()) {
        val ids = cell.id?.split("_")
        val dataElementUid = ids!![0]
        val catOptCombUid = ids[1]
        val catComboUid =
            allTableState.value?.find { tableModel -> tableModel.hasCellWithId(cell.id) }?.id

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
            updateData(catComboUid!!)
        }
    }
}
