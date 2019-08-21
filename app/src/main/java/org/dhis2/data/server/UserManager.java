package org.dhis2.data.server;

import androidx.annotation.NonNull;

import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.user.User;

import io.reactivex.Observable;
import io.reactivex.Single;

public interface UserManager {

    @NonNull
    Observable<User> logIn(@NonNull String username, @NonNull String password);

    @NonNull
    Observable<Boolean> isUserLoggedIn();

    @NonNull
    Single<String> userInitials();

    @NonNull
    Single<String> userFullName();

    @NonNull
    Single<String> userName();

    D2 getD2();
}
