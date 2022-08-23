package org.dhis2.android.rtsm.services

import android.content.Intent
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.user.User
import org.hisp.dhis.android.core.user.openid.IntentWithRequestCode
import org.hisp.dhis.android.core.user.openid.OpenIDConnectConfig
import javax.inject.Inject

class UserManagerImpl @Inject constructor(val d2: D2) : UserManager {

    override fun login(username: String, password: String, serverUrl: String): Observable<User?> {
        return Observable.defer {
            d2.userModule().logIn(username, password, serverUrl).toObservable()
        }
    }

    override fun login(config: OpenIDConnectConfig): Observable<IntentWithRequestCode?> {
        return Observable.defer {
            d2.userModule().openIdHandler().logIn(config).toObservable()
        }
    }

    override fun handleAuthData(
        serverUrl: String,
        data: Intent?,
        requestCode: Int
    ): Observable<User?> {
        return Observable.defer {
            d2.userModule().openIdHandler()
                .handleLogInResponse(serverUrl, data, requestCode).toObservable()
        }
    }

    override fun isUserLoggedIn(): Observable<Boolean?> {
        return Observable.defer {
            d2.userModule().isLogged.toObservable()
        }
    }

    override fun userName(): Single<String?> {
        return Single.defer{
            d2.userModule().userCredentials().get().map { it.username() }
        }
    }

    override fun logout(): Completable? {
        return d2.userModule().logOut()
    }
}