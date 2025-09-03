package org.dhis2.data.server

import android.content.Intent
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.user.User
import org.hisp.dhis.android.core.user.openid.IntentWithRequestCode
import org.hisp.dhis.android.core.user.openid.OpenIDConnectConfig
import java.util.concurrent.Callable

class UserManagerImpl(override val d2: D2, private val repository: ServerSettingsRepository) : UserManager {
    override fun logIn(username: String, password: String, serverUrl: String): Observable<User?> {
        return Observable.defer<User?>(
            Callable {
                d2.userModule().logIn(username, password, serverUrl).toObservable()
            },
        )
    }

    override fun logIn(config: OpenIDConnectConfig): Observable<IntentWithRequestCode?> {
        return Observable.defer<IntentWithRequestCode?>(
            Callable {
                d2.userModule().openIdHandler().logIn(config).toObservable()
            },
        )
    }

    override fun handleAuthData(
        serverUrl: String,
        data: Intent?,
        requestCode: Int,
    ): Observable<User?> {
        return Observable.defer<User?>(
            Callable {
                d2.userModule().openIdHandler().handleLogInResponse(serverUrl, data, requestCode)
                    .toObservable()
            },
        )
    }

    override val isUserLoggedIn: Observable<Boolean>
        get() = d2.userModule().isLogged().toObservable()

    override fun userName(): Single<String> {
        return d2.userModule().user().get().map { it.name() }
    }

    override val theme: Single<Pair<String?, Int>>
        get() = repository.getTheme()

    override fun logout(): Completable {
        return d2.userModule().logOut()
    }

    override fun allowScreenShare(): Boolean {
        return repository.allowScreenShare()
    }

    override suspend fun accountCount(): Int {
        return d2.userModule().accountManager().getAccounts().count()
    }
}
