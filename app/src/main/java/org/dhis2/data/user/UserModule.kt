package org.dhis2.data.user


import com.squareup.sqlbrite2.BriteDatabase

import org.dhis2.data.dagger.PerUser
import org.hisp.dhis.android.core.D2

import dagger.Module
import dagger.Provides

@Module
@PerUser
class UserModule {

    @Provides
    @PerUser
    internal fun userRepository(briteDatabase: BriteDatabase, d2: D2): UserRepository {
        return UserRepositoryImpl(briteDatabase, d2)
    }

}
