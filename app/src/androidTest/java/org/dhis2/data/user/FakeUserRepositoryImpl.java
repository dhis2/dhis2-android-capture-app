package org.dhis2.data.user;

import androidx.annotation.NonNull;

import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.common.ObjectWithUid;
import org.hisp.dhis.android.core.user.UserCredentials;
import org.hisp.dhis.android.core.user.UserRole;

import java.util.Collections;

import io.reactivex.Flowable;

public class FakeUserRepositoryImpl extends UserRepositoryImpl {


    private UserCredentials testingCredentials = UserCredentials.builder()
            .username("TestingAndroid")
            .userRoles(
                    Collections.singletonList(
                            UserRole.builder()
                                    .name("Superuser")
                                    .displayName("Superuser")
                                    .build()
                    )
            )
            .user(
                    ObjectWithUid.create("testingUid")
            )
            .build();

    FakeUserRepositoryImpl(D2 d2) {
        super(d2);
    }

    @NonNull
    @Override
    public Flowable<UserCredentials> credentials() {
        return Flowable.just(testingCredentials);
    }
}
