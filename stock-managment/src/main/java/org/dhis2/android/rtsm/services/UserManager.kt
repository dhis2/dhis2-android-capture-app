package org.dhis2.android.rtsm.services

import android.content.Intent
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import org.hisp.dhis.android.core.user.User
import org.hisp.dhis.android.core.user.openid.IntentWithRequestCode
import org.hisp.dhis.android.core.user.openid.OpenIDConnectConfig

interface UserManager {
    fun login(username: String, password: String, serverUrl: String): Observable<User?>

    fun login(config: OpenIDConnectConfig): Observable<IntentWithRequestCode?>

    fun handleAuthData(serverUrl: String, data: Intent?, requestCode: Int): Observable<User?>

    fun isUserLoggedIn(): Observable<Boolean?>

    fun userName(): Single<String?>

    fun logout(): Completable?
}