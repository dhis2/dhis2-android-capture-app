package org.dhis2.usescases.datasets.dataSetTable

import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import org.dhis2.data.schedulers.SchedulerProvider
import org.dhis2.data.tuples.Pair
import org.dhis2.utils.analytics.AnalyticsHelper
import org.dhis2.utils.analytics.CLICK
import org.dhis2.utils.analytics.INFO_DATASET_TABLE
import org.hisp.dhis.android.core.dataset.DataSet
import timber.log.Timber

class DataSetTablePresenter(
    private val view: DataSetTableView,
    private val tableRepository: DataSetTableRepository,
    private val schedulerProvider: SchedulerProvider,
    private val analyticsHelper: AnalyticsHelper
) {

    val disposable = CompositeDisposable()

    fun init(catCombo: String) {
        disposable.add(
            tableRepository.sections
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    view::setSections,
                    Timber::e
                )
        )

        disposable.add(
            Flowable.zip<DataSet, String, Pair<DataSet, String>>(
                tableRepository.dataSet,
                tableRepository.getCatComboName(catCombo),
                BiFunction { dataSet, catComboName -> Pair.create(dataSet, catComboName) }
            )
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    { data -> view.renderDetails(data.val0(), data.val1()) },
                    Timber::e
                )
        )

        disposable.add(
            tableRepository.dataSetStatus()
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    view::isDataSetOpen,
                    Timber::d
                )
        )

        disposable.add(
            tableRepository.dataSetState()
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    view::setDataSetState,
                    Timber::d
                )
        )
    }

    fun onBackClick() {
        view.back()
    }

    fun onSyncClick() {
        view.showSyncDialog()
    }

    fun onDettach() {
        disposable.dispose()
    }

    fun displayMessage(message: String) {
        view.displayMessage(message)
    }

    fun optionsClick() {
        analyticsHelper.setEvent(INFO_DATASET_TABLE, CLICK, INFO_DATASET_TABLE)
        view.showOptions()
    }

    fun onClickSelectTable(numTable: Int) {
        view.goToTable(numTable)
    }

    fun getCatOptComboFromOptionList(catOpts: List<String>): String {
        return tableRepository.getCatOptComboFromOptionList(catOpts)
    }

    fun updateState() {
        disposable.add(
            tableRepository.dataSetState()
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    view::setDataSetState,
                    Timber::d
                )
        )
    }
}
