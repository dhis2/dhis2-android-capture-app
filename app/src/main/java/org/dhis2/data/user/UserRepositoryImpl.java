package org.dhis2.data.user;

import com.squareup.sqlbrite2.BriteDatabase;

import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.user.UserCredentialsModel;
import org.hisp.dhis.android.core.user.UserModel;

import androidx.annotation.NonNull;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;

public class UserRepositoryImpl implements UserRepository {
    private static final String SELECT_USER = "SELECT * FROM " +
            UserModel.TABLE + " LIMIT 1";
    private static final String SELECT_USER_CREDENTIALS = "SELECT * FROM " +
            UserCredentialsModel.TABLE + " LIMIT 1";

    private final BriteDatabase briteDatabase;
    private final D2 d2;

    UserRepositoryImpl(@NonNull BriteDatabase briteDatabase, D2 d2) {
        this.briteDatabase = briteDatabase;
        this.d2 = d2;
    }

    @NonNull
    @Override
    public Flowable<UserCredentialsModel> credentials() {
        return briteDatabase
                .createQuery(UserCredentialsModel.TABLE, SELECT_USER_CREDENTIALS)
                .mapToOne(UserCredentialsModel::create)
                .take(1).toFlowable(BackpressureStrategy.BUFFER);
    }

    @NonNull
    @Override
    public Flowable<UserModel> me() {
        return briteDatabase
                .createQuery(UserModel.TABLE, SELECT_USER)
                .mapToOne(UserModel::create).toFlowable(BackpressureStrategy.BUFFER);
    }
}
