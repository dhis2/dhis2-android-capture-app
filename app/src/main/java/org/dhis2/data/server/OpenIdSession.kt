package org.dhis2.data.server

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import io.reactivex.disposables.CompositeDisposable
import org.dhis2.data.schedulers.SchedulerProvider
import org.dhis2.data.schedulers.defaultSubscribe
import org.hisp.dhis.android.core.D2
import timber.log.Timber

const val EMPTY_CALLBACK =
    "session callback is empty. Use setSessionCallback when using this class in an activity"

class OpenIdSession(
    val d2: D2,
    val schedulerProvider: SchedulerProvider
) : LifecycleObserver {
    private val disposable = CompositeDisposable()
    private var sessionCallback: () -> Unit = { Timber.log(1, EMPTY_CALLBACK) }

    fun setSessionCallback(lifecycleOwner: LifecycleOwner, sessionCallback: () -> Unit = {}) {
        lifecycleOwner.lifecycle.addObserver(this)
        this.sessionCallback = sessionCallback
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {
        disposable.add(
            d2.userModule().openIdHandler().logOutObservable()
                .defaultSubscribe(
                    schedulerProvider,
                    { sessionCallback() },
                    { Timber.e(it) }
                )
        )
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        disposable.clear()
    }
}
