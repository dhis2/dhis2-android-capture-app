package org.dhis2.data.server

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import org.hisp.dhis.android.core.D2

interface UserManager {
    fun isUserLoggedIn(): Observable<Boolean>

    val d2: D2

    val theme: Single<Pair<String?, Int>>

    fun logout(): Completable

    fun allowScreenShare(): Boolean

    fun needsOfflinePin(): Observable<Boolean>
}
