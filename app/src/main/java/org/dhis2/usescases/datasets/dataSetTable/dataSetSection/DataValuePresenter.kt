package org.dhis2.usescases.datasets.dataSetTable.dataSetSection

import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.processors.FlowableProcessor
import io.reactivex.processors.PublishProcessor
import org.dhis2.commons.data.tuples.Trio
import org.dhis2.commons.schedulers.SchedulerProvider
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
    private val updateProcessor: FlowableProcessor<Unit>
) {

    var disposable: CompositeDisposable = CompositeDisposable()

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
        }.publish()

        disposable.add(
            dataTableModelConnectable.map(repository::setTableData)
                .doOnComplete { getDataSetIndicators() }
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    view::setTableData
                ) { Timber.e(it) }
        )

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

    private fun getDataSetIndicators() {
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
}
