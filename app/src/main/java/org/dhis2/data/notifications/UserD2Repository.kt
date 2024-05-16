package org.dhis2.data.notifications

import org.dhis2.usescases.notifications.domain.User
import org.dhis2.usescases.notifications.domain.UserRepository
import org.hisp.dhis.android.core.D2

class UserD2Repository(
    private val d2: D2,
) : UserRepository {

    override fun getCurrentUser(): User {
        val user = d2.userModule().user().blockingGet()
        return  User(
            user!!.uid(),
            user.displayName()!!
        )
    }
}

