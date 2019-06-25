package org.dhis2.data.server

import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.user.User

import io.reactivex.Observable

class UserManagerImpl(override val d2: D2) : UserManager {

    override val isUserLoggedIn: Observable<Boolean>
        get() = Observable.defer { Observable.fromCallable(d2.userModule().isLogged) }

    override fun logIn(username: String, password: String): Observable<User> {
        return Observable.defer { Observable.fromCallable(d2.userModule().logIn(username, password)) }
    }
}
