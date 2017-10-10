package com.data.user;

import android.support.annotation.NonNull;

import com.squareup.sqlbrite2.BriteDatabase;

import org.hisp.dhis.android.core.user.UserCredentialsModel;
import org.hisp.dhis.android.core.user.UserModel;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;

public class UserRepositoryImpl implements UserRepository {
    private static final String SELECT_USER = "SELECT * FROM " +
            UserModel.TABLE + " LIMIT 1";
    private static final String SELECT_USER_CREDENTIALS = "SELECT * FROM " +
            UserCredentialsModel.TABLE + " LIMIT 1";

    private final BriteDatabase briteDatabase;

    public UserRepositoryImpl(@NonNull BriteDatabase briteDatabase) {
        this.briteDatabase = briteDatabase;
    }

    @NonNull
    @Override
    public Flowable<UserCredentialsModel> credentials() {
        // we don't want to track updates on this table

        /*return RxJavaInterop.toV2Flowable(briteDatabase
                .createQuery(UserCredentialsModel.TABLE, SELECT_USER_CREDENTIALS)
                .mapToOne(UserCredentialsModel::create)
                .take(1));*/
        return briteDatabase
                .createQuery(UserCredentialsModel.TABLE, SELECT_USER_CREDENTIALS)
                .mapToOne(UserCredentialsModel::create)
                .take(1).toFlowable(BackpressureStrategy.BUFFER);
    }

    @NonNull
    @Override
    public Flowable<UserModel> me() {
        /*return RxJavaInterop.toV2Flowable(briteDatabase
                .createQuery(UserModel.TABLE, SELECT_USER)
                .mapToOne(UserModel::create));*/
        return briteDatabase
                .createQuery(UserModel.TABLE, SELECT_USER)
                .mapToOne(UserModel::create).toFlowable(BackpressureStrategy.BUFFER);
    }
}
