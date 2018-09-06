package org.dhis2.data.user;


import org.dhis2.data.dagger.PerUser;
import com.squareup.sqlbrite2.BriteDatabase;

import dagger.Module;
import dagger.Provides;

@Module
@PerUser
public class UserModule {

    @Provides
    @PerUser
    UserRepository userRepository(BriteDatabase briteDatabase) {
        return new UserRepositoryImpl(briteDatabase);
    }

}
