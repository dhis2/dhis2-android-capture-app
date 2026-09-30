package org.dhis2.data.server

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import org.dhis2.commons.prefs.Preference.Companion.PIN
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.common.AuthorizationType
import java.util.concurrent.Callable

class UserManagerImpl(
    override val d2: D2,
    private val repository: ServerSettingsRepository,
) : UserManager {
    override fun isUserLoggedIn(): Observable<Boolean> =
        Observable.defer(
            Callable {
                d2.userModule().rxIsLogged().toObservable()
            },
        )

    override val theme: Single<Pair<String?, Int>> = repository.getTheme()

    override fun logout(): Completable = d2.userModule().rxLogOut()

    override fun allowScreenShare(): Boolean = repository.allowScreenShare()

    override fun needsOfflinePin(): Observable<Boolean> =
        Observable.fromCallable {
            if (d2.userModule().blockingIsLogged()) {
                val authType =
                    d2
                        .userModule()
                        .accountManager()
                        .getCurrentAccount()
                        ?.authorizationType
                val isPinSet =
                    d2
                        .dataStoreModule()
                        .localDataStore()
                        .value(PIN)
                        .blockingExists()
                (authType == AuthorizationType.OAUTH2 || authType == AuthorizationType.OPEN_ID_CONNECT) && !isPinSet
            } else {
                false
            }
        }
}
