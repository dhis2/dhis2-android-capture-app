package org.dhis2.data.server;

import androidx.annotation.NonNull;

import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.user.User;

import io.reactivex.Observable;

public class UserManagerImpl implements UserManager {
    private final D2 d2;

    public UserManagerImpl(@NonNull D2 d2) {
        this.d2 = d2;
    }

    @NonNull
    @Override
    public Observable<User> logIn(@NonNull String username, @NonNull String password) {
        return Observable.defer(() -> d2.userModule().logIn(username, password).toObservable());
    }

    @NonNull
    @Override
    public Observable<Boolean> isUserLoggedIn() {
        return Observable.defer(() -> d2.userModule().isLogged().toObservable());
    }

    @Override
    public D2 getD2() {
        return d2;
    }
}
