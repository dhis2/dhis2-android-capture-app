package org.dhis2.data.user;

import androidx.annotation.NonNull;

import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.user.UserCredentials;

import io.reactivex.Flowable;

public class UserRepositoryImpl implements UserRepository {

    private final D2 d2;

    UserRepositoryImpl(D2 d2) {
        this.d2 = d2;
    }

    @NonNull
    @Override
    public Flowable<UserCredentials> credentials() {
        return Flowable.fromCallable(() -> d2.userModule().userCredentials.blockingGet());
    }
}
