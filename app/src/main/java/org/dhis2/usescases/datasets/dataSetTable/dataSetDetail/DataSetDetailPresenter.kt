package org.dhis2.usescases.datasets.dataSetTable.dataSetDetail

import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.processors.FlowableProcessor
import org.dhis2.commons.data.tuples.Trio
import org.dhis2.commons.matomo.MatomoAnalyticsController
import org.dhis2.commons.schedulers.SchedulerProvider
import org.dhis2.usescases.datasets.dataSetTable.DataSetTableRepositoryImpl
import org.hisp.dhis.android.core.dataset.DataSetInstance
import org.hisp.dhis.android.core.period.Period
import timber.log.Timber

class DataSetDetailPresenter(
    val view: DataSetDetailView,
    val repository: DataSetTableRepositoryImpl,
    val schedulers: SchedulerProvider,
    val matomoAnalyticsController: MatomoAnalyticsController,
    val updateProcessor: FlowableProcessor<Unit>
) {

    private val disposable = CompositeDisposable()

    fun init() {
        disposable.add(
            repository.getDataSetCatComboName()
                .subscribeOn(schedulers.io())
                .observeOn(schedulers.ui())
                .subscribe(
                    { name ->
                        if (name.isNullOrEmpty() || name == "default") {
                            view.hideCatOptCombo()
                        } else {
                            view.setCatOptComboName(name)
                        }
                    },
                    { error -> Timber.d(error) }
                )
        )

        disposable.add(
            updateProcessor.startWith(Unit)
                .switchMap {
                    Flowable.combineLatest<DataSetInstance,
                        Period,
                        Boolean,
                        Trio<DataSetInstance, Period, Boolean>>(
                        repository.dataSetInstance(),
                        repository.getPeriod().toFlowable(),
                        repository.isComplete().toFlowable(),
                        { t1, t2, t3 -> Trio.create(t1, t2, t3) }
                    )
                }
                .subscribeOn(schedulers.io())
                .observeOn(schedulers.ui())
                .subscribe(
                    { data ->
                        view.setDataSetDetails(
                            data.val0()!!,
                            data.val1()!!,
                            data.val2() == true
                        )
                    },
                    { error -> Timber.d(error) }
                )
        )
        disposable.add(
            repository.getDataSet()
                .map { dataSet -> dataSet.style() }
                .subscribeOn(schedulers.io())
                .observeOn(schedulers.ui())
                .subscribe(
                    { style -> view.setStyle(style) },
                    { error -> Timber.d(error) }
                )
        )

        disposable.add(
            view.observeReopenChanges()
                .subscribeOn(schedulers.io())
                .observeOn(schedulers.io())
                .subscribe(
                    { updateProcessor.onNext(Unit) },
                    { Timber.e(it) }
                )
        )
    }

    fun detach() {
        disposable.clear()
    }
}
