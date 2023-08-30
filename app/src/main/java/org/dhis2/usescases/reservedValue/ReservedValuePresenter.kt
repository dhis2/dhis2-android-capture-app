package org.dhis2.usescases.reservedValue

import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.processors.FlowableProcessor
import io.reactivex.processors.PublishProcessor
import org.dhis2.commons.schedulers.SchedulerProvider
import org.dhis2.commons.schedulers.defaultSubscribe
import org.hisp.dhis.android.core.maintenance.D2Error
import timber.log.Timber

class ReservedValuePresenter(
    private val repository: ReservedValueRepository,
    private val schedulerProvider: SchedulerProvider,
    private val view: ReservedValueView,
    private val refillFlowable: Flowable<String>,
) {
    private val disposable: CompositeDisposable = CompositeDisposable()
    private val updateProcessor: FlowableProcessor<Boolean> = PublishProcessor.create()

    fun init() {
        disposable.add(
            updateProcessor.startWith(true)
                .flatMapSingle { repository.reservedValues() }
                .defaultSubscribe(
                    schedulerProvider,
                    { view.setReservedValues(it) },
                    { Timber.e(it) },
                ),
        )

        disposable.add(
            refillFlowable
                .onBackpressureBuffer()
                .parallel()
                .runOn(schedulerProvider.io())
                .flatMap {
                    repository.refillReservedValues(it)
                        .toFlowable(BackpressureStrategy.BUFFER)
                }
                .sequential()
                .subscribe(
                    {
                        Timber.d("Reserved value manager: %s".format(it.percentage()))
                        updateProcessor.onNext(true)
                    },
                    {
                        onReservedValuesError(it)
                    },
                ),
        )
    }

    private fun onReservedValuesError(e: Throwable) {
        if (e is D2Error) {
            view.showReservedValuesError()
        } else {
            Timber.e(e)
        }
    }

    fun onBackClick() {
        view.onBackClick()
    }

    fun onPause() {
        disposable.clear()
    }
}
