package org.dhis2.data.server;

import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.user.User;
import org.hisp.dhis.android.core.user.UserCredentials;
import org.hisp.dhis.android.core.user.openid.IntentWithRequestCode;
import org.hisp.dhis.android.core.user.openid.OpenIDConnectConfig;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import kotlin.Pair;

public class UserManagerImpl implements UserManager {
    private final D2 d2;
    private final ServerSettingsRepository repository;

    public UserManagerImpl(@NonNull D2 d2, ServerSettingsRepository repository) {
        this.d2 = d2;
        this.repository = repository;
    }

    @NonNull
    @Override
    public Observable<User> logIn(@NonNull String username, @NonNull String password, @NonNull String serverUrl) {
        return Observable.defer(() -> d2.userModule().logIn(username, password, serverUrl).toObservable());
    }

    @NonNull
    @Override
    public Observable<IntentWithRequestCode> logIn(@NonNull OpenIDConnectConfig config) {
        return Observable.defer(() -> d2.userModule().openIdHandler().logIn(config).toObservable());
    }

    @NonNull
    @Override
    public Observable<User> handleAuthData(@NonNull String serverUrl, @Nullable Intent data, int requestCode) {
        return Observable.defer(() -> d2.userModule().openIdHandler().handleLogInResponse(serverUrl, data, requestCode).toObservable());
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

    @NonNull
    @Override
    public Single<Pair<String, Integer>> getTheme() {
        return repository.getTheme();
    }

    public Completable logout() {
        return d2.userModule().logOut();
    }

    @Override
    public boolean allowScreenShare() {
        return repository.allowScreenShare();
    }
}
