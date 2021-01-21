package org.dhis2.data.schedulers

import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.disposables.Disposable

interface SchedulerProvider {
    fun computation(): Scheduler
    fun io(): Scheduler
    fun ui(): Scheduler
}

fun <T> Observable<T>.defaultSubscribe(
    schedulerProvider: SchedulerProvider,
    onNext: (T) -> Unit? = {},
    onError: (Throwable) -> Unit? = {},
    onComplete: () -> Unit? = {}
): Disposable {
    return subscribeOn(schedulerProvider.io())
        .observeOn(schedulerProvider.ui())
        .subscribe(
            { onNext(it) },
            { onError(it) },
            { onComplete() }
        )
}

fun <T> Flowable<T>.defaultSubscribe(
    schedulerProvider: SchedulerProvider,
    onNext: (T) -> Unit? = {},
    onError: (Throwable) -> Unit? = {},
    onComplete: () -> Unit? = {}
): Disposable {
    return subscribeOn(schedulerProvider.io())
        .observeOn(schedulerProvider.ui())
        .subscribe(
            { onNext(it) },
            { onError(it) },
            { onComplete() }
        )
}
