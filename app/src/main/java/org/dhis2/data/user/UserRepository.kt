package org.dhis2.data.user

import io.reactivex.Flowable
import org.hisp.dhis.android.core.user.User

interface UserRepository {
    fun credentials(): Flowable<User>
}
