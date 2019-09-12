package org.dhis2.data.user;


import org.dhis2.data.dagger.PerUser;
import org.hisp.dhis.android.core.D2;

import dagger.Module;
import dagger.Provides;

@Module
@PerUser
public class UserModule {

    @Provides
    @PerUser
    UserRepository userRepository(D2 d2) {
        return new UserRepositoryImpl(d2);
    }

}
