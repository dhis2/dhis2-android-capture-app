package org.dhis2.data.server

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import org.dhis2.commons.schedulers.SchedulerProvider
import org.dhis2.commons.schedulers.defaultSubscribe
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.user.AccountDeletionReason
import timber.log.Timber

const val EMPTY_CALLBACK =
    "session callback is empty. Use setSessionCallback when using this class in an activity"

class OpenIdSession(
    val d2: D2,
    val schedulerProvider: SchedulerProvider,
) : LifecycleObserver {
    private val disposable = CompositeDisposable()
    private var sessionCallback: (LogOutReason) -> Unit = { Timber.log(1, EMPTY_CALLBACK) }

    enum class LogOutReason {
        OPEN_ID,
        DISABLED_ACCOUNT,
    }

    fun setSessionCallback(
        lifecycleOwner: LifecycleOwner,
        sessionCallback: (LogOutReason) -> Unit = {},
    ) {
        lifecycleOwner.lifecycle.addObserver(this)
        this.sessionCallback = sessionCallback
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onCreate() {
        disposable.add(
            Observable.merge(
                d2.userModule().openIdHandler().logOutObservable()
                    .map { LogOutReason.OPEN_ID },
                d2.userModule().accountManager().accountDeletionObservable()
                    .filter { it == AccountDeletionReason.ACCOUNT_DISABLED }
                    .map { LogOutReason.DISABLED_ACCOUNT },
            ).defaultSubscribe(
                schedulerProvider,
                { sessionCallback(it) },
                { Timber.e(it) },
            ),
        )
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onDestroy() {
        disposable.clear()
    }
}
