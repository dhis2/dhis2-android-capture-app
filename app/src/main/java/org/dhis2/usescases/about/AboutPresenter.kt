package org.dhis2.usescases.about

import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import org.dhis2.commons.schedulers.SchedulerProvider
import org.dhis2.data.user.UserRepository
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.user.UserCredentials
import timber.log.Timber

class AboutPresenter(
    private val view: AboutView,
    private val d2: D2,
    private val provider: SchedulerProvider,
    private val userRepository: UserRepository
) {
    var disposable = CompositeDisposable()
    fun init() {
        disposable.add(
            Flowable.zip<UserCredentials, String, Pair<UserCredentials, String>>(
                userRepository.credentials(),
                d2.systemInfoModule().systemInfo().get().toFlowable()
                    .map { it.contextPath() ?: "" },
                BiFunction { fields, result -> Pair(fields, result) }
            ).cacheWithInitialCapacity(1)
                .subscribeOn(provider.io())
                .observeOn(provider.ui())
                .subscribe(
                    { aboutData ->
                        view.renderUserCredentials(aboutData.first)
                        view.renderServerUrl(aboutData.second)
                    }
                ) { t: Throwable? ->
                    Timber.e(t)
                }
        )
    }

    fun onPause() {
        disposable.clear()
    }
}
