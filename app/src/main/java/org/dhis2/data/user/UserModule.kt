package org.dhis2.data.user

import dagger.Module
import dagger.Provides
import org.dhis2.commons.di.dagger.PerUser
import org.hisp.dhis.android.core.D2

@Module
class UserModule {
    @Provides
    @PerUser
    fun userRepository(d2: D2?): UserRepository {
        return UserRepositoryImpl(d2!!)
    }
}
