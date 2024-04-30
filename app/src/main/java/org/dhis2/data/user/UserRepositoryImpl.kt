package org.dhis2.data.user

import io.reactivex.Flowable
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.user.User

class UserRepositoryImpl(private val d2: D2) : UserRepository {
    override fun credentials(): Flowable<User> {
        return Flowable.fromCallable { d2.userModule().user().blockingGet() }
    }
}
