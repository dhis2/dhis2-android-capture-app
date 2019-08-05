package org.dhis2.data.user;

import com.squareup.sqlbrite2.BriteDatabase;

import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.user.UserCredentials;

import androidx.annotation.NonNull;
import io.reactivex.Flowable;

public class UserRepositoryImpl implements UserRepository {

    private final BriteDatabase briteDatabase;
    private final D2 d2;

    UserRepositoryImpl(@NonNull BriteDatabase briteDatabase, D2 d2) {
        this.briteDatabase = briteDatabase;
        this.d2 = d2;
    }

    @NonNull
    @Override
    public Flowable<UserCredentials> credentials() {
        return Flowable.fromCallable(() -> d2.userModule().userCredentials.blockingGet());
    }

    /*@NonNull
    @Override
    public Flowable<User> me() {
        return Flowable.fromCallable(() -> d2.userModule().user.blockingGet());
    }*/
}
