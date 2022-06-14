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
import org.dhis2.commons.featureconfig.model.Feature
import org.dhis2.commons.schedulers.SchedulerProvider
import org.dhis2.compose_table.model.TableModel
import org.dhis2.data.forms.dataentry.ValueStore
import org.dhis2.data.forms.dataentry.tablefields.RowAction
import org.dhis2.form.model.StoreResult
import org.dhis2.form.model.ValueStoreResult
import org.dhis2.usescases.datasets.dataSetTable.DataSetTableModel
import timber.log.Timber

class DataValuePresenter(
    private val view: DataValueContract.View,
    private val repository: DataValueRepository,
    private val valueStore: ValueStore,
    private val schedulerProvider: SchedulerProvider,
    private val updateProcessor: FlowableProcessor<Unit>,
    private val featureConfigRepository: FeatureConfigRepository? = null
) {
    private val mapper = TableDataToTableModelMapper()
    var disposable: CompositeDisposable = CompositeDisposable()

    private val tableState: MutableLiveData<List<TableModel>> = MutableLiveData(emptyList())
    private val indicatorsState: MutableLiveData<List<TableModel>> = MutableLiveData(emptyList())
    private val allTableState: MutableLiveData<List<TableModel>> = MutableLiveData(emptyList())

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
                            view.updateData(rowAction, it.catCombo)
                            valueStore.save(it)
                        } ?: Flowable.just(
                            StoreResult("", ValueStoreResult.VALUE_HAS_NOT_CHANGED)
                        )
                    }
                }.subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    { storeResult ->
                        val valueChange = ValueStoreResult.VALUE_CHANGED
                        if (storeResult.valueStoreResult == valueChange) {
                            getDataSetIndicators()
                            view.showSnackBar()
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
                dataTableModelConnectable.map(repository::setTableData)
                    .map { tableData ->
                        mapper.map(tableData)
                    }
                    .subscribeOn(schedulerProvider.io())
                    .observeOn(schedulerProvider.ui())
                    .subscribe(
                        {
                            tableState.value = tableState.value?.toMutableList()?.apply {
                                add(it)
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
                dataTableModelConnectable.map(repository::setTableData)
                    .subscribeOn(schedulerProvider.io())
                    .observeOn(schedulerProvider.ui())
                    .subscribe(
                        view::setTableData
                    ) { Timber.e(it) }
            )
        }
    }

    private fun updateTableList() {
        allTableState.value = mutableListOf<TableModel>().apply {
            addAll(tableState.value ?: emptyList())
            addAll(indicatorsState.value ?: emptyList())
        }
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
    fun isComposeTableEnable(): Boolean {
        return featureConfigRepository?.isFeatureEnable(Feature.ANDROAPP_4754) == true
    }
}
