package org.dhis2.commons.schedulers

import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import timber.log.Timber

interface SchedulerProvider {
    fun computation(): Scheduler
    fun io(): Scheduler
    fun ui(): Scheduler
}

fun <T> Single<T>.defaultSubscribe(
    schedulerProvider: SchedulerProvider,
    onSuccess: (T) -> Unit? = {},
    onError: (Throwable) -> Unit? = {},
): Disposable {
    return subscribeOn(schedulerProvider.io())
        .observeOn(schedulerProvider.ui())
        .subscribe(
            { onSuccess(it) },
            { onError(it) },
        )
}

fun <T> Observable<T>.defaultSubscribe(
    schedulerProvider: SchedulerProvider,
    onNext: (T) -> Unit? = {},
    onError: (Throwable) -> Unit? = {},
    onComplete: () -> Unit? = {},
): Disposable {
    return subscribeOn(schedulerProvider.io())
        .observeOn(schedulerProvider.ui())
        .subscribe(
            { onNext(it) },
            { onError(it) },
            { onComplete() },
        )
}

fun <T> Flowable<T>.defaultSubscribe(
    schedulerProvider: SchedulerProvider,
    onNext: (T) -> Unit? = {},
    onError: (Throwable) -> Unit? = { Timber.d(it) },
    onComplete: () -> Unit? = {},
): Disposable {
    return subscribeOn(schedulerProvider.io())
        .observeOn(schedulerProvider.ui())
        .subscribe(
            { onNext(it) },
            { onError(it) },
            { onComplete() },
        )
}
