package org.dhis2.data.server;

import androidx.annotation.NonNull;

import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.user.User;
import org.hisp.dhis.android.core.user.UserCredentials;

import io.reactivex.Observable;
import io.reactivex.Single;

public class UserManagerImpl implements UserManager {
    private final D2 d2;

    public UserManagerImpl(@NonNull D2 d2) {
        this.d2 = d2;
    }

    @NonNull
    @Override
    public Observable<User> logIn(@NonNull String username, @NonNull String password, @NonNull String serverUrl) {
        return Observable.defer(() -> d2.userModule().logIn(username, password, serverUrl).toObservable());
    }

    @NonNull
    @Override
    public Observable<Boolean> isUserLoggedIn() {
        return Observable.defer(() -> d2.userModule().isLogged().toObservable());
    }

    @NonNull
    @Override
    public Single<String> userInitials() {
        return Single.defer(() -> d2.userModule().user().get())
                .map(user -> {
                    String fn = user.firstName() != null ? user.firstName() : "";
                    String sn = user.surname() != null ? user.surname() : "";
                    return String.format("%s%s", fn.charAt(0), sn.charAt(0));
                });
    }

    @Override
    @NonNull
    public Single<String> userFullName() {
        return Single.defer(() -> d2.userModule().user().get())
                .map(user -> String.format("%s %s", user.firstName(), user.surname()));
    }

    @NonNull
    @Override
    public Single<String> userName() {

        return Single.defer(() -> d2.userModule().userCredentials().get())
                .map(UserCredentials::username);
    }

    @Override
    public D2 getD2() {
        return d2;
    }
}
