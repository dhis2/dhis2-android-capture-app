package org.dhis2.usescases.datasets.dataSetTable.dataSetSection

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.flowables.ConnectableFlowable
import io.reactivex.processors.FlowableProcessor
import io.reactivex.processors.PublishProcessor
import org.dhis2.commons.data.tuples.Trio
import org.dhis2.commons.featureconfig.data.FeatureConfigRepository
import org.dhis2.commons.featureconfig.model.Feature.ANDROAPP_4754
import org.dhis2.commons.schedulers.SchedulerProvider
import org.dhis2.composetable.model.TableCell
import org.dhis2.composetable.model.TableModel
import org.dhis2.composetable.model.TextInputModel
import org.dhis2.data.forms.dataentry.ValueStore
import org.dhis2.data.forms.dataentry.tablefields.RowAction
import org.dhis2.data.forms.dataentry.tablefields.spinner.SpinnerViewModel
import org.dhis2.form.model.StoreResult
import org.dhis2.form.model.ValueStoreResult
import org.dhis2.form.model.ValueStoreResult.ERROR_UPDATING_VALUE
import org.dhis2.form.model.ValueStoreResult.VALUE_CHANGED
import org.dhis2.usescases.datasets.dataSetTable.DataSetTableModel
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.dataelement.DataElement
import timber.log.Timber

class DataValuePresenter(
    private val view: DataValueContract.View,
    private val repository: DataValueRepository,
    private val valueStore: ValueStore,
    private val schedulerProvider: SchedulerProvider,
    private val updateProcessor: FlowableProcessor<Unit>,
    private val featureConfigRepository: FeatureConfigRepository? = null,
    private val mapper: TableDataToTableModelMapper
) {
    var disposable: CompositeDisposable = CompositeDisposable()

    private val tableState: MutableLiveData<List<TableModel>> = MutableLiveData(emptyList())
    private val indicatorsState: MutableLiveData<List<TableModel>> = MutableLiveData(emptyList())
    private val allTableState: MutableLiveData<List<TableModel>> = MutableLiveData(emptyList())
    private val errors:MutableMap<String, String> =  mutableMapOf()

    private val processor: FlowableProcessor<RowAction> =
        PublishProcessor.create()
    private val processorOptionSet: FlowableProcessor<Trio<String, String, Int>> =
        PublishProcessor.create()

    fun init() {
        val dataTableModelConnectable = updateProcessor.startWith(Unit).switchMap {
            repository.getCatCombo()
                .flatMapIterable { categoryCombos -> categoryCombos }
                .map { categoryCombo ->
                    repository.getDataTableModel(categoryCombo).blockingFirst()
                }
        }.observeOn(schedulerProvider.ui())
            .doOnNext {
                view.clearTables()
            }.publish()

        initTables(dataTableModelConnectable)

        disposable.add(
            dataTableModelConnectable
                .switchMap { dataTableModel ->
                    processor.flatMap { rowAction ->

                        var dataSetTableModel: DataSetTableModel? = null
                        val dataValue = dataTableModel.dataValues?.firstOrNull {
                            it.dataElement == rowAction.dataElement() &&
                                    it.categoryOptionCombo == rowAction.catOptCombo()
                        }
                        when (dataValue) {
                            null -> if (!rowAction.value().isNullOrEmpty()) {
                                dataSetTableModel = DataSetTableModel(
                                    rowAction.dataElement(),
                                    dataTableModel.periodId,
                                    dataTableModel.orgUnitUid,
                                    rowAction.catOptCombo(),
                                    dataTableModel.attributeOptionComboUid,
                                    rowAction.value(),
                                    "",
                                    "",
                                    rowAction.listCategoryOption(),
                                    rowAction.catCombo()
                                ).also {
                                    dataTableModel.dataValues?.add(it)
                                }
                            }
                            else -> {
                                dataSetTableModel = dataValue.setValue(rowAction.value())
                                if (rowAction.value().isNullOrEmpty()) {
                                    dataTableModel.dataValues.remove(dataValue)
                                }
                            }
                        }

                        dataSetTableModel?.let {
                            val result = valueStore.save(it)
                            val saveResult = result.blockingFirst().valueStoreResult
                            if ((saveResult == VALUE_CHANGED || saveResult == ERROR_UPDATING_VALUE) &&
                                featureConfigRepository?.isFeatureEnable(ANDROAPP_4754) == true
                            ) {
                                if(saveResult == ERROR_UPDATING_VALUE){
                                    errors[it.dataElement+"_"+it.categoryOptionCombo] = "Value type error message"
                                }else{
                                    errors.remove(it.dataElement+"_"+it.categoryOptionCombo)
                                }
                                updateData(it)
                            } else {
                                view.updateData(rowAction, it.catCombo)
                            }
                            result
                        } ?: Flowable.just(
                            StoreResult("", ValueStoreResult.VALUE_HAS_NOT_CHANGED)
                        )
                    }
                }.subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    { storeResult ->
                        val valueChange = VALUE_CHANGED
                        if (storeResult.valueStoreResult == valueChange) {
                            getDataSetIndicators()
                        }
                        view.onValueProcessed()
                    },
                    { Timber.e(it) }
                )
        )

        disposable.add(
            repository.getCatCombo().map { it.size }
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    { view.updateTabLayout(it) },
                    { Timber.e(it) }
                )
        )

        disposable.add(
            dataTableModelConnectable.connect()
        )
    }

    private fun initTables(dataTableModelConnectable: ConnectableFlowable<DataTableModel>) {
        if (isComposeTableEnable()) {
            view.updateProgressVisibility()
            disposable.add(
                dataTableModelConnectable.map { repository.setTableData(it, errors) }
                    .map { tableData ->
                        mapper(tableData)
                    }
                    .subscribeOn(schedulerProvider.io())
                    .observeOn(schedulerProvider.ui())
                    .subscribe(
                        {
                            tableState.value = tableState.value?.toMutableList()?.apply {
                                add(addUpperSpaceIfIsNotFirstTable(it))
                            }
                            updateTableList()
                        }
                    ) { Timber.e(it) }
            )

            disposable.add(
                repository.getDataSetIndicators().map { indicatorsData ->
                    mapper.map(indicatorsData)
                }
                    .subscribeOn(schedulerProvider.io())
                    .observeOn(schedulerProvider.ui())
                    .subscribe(
                        {
                            indicatorsState.value = listOf(it)
                            updateTableList()
                        },
                        { Timber.e(it) }
                    )
            )
        } else {
            disposable.add(
                dataTableModelConnectable.map { repository.setTableData(it, errors) }
                    .subscribeOn(schedulerProvider.io())
                    .observeOn(schedulerProvider.ui())
                    .subscribe(
                        view::setTableData
                    ) { Timber.e(it) }
            )
        }
    }

    private fun MutableList<TableModel>.addUpperSpaceIfIsNotFirstTable(
        it: TableModel
    ) = it.copy(upperPadding = this.isNotEmpty())

    private fun updateData(datasetTableModel: DataSetTableModel) {
        val dataTableModel =
            repository.getDataTableModel(datasetTableModel.catCombo!!)
                .blockingFirst()
        val tableData = repository.setTableData(dataTableModel, errors)
        val updatedTableModel = mapper(tableData)

        allTableState.value = allTableState.value?.map { tableModel ->
            if (tableModel.id == datasetTableModel.catCombo) {
                updatedTableModel
            } else {
                tableModel
            }
        }
    }

    private fun updateTableList() {
        allTableState.postValue(
            mutableListOf<TableModel>().apply {
                addAll(tableState.value ?: emptyList())
                addAll(indicatorsState.value ?: emptyList())
            }
        )
    }

    fun getDataSetIndicators() {
        disposable.add(
            repository.getDataSetIndicators()
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    { if (it.isNotEmpty()) view.renderIndicators(it) },
                    { Timber.e(it) }
                )
        )
    }

    fun onDettach() {
        disposable.clear()
    }

    fun getProcessor(): FlowableProcessor<RowAction> {
        return processor
    }

    fun getProcessorOptionSet(): FlowableProcessor<Trio<String, String, Int>> {
        return processorOptionSet
    }

    fun saveCurrentSectionMeasures(rowHeaderWidth: Int, columnHeaderHeight: Int) {
        repository.saveCurrentSectionMeasures(rowHeaderWidth, columnHeaderHeight)
    }

    fun tableData(): LiveData<List<TableModel>> = allTableState

    fun onCellValueChanged(tableCell: TableCell) {
        val updatedData = allTableState.value?.map { tableModel ->
            val hasRowWithDataElement =
                tableModel.tableRows.find { tableCell.id?.contains(it.rowHeader.id.toString()) == true }
            if (hasRowWithDataElement != null) {
                tableModel.copy(
                    overwrittenValues = listOf(tableCell)
                )
            } else {
                tableModel
            }
        }
        allTableState.postValue(updatedData)
    }

    fun isComposeTableEnable(): Boolean {
        return featureConfigRepository?.isFeatureEnable(ANDROAPP_4754) == true
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
        return dataElement.valueType()?.toKeyBoardInputType()?.let { inputType ->
            TextInputModel(
                id = cell.id ?: "",
                mainLabel = dataElement.displayFormName() ?: "-",
                secondaryLabels = repository.getCatOptComboOptions(
                    ids[1]
                ).map {
                    it.displayName() ?: "-"
                },
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
        val ids = cell.id?.split("_")
        val dataElementUid = ids!![0]
        val catOptCombUid = ids[1]
        val catComboUid = allTableState.value?.find { tableModel ->
            tableModel.tableRows.find { tableRowModel ->
                tableRowModel.values.values.find { it.id == cell.id } != null
            } != null
        }?.id

        val row = RowAction.create(
            cell.id!!,
            cell.value,
            dataElementUid,
            catOptCombUid,
            catComboUid,
            cell.row!!,
            cell.column!!
        )
        processor.onNext(row)
    }
}
