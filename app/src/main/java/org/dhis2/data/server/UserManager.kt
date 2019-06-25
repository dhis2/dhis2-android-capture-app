package org.dhis2.data.server

import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.user.User

import io.reactivex.Observable

interface UserManager {

    val isUserLoggedIn: Observable<Boolean>

    val d2: D2

    fun logIn(username: String, password: String): Observable<User>
}
