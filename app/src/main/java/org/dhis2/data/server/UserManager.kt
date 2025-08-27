package org.dhis2.data.server

import android.content.Intent
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.user.User
import org.hisp.dhis.android.core.user.openid.IntentWithRequestCode
import org.hisp.dhis.android.core.user.openid.OpenIDConnectConfig

interface UserManager {
    fun logIn(username: String, password: String, serverUrl: String): Observable<User?>

    fun logIn(config: OpenIDConnectConfig): Observable<IntentWithRequestCode?>

    fun handleAuthData(serverUrl: String, data: Intent?, requestCode: Int): Observable<User?>

    val isUserLoggedIn: Observable<Boolean>

    fun userName(): Single<String>

    val d2: D2

    val theme: Single<Pair<String?, Int>>

    fun logout(): Completable

    fun allowScreenShare(): Boolean

    suspend fun accountCount(): Int
}
