package com.data.server;

import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.user.User;

import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;
import retrofit2.Response;

class UserManagerImpl implements UserManager {
    private final D2 d2;

    UserManagerImpl(@NonNull D2 d2) {
        this.d2 = d2;
    }

    @NonNull
    @Override
    public Observable<Response<User>> logIn(@NonNull String username, @NonNull String password) {
        return Observable.defer(() -> Observable.fromCallable(d2.logIn(username, password)));
    }

    @NonNull
    @Override
    public Observable<Boolean> isUserLoggedIn() {
        return Observable.defer(() -> Observable.fromCallable(d2.isUserLoggedIn()));
    }
}
