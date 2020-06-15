package org.dhis2.data.user;

import org.hisp.dhis.android.core.D2;

public class FakeUserModule extends UserModule {
    @Override
    UserRepository userRepository(D2 d2) {
        return new FakeUserRepositoryImpl(d2);
    }
}
